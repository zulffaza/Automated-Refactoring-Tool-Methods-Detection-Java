package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodVariableAnalysis;
import com.finalproject.automated.refactoring.tool.model.BlockModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.StatementModel;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 14 may 2019
 */

@Service
public class MethodVariableAnalysisImpl implements MethodVariableAnalysis {

    @Override
    public void analysis(@NonNull MethodModel methodModel) {
        methodModel.getStatements()
                .forEach(this::readStatement);
    }

    private void readStatement(StatementModel statementModel) {
        readVariable(statementModel.getStatement());

        if (statementModel instanceof BlockModel) {
            ((BlockModel) statementModel).getStatements()
                    .forEach(this::readStatement);
        }
    }

    private void readVariable(String statement) {
        List<String> splitStatement = Arrays.asList(statement.split("\\s"));
        List<String> variables = new ArrayList<>(splitStatement);

        variables.stream()
                .map(this::removeUnusedCharacter2)
                .flatMap(this::removeUnusedCharacter)
                .filter(this::isVariable)
                .forEach(System.out::println);

        System.out.println();
    }

    private Stream<String> removeUnusedCharacter(String variable) {
        String[] split = variable.split("\\.");

        if (split.length == 0)
            split = new String[]{variable};

        return Arrays.stream(split);
    }

    private String removeUnusedCharacter2(String variable) {
        if (variable.contains(";"))
            variable = variable.replace(";", ".");

        if (variable.contains("("))
            variable = variable.replace("(", ".");

        if (variable.contains(")"))
            variable = variable.replace(")", ".");

        return variable;
    }

    private Boolean isVariable(String variable) {
        return variable.matches("(?:[a-zA-Z0-9_$<>])*") || variable.contains("=");
    }
}
