package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.service.StartIndexAnalysis;
import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Stack;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 9 December 2018
 */

@Service
public class StartIndexAnalysisImpl implements StartIndexAnalysis {

    private static final String COMMENTS_START = "/";
    private static final String MULTI_LINE_COMMENTS_START = "*";
    private static final String OPEN_PARENTHESES = "(";
    private static final String CLOSE_PARENTHESES = ")";
    private static final String OPEN_BRACES = "{";
    private static final String CLOSE_BRACES = "}";
    private static final String SEMICOLON = ";";

    private static final Integer NEXT_INDEX = 1;

    @Override
    public void analysis(@NonNull String content, @NonNull IndexModel indexModel) {
        Integer index = indexModel.getStart();
        Stack<String> stack = new Stack<>();

        while (!isValid(content, index, stack)) {
            index = reduceIndex(index);
        }

        indexModel.setStart(raiseIndex(index));
    }

    private Boolean isValid(String content, Integer index, Stack<String> stack) {
        String character = nextCharacter(content, index);
        fillStack(stack, character);

        if (isComments(content, index, character)) {
            return Boolean.TRUE;
        } else {
            return validate(stack, character);
        }
    }

    private String nextCharacter(String content, Integer index) {
        return content.substring(index, index + NEXT_INDEX);
    }

    private void fillStack(Stack<String> stack, String character) {
        switch (character) {
            case CLOSE_PARENTHESES:
                stack.push(character);
                break;
            case OPEN_PARENTHESES:
                stack.pop();
                break;
        }
    }

    private Boolean isComments(String content, Integer index, String character) {
        String nextCharacter = nextCharacter(content, index + NEXT_INDEX);

        if (character.equals(MULTI_LINE_COMMENTS_START)) {
            return Boolean.TRUE;
        } else {
            return isOneLineComments(character, nextCharacter);
        }
    }

    private Boolean isOneLineComments(String character, String nextCharacter) {
        return character.equals(COMMENTS_START) &&
                nextCharacter.equals(COMMENTS_START);
    }

    private Boolean validate(Stack<String> stack, String character) {
        if (!stack.empty()) {
            return Boolean.FALSE;
        } else {
            return isEOS(character);
        }
    }

    private Boolean isEOS(String character) {
        return character.equals(SEMICOLON) || character.equals(CLOSE_BRACES) ||
                character.equals(OPEN_BRACES);
    }

    private Integer reduceIndex(Integer index) {
        return index - NEXT_INDEX;
    }

    private Integer raiseIndex(Integer index) {
        return index + NEXT_INDEX;
    }
}
