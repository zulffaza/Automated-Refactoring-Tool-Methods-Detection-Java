package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.helper.MergeListHelper;
import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodVariableAnalysis;
import com.finalproject.automated.refactoring.tool.model.BlockModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
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

    // Get variable used inside array index

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
    private static final String CLOSE_SQUARE_BRACKETS = "]";

    private static final Integer FIRST_INDEX = 0;
    private static final Integer SECOND_INDEX = 1;

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
        doReadVariable(variables);
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

        for (Integer newIndex = FIRST_INDEX; newIndex < variable.length(); newIndex++) {
            String character = variable.substring(newIndex, newIndex + SECOND_INDEX);

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

    private void doReadVariable(List<String> variables) {
        variables.stream()
                .filter(this::isNotString)
                .flatMap(this::removeUnusedCharacter)
                .map(this::removeCalledFields)
                .filter(this::isVariable)
                .forEach(System.out::println);
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
        return split.size() > 1 && KEYWORDS.contains(split.get(FIRST_INDEX));
    }

    private Boolean isVariable(String variable) {
        return variable.matches(VARIABLE_NAME_REGEX) &&
                !KEYWORDS.contains(variable);
    }
}
