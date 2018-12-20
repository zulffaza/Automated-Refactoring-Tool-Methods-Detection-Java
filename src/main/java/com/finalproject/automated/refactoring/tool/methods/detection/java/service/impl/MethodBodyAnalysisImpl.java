package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodBodyAnalysis;
import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Stack;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 12 December 2018
 */

@Service
public class MethodBodyAnalysisImpl implements MethodBodyAnalysis {

    private static final String OPEN_BRACES = "{";
    private static final String CLOSE_BRACES = "}";

    @Override
    public void analysis(@NonNull String content, @NonNull IndexModel indexModel,
                         @NonNull MethodModel methodModel) {
        Integer startBodyIndex = indexModel.getStart();

        String body = searchMethodBody(content, startBodyIndex, indexModel.getEnd());
        methodModel.setBody(body);
    }

    private String searchMethodBody(String content, Integer startBodyIndex, Integer endIndex) {
        Stack<Integer> stack = new Stack<>();
        Integer index;

        for (index = startBodyIndex; index < content.length(); index++) {
            String character = String.valueOf(content.charAt(index));

            switch (character) {
                case OPEN_BRACES:
                    stack.push(index);
                    break;
                case CLOSE_BRACES:
                    startBodyIndex = stack.pop();
                    break;
            }

            if (index > endIndex && stack.empty())
                break;
        }

        return content.substring(startBodyIndex + 1, index - 1);
    }
}
