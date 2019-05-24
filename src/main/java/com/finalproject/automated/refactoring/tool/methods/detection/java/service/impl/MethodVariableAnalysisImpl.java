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
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 14 may 2019
 */

@Service
public class MethodVariableAnalysisImpl implements MethodVariableAnalysis {

    private static final Character UNDERSCORE_CHARACTER = '_';

    private static final String WHITESPACE_REGEX = "\\s";
    private static final String SPACE_DELIMITER = " ";
    private static final String ESCAPE = "\\";
    private static final String DOUBLE_QUOTES = "\"";
    private static final String EMPTY_STRING = "";
    private static final String OPEN_PARENTHESES = "(";
    private static final String OPEN_PARENTHESES_REGEX = "\\" + OPEN_PARENTHESES;
    private static final String CLOSE_PARENTHESES = ")";
    private static final String CLOSE_PARENTHESES_REGEX = "\\" + CLOSE_PARENTHESES;
    private static final String DOUBLE_COLON = "::";
    private static final String UNUSED_CHARACTERS_REGEX = "(?:[,:;])+";
    private static final String POINT = ".";
    private static final String POINT_REGEX = "\\" + POINT;
    private static final String VARIABLE_NAME_REGEX = "^(?:[a-zA-Z_$])(?:[a-zA-Z0-9_$<>\\[\\].,\\s])*";
    private static final String LESS_THAN = "<";
    private static final String GREATER_THAN = ">";
    private static final String OPEN_SQUARE_BRACKETS = "[";
    private static final String CLOSE_SQUARE_BRACKETS = "]";

    private static final Integer FIRST_INDEX = 0;
    private static final Integer SECOND_INDEX = 1;
    private static final Integer SINGLE_LIST_SIZE = 1;
    private static final Integer NEXT_CHAR_INDEX_METHOD_REFERENCE = 2;

    private static final List<String> OPERATORS = Arrays.asList(
            "+", "-", "*", "/", "%",
            "=",
            "!", ">", "<",
            "&", "|", "^", "~"
    );

    private static final List<String> REGEX_SPECIAL_CHARACTERS = Arrays.asList(
            "+", "-", "*", ">", "<", "&", "|", "^"
    );

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
        List<String> variables = readVariable(statementModel.getStatement());
        saveVariables(variables, methodModel);

        if (statementModel instanceof BlockModel) {
            readBlockStatement(statementModel, methodModel);
        }
    }

    private void readBlockStatement(StatementModel statementModel, MethodModel methodModel) {
        ((BlockModel) statementModel).getStatements()
                .forEach(blockStatementModel -> readStatement(blockStatementModel, methodModel));
    }

    private List<String> readVariable(String statement) {
        List<String> variables = Arrays.asList(statement.split(WHITESPACE_REGEX));
        variables = new ArrayList<>(variables);

        return doReadVariable(variables);
    }

    private List<String> doReadVariable(List<String> variables) {
        variables = variables.stream()
                .flatMap(this::removeUnusedCharacter)
                .collect(Collectors.toList());

        normalizeString(variables);
        normalizeGenerics(variables);

        return variables.stream()
                .filter(this::isNotString)
                .flatMap(this::splitArrayVariables)
                .filter(this::isVariable)
                .collect(Collectors.toList());
    }

    private Stream<String> removeUnusedCharacter(String variable) {
        variable = variable.replaceAll(OPEN_PARENTHESES_REGEX, OPEN_PARENTHESES + SPACE_DELIMITER);
        variable = variable.replaceAll(CLOSE_PARENTHESES_REGEX, SPACE_DELIMITER + CLOSE_PARENTHESES + SPACE_DELIMITER);
        variable = changeMethodReferenceIntoMethodCall(variable).replace(DOUBLE_COLON, POINT);
        variable = variable.replaceAll(POINT_REGEX, SPACE_DELIMITER + POINT);
        variable = variable.replaceAll(UNUSED_CHARACTERS_REGEX, EMPTY_STRING);
        variable = normalizeOperators(variable);

        return splitMethodParameters(variable);
    }

    private String changeMethodReferenceIntoMethodCall(String variable) {
        Integer index = variable.indexOf(DOUBLE_COLON);

        while (index >= FIRST_INDEX) {
            variable = createMethodCallFromMethodReference(variable, index);
            index = variable.indexOf(DOUBLE_COLON, ++index);
        }

        return variable;
    }

    private String createMethodCallFromMethodReference(String variable, Integer index) {
        index += NEXT_CHAR_INDEX_METHOD_REFERENCE;

        while (isEndOfMethodReferenceFound(variable, index)) {
            index++;
        }

        return variable.substring(FIRST_INDEX, index) + OPEN_PARENTHESES +
                SPACE_DELIMITER + variable.substring(index);
    }

    private Boolean isEndOfMethodReferenceFound(String variable, Integer index) {
        return index < variable.length() || variable.startsWith(SPACE_DELIMITER, index);
    }

    private String normalizeOperators(String variable) {
        for (Integer index = FIRST_INDEX; index < OPERATORS.size(); index++) {
            variable = normalizeOperator(variable, OPERATORS.get(index));
        }

        return variable;
    }

    private String normalizeOperator(String variable, String operator) {
        String delimiter = createDelimiter(operator);
        return variable.replaceAll(delimiter, SPACE_DELIMITER + operator + SPACE_DELIMITER);
    }

    private String createDelimiter(String operator) {
        if (REGEX_SPECIAL_CHARACTERS.contains(operator)) {
            operator = "\\" + operator;
        }

        return operator;
    }

    private Stream<String> splitMethodParameters(String variable) {
        List<String> split = Arrays.asList(variable.split(WHITESPACE_REGEX));
        split = createNewListIfSplitEmpty(split, variable);

        removeCastingType(split);
        removeStaticClass(split);

        return split.stream();
    }

    private List<String> createNewListIfSplitEmpty(List<String> split, String variable) {
        if (split.isEmpty()) {
            split = Collections.singletonList(variable);
        }

        split = new ArrayList<>(split);
        split.removeIf(String::isEmpty);

        return split;
    }

    private void removeCastingType(List<String> split) {
        List<Integer> removeIndex = new ArrayList<>();

        for (Integer index = FIRST_INDEX; index < (split.size() - SECOND_INDEX); index++) {
            if (isRemoveIndex(split, index, split.get(index + SECOND_INDEX))) {
                removeIndex.add(index);
            }
        }

        for (Integer index = removeIndex.size() - SECOND_INDEX; index >= FIRST_INDEX; index--) {
            split.remove(removeIndex.get(index).intValue());
        }
    }

    private Boolean isRemoveIndex(List<String> split, Integer index, String nextVariable) {
        return isContainsKeywordsOrClass(split, index) && nextVariable.equals(CLOSE_PARENTHESES);
    }

    private void removeStaticClass(List<String> split) {
        Integer maxSize = split.size() - SECOND_INDEX;
        AtomicBoolean isCallMethod = new AtomicBoolean();

        for (Integer index = FIRST_INDEX; index < maxSize; index++) {
            String variable = split.get(index);
            String nextVariable = split.get(index + SECOND_INDEX);

            if (isStaticMethodCalls(variable, nextVariable)) {
                flagToIgnore(split, index);
            }

            checkIfCallMethod(variable, isCallMethod);
        }

        if (isNeedNormalization(split, isCallMethod)) {
            normalizeCalledMethod(split);
        }
    }

    private Boolean isStaticMethodCalls(String variable, String nextVariable) {
        return isClassName(variable) && nextVariable.contains(POINT);
    }

    private void flagToIgnore(List<String> split, Integer index) {
        split.set(index, POINT + split.get(index));
    }

    private void checkIfCallMethod(String variable, AtomicBoolean isCallMethod) {
        if (variable.contains(OPEN_PARENTHESES)) {
            isCallMethod.set(Boolean.TRUE);
        }
    }

    private Boolean isNeedNormalization(List<String> split, AtomicBoolean isCallMethod) {
        boolean isCallField = split.stream()
                .anyMatch(this::isCalledField);

        return isCallField && !isCallMethod.get();
    }

    private Boolean isCalledField(String variable) {
        return variable.contains(POINT);
    }

    private void normalizeCalledMethod(List<String> split) {
        Integer endIndex;

        for (endIndex = SECOND_INDEX; endIndex < split.size(); endIndex++) {
            String variable = split.get(endIndex);

            if (!variable.startsWith(POINT)) {
                break;
            }
        }

        List<String> removeElements = split.subList(SECOND_INDEX, endIndex);

        split.set(FIRST_INDEX, split.get(FIRST_INDEX).replaceAll(POINT_REGEX, EMPTY_STRING));
        split.set(FIRST_INDEX, split.get(FIRST_INDEX) + String.join(EMPTY_STRING, removeElements));
        split.removeAll(removeElements);
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

    private void normalizeGenerics(List<String> variables) {
        Integer maxSize = variables.size() - SECOND_INDEX;

        List<Integer> mergeIndex = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        Boolean isGenerics = Boolean.FALSE;

        for (Integer index = FIRST_INDEX; index < maxSize; index++) {
            String variable = variables.get(index);
            String nextVariable = variables.get(index + SECOND_INDEX);

            if (variable.contains(DOUBLE_QUOTES)) {
                continue;
            }

            if (!isGenerics && isOpeningGenerics(variable, nextVariable, stack)) {
                isGenerics = Boolean.TRUE;
                mergeIndex.add(index);
            }

            if (variable.equals(LESS_THAN)) {
                stack.push(LESS_THAN);
            }

            if (variable.equals(GREATER_THAN)) {
                if (!stack.isEmpty()) {
                    stack.pop();
                }

                if (stack.isEmpty() && isGenerics) {
                    isGenerics = Boolean.FALSE;
                    Integer savedIndex = index;

                    if (nextVariable.equals(OPEN_PARENTHESES)) {
                        savedIndex++;
                    }

                    mergeIndex.add(savedIndex);
                }
            }
        }

        beautifyGenerics(variables, mergeIndex);
        MergeListHelper.mergeListOfString(variables, mergeIndex, EMPTY_STRING);
    }

    private Boolean isOpeningGenerics(String variable, String nextVariable, Stack stack) {
        return isClassName(variable) && nextVariable.equals(LESS_THAN) && stack.isEmpty();
    }

    private void beautifyGenerics(List<String> variables, List<Integer> mergeIndex) {
        Integer maxSize = mergeIndex.size() - SECOND_INDEX;

        for (Integer index = FIRST_INDEX; index < maxSize; index++) {
            Integer startPoint = mergeIndex.get(index);
            Integer endPoint = mergeIndex.get(++index);

            beautifyGenericsForRange(variables, startPoint, endPoint);
        }
    }

    private void beautifyGenericsForRange(List<String> variables, Integer startPoint, Integer endPoint) {
        if (variables.get(endPoint).equals(OPEN_PARENTHESES)) {
            endPoint--;
        }

        for (Integer index = startPoint; index < endPoint; index++) {
            String variable = variables.get(index);
            String nextVariable = variables.get(index + SECOND_INDEX);

            if (isNeedCommaSeparator(variable, nextVariable)) {
                variables.set(index, variable + ", ");
            }
        }
    }

    private Boolean isNeedCommaSeparator(String variable, String nextVariable) {
        return (isClassName(variable) || variable.equals(GREATER_THAN)) &&
                !nextVariable.equals(LESS_THAN) && !nextVariable.equals(GREATER_THAN);
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

    private Boolean isNotString(String variable) {
        return !variable.contains(DOUBLE_QUOTES);
    }

    private Stream<String> splitArrayVariables(String variable) {
        List<String> split = Arrays.asList(variable.split(Pattern.quote(OPEN_SQUARE_BRACKETS)));
        split = createNewListIfSplitEmpty(split, variable);

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

        if (isContainsKeywordsOrClass(split, FIRST_INDEX)) {
            split.remove(FIRST_INDEX.intValue());
        }
    }

    private String removeCloseSquareBrackets(String variable) {
        return variable.replace(CLOSE_SQUARE_BRACKETS, EMPTY_STRING);
    }

    private Boolean isContainsKeywordsOrClass(List<String> split, Integer index) {
        String variable = split.get(index);

        return split.size() > SINGLE_LIST_SIZE &&
                (KEYWORDS.contains(variable) || PRIMITIVE_TYPES.contains(variable) || isClassName(variable));
    }

    private Boolean isVariable(String variable) {
        return (variable.matches(VARIABLE_NAME_REGEX) && !KEYWORDS.contains(variable)) ||
                OPERATORS.contains(variable);
    }

    private void saveVariables(List<String> variables, MethodModel methodModel) {
        AtomicBoolean isClass = new AtomicBoolean();

        variables.stream()
                .filter(this::isNotOperators)
                .forEach(variable -> saveVariable(variable, isClass, methodModel));
    }

    private Boolean isNotOperators(String variable) {
        return !OPERATORS.contains(variable);
    }

    private void saveVariable(String variable, AtomicBoolean isClass, MethodModel methodModel) {
        if (isPropertyType(variable)) {
            savePropertyType(variable, isClass, methodModel);
        } else {
            checkVariableDomain(variable, isClass, methodModel);
        }
    }

    private Boolean isPropertyType(String variable) {
        return PRIMITIVE_TYPES.contains(variable) || isClassName(variable);
    }

    private Boolean isClassName(String variable) {
        return Character.isUpperCase(variable.charAt(FIRST_INDEX)) && !isConst(variable);
    }

    private Boolean isConst(String variable) {
        Integer index;

        for (index = FIRST_INDEX; index < variable.length(); index++) {
            if (!isComplete(variable.charAt(index))) {
                break;
            }
        }

        return isSuccess(variable, index);
    }

    private Boolean isComplete(Character character) {
        return Character.isUpperCase(character) || character.equals(UNDERSCORE_CHARACTER);
    }

    private Boolean isSuccess(String variable, Integer index) {
        return index == variable.length();
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
