package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodVariableAnalysis;
import com.finalproject.automated.refactoring.tool.model.BlockModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.PropertyModel;
import com.finalproject.automated.refactoring.tool.model.StatementModel;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 14 may 2019
 */

@Service
public class MethodVariableAnalysisImpl implements MethodVariableAnalysis {

    @Autowired
    private VariableHelper variableHelper;

    private static final Integer SINGLE_LIST_SIZE = 1;

    @Override
    public void analysis(@NonNull MethodModel methodModel) {
        methodModel.getStatements()
                .forEach(statementModel -> readStatement(statementModel, methodModel));
    }

    private void readStatement(StatementModel statementModel, MethodModel methodModel) {
        List<String> variables = variableHelper.readVariable(statementModel.getStatement());
        saveVariables(variables, methodModel);

        if (statementModel instanceof BlockModel) {
            readBlockStatement(statementModel, methodModel);
        }
    }

    private void readBlockStatement(StatementModel statementModel, MethodModel methodModel) {
        ((BlockModel) statementModel).getStatements()
                .forEach(blockStatementModel -> readStatement(blockStatementModel, methodModel));
    }

    private void saveVariables(List<String> variables, MethodModel methodModel) {
        AtomicBoolean isClass = new AtomicBoolean();

        variables.stream()
                .filter(this::isNotOperators)
                .forEach(variable -> saveVariable(variable, isClass, methodModel));
    }

    private Boolean isNotOperators(String variable) {
        return !variable.matches(VariableHelper.OPERATORS_CHARACTERS_REGEX);
    }

    private void saveVariable(String variable, AtomicBoolean isClass, MethodModel methodModel) {
        if (isPropertyType(variable)) {
            savePropertyType(variable, isClass, methodModel);
        } else {
            checkVariableDomain(variable, isClass, methodModel);
        }
    }

    private Boolean isPropertyType(String variable) {
        return VariableHelper.PRIMITIVE_TYPES.contains(variable) ||
                variableHelper.isClassName(variable);
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
