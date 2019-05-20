package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.helper.MergeListHelper;
import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodVariableAnalysis;
import com.finalproject.automated.refactoring.tool.model.BlockModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.PropertyModel;
import com.finalproject.automated.refactoring.tool.model.StatementModel;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 14 may 2019
 */

@Service
public class MethodVariableAnalysisImpl implements MethodVariableAnalysis {

    private static final String WHITESPACE_REGEX = "\\s";
    private static final String SPACE_DELIMITER = " ";
    private static final String ESCAPE = "\\";
    private static final String DOUBLE_QUOTES = "\"";
    private static final String EMPTY_STRING = "";
    private static final String OPEN_PARENTHESES = "(";
    private static final String UNUSED_CHARACTERS_REGEX = "(?:[,):;!+-])+";
    private static final String POINT_REGEX = "\\.";
    private static final String VARIABLE_NAME_REGEX = "^(?:[a-zA-Z_$])(?:[a-zA-Z0-9_$<>\\[\\]])*";
    private static final String OPEN_SQUARE_BRACKETS = "[";
    private static final String OPEN_SQUARE_BRACKETS_REGEX = "\\" + OPEN_SQUARE_BRACKETS;
    private static final String CLOSE_SQUARE_BRACKETS = "]";

    private static final Integer FIRST_INDEX = 0;
    private static final Integer SECOND_INDEX = 1;
    private static final Integer SINGLE_LIST_SIZE = 1;

    private static final List<String> KEYWORDS = Arrays.asList(
            "class",
            "if", "else",
            "switch", "case", "default",
            "continue", "break",
            "try", "catch", "finally",
            "for", "while", "do",
            "super", "this",
            "new", "instanceof", "new", "return",
            "throw", "assert",
            "true", "false", "null"
    );

    private static final List<String> PRIMITIVE_TYPES = Arrays.asList(
            "byte", "char", "short", "int", "long",
            "float", "double",
            "boolean"
    );

    @Override
    public void analysis(@NonNull MethodModel methodModel) {
        methodModel.getStatements()
                .forEach(statementModel -> readStatement(statementModel, methodModel));
    }

    private void readStatement(StatementModel statementModel, MethodModel methodModel) {
        readVariable(statementModel.getStatement(), methodModel);

        if (statementModel instanceof BlockModel) {
            readBlockStatement(statementModel, methodModel);
        }
    }

    private void readBlockStatement(StatementModel statementModel, MethodModel methodModel) {
        ((BlockModel) statementModel).getStatements()
                .forEach(blockStatementModel -> readStatement(blockStatementModel, methodModel));
    }

    private void readVariable(String statement, MethodModel methodModel) {
        List<String> variables = Arrays.asList(statement.split(WHITESPACE_REGEX));
        variables = new ArrayList<>(variables);

        normalizeString(variables);
        doReadVariable(variables, methodModel);
    }

    private void normalizeString(List<String> variables) {
        List<Integer> mergeIndex = new ArrayList<>();
        AtomicBoolean isString = new AtomicBoolean();

        for (Integer index = FIRST_INDEX; index < variables.size(); index++) {
            String variable = variables.get(index);

            if (isMergePoint(variable, isString)) {
                mergeIndex.add(index);
            }
        }

        MergeListHelper.mergeListOfString(variables, mergeIndex, SPACE_DELIMITER);
    }

    private Boolean isMergePoint(String variable, AtomicBoolean isString) {
        AtomicBoolean isEscape = new AtomicBoolean();
        Boolean lastIsString = isString.get();

        for (Integer index = FIRST_INDEX; index < variable.length(); index++) {
            String character = variable.substring(index, index + SECOND_INDEX);

            if (isLastEscape(isEscape)) {
                continue;
            }

            ifEscape(character, isEscape);
            ifString(character, isString);
        }

        return isVariableString(lastIsString, isString.get());
    }

    private Boolean isLastEscape(AtomicBoolean isEscape) {
        Boolean isLastEscape = isEscape.get();

        if (isLastEscape) {
            isEscape.set(Boolean.FALSE);
        }

        return isLastEscape;
    }

    private void ifEscape(String character, AtomicBoolean isEscape) {
        if (character.equals(ESCAPE)) {
            isEscape.set(Boolean.TRUE);
        }
    }

    private void ifString(String character, AtomicBoolean isString) {
        if (character.equals(DOUBLE_QUOTES)) {
            isString.set(!isString.get());
        }
    }

    private Boolean isVariableString(Boolean lastIsString, Boolean isString) {
        return lastIsString != isString;
    }

    private void doReadVariable(List<String> variables, MethodModel methodModel) {
        AtomicBoolean isClass = new AtomicBoolean();

        variables.stream()
                .filter(this::isNotString)
                .flatMap(this::removeUnusedCharacter)
                .flatMap(this::splitArrayVariables)
                .map(this::removeCalledFields)
                .filter(this::isVariable)
                .forEach(variable -> saveVariable(variable, isClass, methodModel));
    }

    private Boolean isNotString(String variable) {
        return !variable.contains(DOUBLE_QUOTES);
    }

    private Stream<String> removeUnusedCharacter(String variable) {
        variable = variable.replaceAll(UNUSED_CHARACTERS_REGEX, EMPTY_STRING);

        if (variable.contains(OPEN_PARENTHESES)) {
            variable = variable.replace(OPEN_PARENTHESES, OPEN_PARENTHESES + SPACE_DELIMITER);
        }

        return splitMethodParameters(variable);
    }

    private Stream<String> splitMethodParameters(String variable) {
        List<String> split = Arrays.asList(variable.split(WHITESPACE_REGEX));
        split = createNewListIfSplitEmpty(split, variable);

        return split.stream();
    }

    private List<String> createNewListIfSplitEmpty(List<String> split, String variable) {
        if (split.isEmpty()) {
            return Collections.singletonList(variable);
        }

        return split;
    }

    private Stream<String> splitArrayVariables(String variable) {
        List<String> split = Arrays.asList(variable.split(OPEN_SQUARE_BRACKETS_REGEX));
        split = new ArrayList<>(split);

        if (split.size() > SINGLE_LIST_SIZE) {
            customizeArrayVariables(split);
        }

        return split.stream();
    }

    private void customizeArrayVariables(List<String> split) {
        if (split.get(SECOND_INDEX).equals(CLOSE_SQUARE_BRACKETS)) {
            undoSplitArrayVariables(split);
        } else {
            removeFirstIndex(split);
        }
    }

    private void undoSplitArrayVariables(List<String> split) {
        split.set(FIRST_INDEX, String.join(OPEN_SQUARE_BRACKETS,
                split.get(FIRST_INDEX), split.get(SECOND_INDEX)));
        split.remove(SECOND_INDEX.intValue());
    }

    private void removeFirstIndex(List<String> split) {
        for (Integer index = FIRST_INDEX; index < split.size(); index++) {
            split.set(index, removeCloseSquareBrackets(split.get(index)));
        }

        split.remove(FIRST_INDEX.intValue());
    }

    private String removeCloseSquareBrackets(String variable) {
        return variable.replace(CLOSE_SQUARE_BRACKETS, EMPTY_STRING);
    }

    private String removeCalledFields(String variable) {
        List<String> split = Arrays.asList(variable.split(POINT_REGEX));
        split = createNewListIfSplitEmpty(split, variable);

        ifContainsKeywords(split);

        return split.get(FIRST_INDEX)
                .trim();
    }

    private void ifContainsKeywords(List<String> split) {
        if (isContainsKeywords(split)) {
            split.set(FIRST_INDEX, split.get(SECOND_INDEX));
        }
    }

    private Boolean isContainsKeywords(List<String> split) {
        return split.size() > SINGLE_LIST_SIZE && KEYWORDS.contains(split.get(FIRST_INDEX));
    }

    private Boolean isVariable(String variable) {
        return variable.matches(VARIABLE_NAME_REGEX) &&
                !KEYWORDS.contains(variable);
    }

    private void saveVariable(String variable, AtomicBoolean isClass, MethodModel methodModel) {
        if (isClassName(variable)) {
            savePropertyType(variable, isClass, methodModel);
        } else {
            checkVariableDomain(variable, isClass, methodModel);
        }
    }

    private Boolean isClassName(String variable) {
        return Character.isUpperCase(variable.charAt(FIRST_INDEX));
    }

    private void savePropertyType(String variable, AtomicBoolean isClass, MethodModel methodModel) {
        PropertyModel propertyModel = PropertyModel.builder()
                .type(variable)
                .build();

        methodModel.getLocalVariables()
                .add(propertyModel);

        isClass.set(Boolean.TRUE);
    }

    private void checkVariableDomain(String variable, AtomicBoolean isClass, MethodModel methodModel) {
        if (isClass.get()) {
            saveLocalVariable(variable, isClass, methodModel);
        } else {
            checkVariable(variable, methodModel);
        }
    }

    private void saveLocalVariable(String variable, AtomicBoolean isClass, MethodModel methodModel) {
        int lastIndex = methodModel.getLocalVariables().size() - SINGLE_LIST_SIZE;
        methodModel.getLocalVariables()
                .get(lastIndex)
                .setName(variable);

        isClass.set(Boolean.FALSE);
    }

    private void checkVariable(String variable, MethodModel methodModel) {
        if (isVariableUsed(variable, methodModel)) {
            saveGlobalVariable(variable, methodModel);
        }
    }

    private Boolean isVariableUsed(String variable, MethodModel methodModel) {
        return !isParameter(variable, methodModel) &&
                !isLocalVariable(variable, methodModel) &&
                !isGlobalVariable(variable, methodModel);
    }

    private Boolean isParameter(String variable, MethodModel methodModel) {
        return methodModel.getParameters()
                .stream()
                .anyMatch(propertyModel -> isContainsVariable(variable, propertyModel));
    }

    private Boolean isContainsVariable(String variable, PropertyModel propertyModel) {
        return propertyModel.getName().equals(variable);
    }

    private Boolean isLocalVariable(String variable, MethodModel methodModel) {
        return methodModel.getLocalVariables()
                .stream()
                .anyMatch(propertyModel -> isContainsVariable(variable, propertyModel));
    }

    private Boolean isGlobalVariable(String variable, MethodModel methodModel) {
        return methodModel.getGlobalVariables()
                .contains(variable);
    }

    private void saveGlobalVariable(String variable, MethodModel methodModel) {
        methodModel.getGlobalVariables()
                .add(variable);
    }
}
