package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.files.detection.model.FileModel;
import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodAttributesAnalysis;
import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.PropertyModel;
import com.finalproject.automated.refactoring.tool.utils.service.MergeListHelper;
import com.finalproject.automated.refactoring.tool.utils.service.VariableHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 12 December 2018
 */

@Service
public class MethodAttributesAnalysisImpl implements MethodAttributesAnalysis {

    @Autowired
    private MergeListHelper mergeListHelper;

    private static final String OPEN_PARENTHESES = "(";
    private static final String CLOSE_PARENTHESES = ")";
    private static final String CLASS_REGEX = "(?:class)+(?:\\s)+(?:([A-Z_$])[a-zA-Z0-9_$<>,\\s]+)";
    private static final String ANONYMOUS_CLASS_REGEX = "(?:new)+(?:\\s)+(?:([A-Z_$])[a-zA-Z0-9_$<>,\\s]+)+(?:(?:[\\[\\]])|((?:\\()+(?:[@\\w\\[\\]<>\\(\\)=\".,\\s])*(?:\\))))+(\\{)+";
    private static final String ONE_LINE_COMMENT_REGEX = "^(?://)+(?:[ \\t\\S])*";
    private static final String MULTI_LINE_COMMENT_REGEX = "^(?:/\\*)+(?:[\\s\\S])*(?:\\*/)+";
    private static final String ERROR_MULTI_LINE_COMMENT_REGEX = "^(?:[\\s\\S])*(?:\\*/)+";
    private static final String EMPTY_STRING = "";
    private static final String OPEN_BRACES = "{";
    private static final String NEW_KEYWORD = "new";
    private static final String OPEN_PARENTHESES_DELIMITER = "\\" + OPEN_PARENTHESES;
    private static final String CLOSE_PARENTHESES_DELIMITER = "\\" + CLOSE_PARENTHESES;
    private static final String AT = "@";
    private static final String LESS_THAN = "<";
    private static final String GREATER_THAN = ">";
    private static final String QOMMA_DELIMITER = ",";
    private static final String SPACE_DELIMITER = " ";
    private static final String WHITESPACE_DELIMITER = "(?:\\s)+";
    private static final String NON_WORDS_DELIMITER = "(?:\\W)+";
    private static final String ANONYMOUS_CLASS_EXCEPTION_MESSAGE = "Is it a anonymous class...";
    private static final String NON_WORDS_EXCEPTION_MESSAGE = "Method name contains non words character...";

    private static final Integer FIRST_INDEX = 0;
    private static final Integer SECOND_INDEX = 1;
    private static final Integer NORMAL_SIZE = 2;
    private static final Integer ONE_RESERVED_WORDS = 1;
    private static final Integer TWO_RESERVED_WORDS = 2;

    @Override
    public void analysis(FileModel fileModel, IndexModel indexModel, MethodModel methodModel) {
        String methodDeclarations = fileModel.getContent()
                .substring(indexModel.getStart(), indexModel.getEnd()).trim();
        methodDeclarations = removeMethodComments(methodDeclarations);

        List<String> splitByOpenParentheses = new ArrayList<>(
                Arrays.asList(methodDeclarations.split(OPEN_PARENTHESES_DELIMITER)));
        normalizeAttributes(splitByOpenParentheses, OPEN_PARENTHESES);

        List<String> splitByCloseParentheses = new ArrayList<>(
                Arrays.asList(splitByOpenParentheses.get(SECOND_INDEX).split(CLOSE_PARENTHESES_DELIMITER)));
        normalizeAttributes(splitByCloseParentheses, CLOSE_PARENTHESES);

        searchKeywords(fileModel, splitByOpenParentheses.get(FIRST_INDEX), methodModel);
        searchParameters(splitByCloseParentheses.get(FIRST_INDEX), methodModel);
        searchExceptions(splitByCloseParentheses.get(SECOND_INDEX), methodModel);
    }

    private String removeMethodComments(String methodDeclarations) {
        methodDeclarations = removeWithEmptyString(ONE_LINE_COMMENT_REGEX, methodDeclarations);
        methodDeclarations = removeWithEmptyString(MULTI_LINE_COMMENT_REGEX, methodDeclarations);
        methodDeclarations = removeWithEmptyString(ERROR_MULTI_LINE_COMMENT_REGEX, methodDeclarations);

        return methodDeclarations;
    }

    private String removeWithEmptyString(String regex, String input) {
        Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE)
                .matcher(Matcher.quoteReplacement(input));

        if (matcher.find()) {
            return matcher
                    .replaceAll(EMPTY_STRING)
                    .trim();
        } else {
            return input;
        }
    }

    private void normalizeAttributes(List<String> words, String joinDelimiter) {
        if (!isNormal(words))
            doNormalizeAttributes(words, joinDelimiter);
    }

    private void doNormalizeAttributes(List<String> words, String joinDelimiter) {
        Integer splitIndex = searchSplitIndex(words);
        splitIndex = normalizeIndex(words, splitIndex);

        joinList(words, joinDelimiter, splitIndex);
    }

    private Boolean isNormal(List<String> words) {
        return words.size() == NORMAL_SIZE;
    }

    private Integer searchSplitIndex(List<String> words) {
        Integer index;

        for (index = FIRST_INDEX; index < words.size(); index++) {
            String firstChar = words.get(index).substring(FIRST_INDEX, SECOND_INDEX);

            if (isSplitIndexFound(firstChar, index))
                break;
        }

        return index;
    }

    private Boolean isSplitIndexFound(String firstChar, Integer index) {
        boolean isAt = firstChar.equals(AT);
        boolean isFirst = index.equals(FIRST_INDEX);

        return ((isFirst && !isAt) || (!isFirst && isAt));
    }

    private Integer normalizeIndex(List<String> words, Integer index) {
        if (index.equals(words.size()))
            return --index;

        return index;
    }

    private void joinList(List<String> words, String delimiter, Integer limitIndex) {
        List<String> keywords = words.subList(FIRST_INDEX, limitIndex);
        List<String> parameters = words.subList(limitIndex, words.size());

        String keyword = String.join(delimiter, keywords);
        String parameter = String.join(delimiter, parameters);

        words.clear();
        words.add(keyword);
        words.add(parameter);
    }

    private void searchKeywords(FileModel fileModel, String keywords, MethodModel methodModel)
            throws IndexOutOfBoundsException, IllegalArgumentException {
        keywords = keywords.trim();

        List<String> words = Arrays.stream(keywords.split(WHITESPACE_DELIMITER))
                .map(String::trim)
                .collect(Collectors.toList());

        normalizeKeywords(words, SPACE_DELIMITER);
        buildKeywords(words, fileModel, methodModel);
    }

    private void normalizeKeywords(List<String> words, String delimiter) {
        List<Integer> mergeIndex = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (Integer index = FIRST_INDEX; index < words.size(); index++) {
            String word = words.get(index);

            if (isMergePoint(word, stack))
                fillMergeIndex(word, index, mergeIndex);
        }

        mergeListHelper.mergeListOfString(words, mergeIndex, delimiter);
    }

    private Boolean isMergePoint(String word, Stack<String> stack) {
        Boolean saveIndex = Boolean.FALSE;
        boolean isEmpty = stack.isEmpty();

        for (Integer newIndex = FIRST_INDEX; newIndex < word.length(); newIndex++) {
            String character = word.substring(newIndex, newIndex + SECOND_INDEX);

            if (isContainOpenParentheses(character)) {
                stack.push(character);
                saveIndex = Boolean.TRUE;
            }

            if (!stack.isEmpty() && isContainCloseParentheses(character, stack)) {
                stack.pop();
                saveIndex = !isEmpty && stack.isEmpty();
            }
        }

        return saveIndex;
    }

    private void fillMergeIndex(String word, Integer index, List<Integer> mergeIndex) {
        String firstCharacter = word.substring(FIRST_INDEX, SECOND_INDEX);

        if (isContainOpenParentheses(firstCharacter))
            index -= SECOND_INDEX;

        mergeIndex.add(index);
    }

    private Boolean isContainOpenParentheses(String character) {
        return character.equals(OPEN_PARENTHESES) || character.equals(LESS_THAN);
    }

    private Boolean isContainCloseParentheses(String character, Stack<String> stack) {
        Boolean result = Boolean.FALSE;

        switch (stack.peek()) {
            case OPEN_PARENTHESES:
                result = character.equals(CLOSE_PARENTHESES);
                break;
            case LESS_THAN:
                result = character.equals(GREATER_THAN);
                break;
        }

        return result;
    }

    private void buildKeywords(List<String> words, FileModel fileModel,
                               MethodModel methodModel)
            throws IndexOutOfBoundsException, IllegalArgumentException {
        Boolean isConstructor = isConstructor(words, fileModel);

        Integer numOfReservedWords = getNumOfReservedWords(isConstructor);
        Integer size = words.size();
        Integer maxKeywords = size - numOfReservedWords;

        saveKeywords(words, maxKeywords, methodModel);
        numOfReservedWords--;

        if (!isConstructor) {
            setReturnType(methodModel, maxKeywords, words);
        }

        setName(words.get(maxKeywords + numOfReservedWords), methodModel);
    }

    private Boolean isConstructor(List<String> words, FileModel fileModel) {
        String content = fileModel.getContent();
        List<String> allClass = getAllClassNameByRegex(content, CLASS_REGEX);
        List<String> allAnonymousClass = getAllClassNameByRegex(content, ANONYMOUS_CLASS_REGEX);
        List<String> classes = Stream.concat(allClass.stream(), allAnonymousClass.stream())
                .distinct()
                .collect(Collectors.toList());

        return isMethodConstructor(words, classes);
    }

    private List<String> getAllClassNameByRegex(String content, String regex) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex)
                .matcher(content);

        while (matcher.find()) {
            searchClassName(matches, matcher);
        }

        return matches;
    }

    private void searchClassName(List<String> matches, Matcher matcher) {
        List<String> groups = new ArrayList<>(Arrays.asList(
                matcher.group().split(SPACE_DELIMITER)));
        normalizeKeywords(groups, SPACE_DELIMITER);

        if (groups.size() >= NORMAL_SIZE) {
            matches.add(groups.get(SECOND_INDEX).trim());
        }
    }

    private Boolean isMethodConstructor(List<String> words, List<String> classes) {
        return words.stream()
                .anyMatch(classes::contains);
    }

    private Integer getNumOfReservedWords(Boolean isConstructor) {
        if (isConstructor)
            return ONE_RESERVED_WORDS;
        else
            return TWO_RESERVED_WORDS;
    }

    private void saveKeywords(List<String> words, Integer numOfKeywords,
                              MethodModel methodModel) throws IllegalArgumentException {
        checkNewKeyword(words);

        words.stream()
                .limit(numOfKeywords)
                .forEach(methodModel.getKeywords()::add);
    }

    private void checkNewKeyword(List<String> words) throws IllegalArgumentException {
        if (words.contains(NEW_KEYWORD)) {
            throw new IllegalArgumentException(ANONYMOUS_CLASS_EXCEPTION_MESSAGE);
        }
    }

    private void setReturnType(MethodModel methodModel, Integer maxKeywords,
                               List<String> words) {
        String returnType = words.get(maxKeywords);
        methodModel.setReturnType(returnType);
    }

    private void setName(String name, MethodModel methodModel) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile(NON_WORDS_DELIMITER);
        Matcher matcher = pattern.matcher(name);

        if (isNotMethodName(name, matcher)) {
            throw new IllegalArgumentException(NON_WORDS_EXCEPTION_MESSAGE);
        }

        methodModel.setName(name);
    }

    private Boolean isNotMethodName(String name, Matcher matcher) throws IllegalArgumentException {
        return VariableHelper.KEYWORDS.contains(name) || matcher.find();
    }

    private void searchParameters(String parameters, MethodModel methodModel) {
        parameters = parameters.trim();

        if (!parameters.isEmpty()) {
            List<String> words = Arrays.stream(parameters.split(QOMMA_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toList());

            normalizeKeywords(words, QOMMA_DELIMITER + SPACE_DELIMITER);

            words.stream()
                    .map(this::splitList)
                    .map(this::createPropertyModel)
                    .forEach(methodModel.getParameters()::add);
        }
    }

    private List<String> splitList(String parameter) {
        List<String> words = Arrays.stream(parameter.split(WHITESPACE_DELIMITER))
                .map(String::trim)
                .collect(Collectors.toList());

        normalizeKeywords(words, SPACE_DELIMITER);

        return words;
    }

    private PropertyModel createPropertyModel(List<String> splittedParameters) {
        String name = splittedParameters.remove(splittedParameters.size() - SECOND_INDEX);
        String type = splittedParameters.remove(splittedParameters.size() - SECOND_INDEX);

        return PropertyModel.builder()
                .keywords(splittedParameters)
                .type(type)
                .name(name)
                .build();
    }

    private void searchExceptions(String exceptions, MethodModel methodModel) {
        exceptions = exceptions.replace(OPEN_BRACES, EMPTY_STRING).trim();

        if (!exceptions.isEmpty()) {
            List<String> words = Arrays.asList(exceptions.split(QOMMA_DELIMITER));
            words.set(FIRST_INDEX, words.get(FIRST_INDEX).split(WHITESPACE_DELIMITER)[SECOND_INDEX]);

            words.stream()
                    .map(String::trim)
                    .forEach(methodModel.getExceptions()::add);
        }
    }
}
