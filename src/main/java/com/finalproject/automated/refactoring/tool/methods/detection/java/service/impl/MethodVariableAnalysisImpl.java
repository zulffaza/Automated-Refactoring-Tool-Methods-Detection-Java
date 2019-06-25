package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.model.SaveVariableVA;
import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodVariableAnalysis;
import com.finalproject.automated.refactoring.tool.model.BlockModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.PropertyModel;
import com.finalproject.automated.refactoring.tool.model.StatementModel;
import com.finalproject.automated.refactoring.tool.model.VariablePropertyModel;
import com.finalproject.automated.refactoring.tool.utils.service.VariableHelper;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 14 may 2019
 */

@Service
public class MethodVariableAnalysisImpl implements MethodVariableAnalysis {

    @Autowired
    private VariableHelper variableHelper;

    private static final String GLOBAL_VARIABLE_PREFIX = "this.";
    private static final String EMPTY_STRING = "";

    private static final Integer FIRST_INDEX = 0;

    @Override
    public void analysis(@NonNull MethodModel methodModel) {
        methodModel.getStatements()
                .forEach(statementModel -> readStatement(statementModel, methodModel));

        removeUnusedVariable(methodModel);
    }

    private void readStatement(StatementModel statementModel, MethodModel methodModel) {
        List<String> variables = variableHelper.readVariable(statementModel.getStatement());
        saveVariables(statementModel, variables, methodModel);

        if (statementModel instanceof BlockModel) {
            readBlockStatement(statementModel, methodModel);
        }
    }

    private void readBlockStatement(StatementModel statementModel, MethodModel methodModel) {
        ((BlockModel) statementModel).getStatements()
                .forEach(blockStatementModel -> readStatement(blockStatementModel, methodModel));
    }

    private void saveVariables(StatementModel statementModel, List<String> variables,
                               MethodModel methodModel) {
        SaveVariableVA saveVariableVA = SaveVariableVA.builder()
                .statementModel(statementModel)
                .methodModel(methodModel)
                .build();

        variables.stream()
                .filter(this::isNotOperators)
                .forEach(variable -> saveVariable(variable, saveVariableVA));
    }

    private Boolean isNotOperators(String variable) {
        return !variable.matches(VariableHelper.OPERATORS_CHARACTERS_REGEX);
    }

    private void saveVariable(String variable, SaveVariableVA saveVariableVA) {
        if (isPropertyType(variable)) {
            savePropertyType(variable, saveVariableVA);
        } else if (checkGlobalVariable(variable)) {
            saveGlobalVariableWithPreProcessing(variable, saveVariableVA.getMethodModel());
        } else {
            checkVariableDomain(variable, saveVariableVA);
        }
    }

    private Boolean isPropertyType(String variable) {
        return VariableHelper.PRIMITIVE_TYPES.contains(variable) ||
                variableHelper.isClassName(variable);
    }

    private void savePropertyType(String variable, SaveVariableVA saveVariableVA) {
        VariablePropertyModel variablePropertyModel = VariablePropertyModel.variablePropertyBuilder()
                .statementIndex(saveVariableVA.getStatementModel().getIndex())
                .build();

        variablePropertyModel.setType(variable);

        saveVariableVA.getMethodModel()
                .getLocalVariables()
                .add(variablePropertyModel);

        saveVariableVA.getIsClass()
                .set(Boolean.TRUE);
    }

    private Boolean checkGlobalVariable(String variable) {
        return variable.startsWith(GLOBAL_VARIABLE_PREFIX);
    }

    private void saveGlobalVariableWithPreProcessing(String variable, MethodModel methodModel) {
        variable = variable.replace(GLOBAL_VARIABLE_PREFIX, EMPTY_STRING);
        saveGlobalVariable(variable, methodModel);
    }

    private void saveGlobalVariable(String variable, MethodModel methodModel) {
        methodModel.getGlobalVariables()
                .add(variable);
    }

    private void checkVariableDomain(String variable, SaveVariableVA saveVariableVA) {
        if (saveVariableVA.getIsClass().get()) {
            saveLocalVariable(variable, saveVariableVA);
        } else {
            checkVariable(variable, saveVariableVA.getMethodModel());
        }
    }

    private void saveLocalVariable(String variable, SaveVariableVA saveVariableVA) {
        saveVariableVA.getMethodModel()
                .getLocalVariables()
                .stream()
                .filter(this::isNoNameLocalVariable)
                .forEach(propertyModel ->
                        saveLocalVariableName(propertyModel, variable));

        saveVariableVA.getIsClass()
                .set(Boolean.FALSE);
    }

    private Boolean isNoNameLocalVariable(PropertyModel propertyModel) {
        return propertyModel.getName() == null;
    }

    private void saveLocalVariableName(PropertyModel propertyModel, String variable) {
        propertyModel.setName(variable);
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
        String globalVariable = GLOBAL_VARIABLE_PREFIX + variable;

        return methodModel.getGlobalVariables().contains(variable) ||
                methodModel.getGlobalVariables().contains(globalVariable);
    }

    private void removeUnusedVariable(MethodModel methodModel) {
        List<VariablePropertyModel> localVariables = methodModel.getLocalVariables()
                .stream()
                .filter(variable -> isNotMethodParameter(methodModel.getParameters(), variable, methodModel))
                .collect(Collectors.toList());

        methodModel.setLocalVariables(localVariables);
    }

    private Boolean isNotMethodParameter(List<PropertyModel> parameters, PropertyModel variable,
                                         MethodModel methodModel) {
        List<String> equalsToParameters = parameters.stream()
                .filter(parameter -> isParameterNameEquals(parameter, variable))
                .map(PropertyModel::getName)
                .collect(Collectors.toList());

        methodModel.getGlobalVariables()
                .addAll(FIRST_INDEX, equalsToParameters);

        return equalsToParameters.isEmpty();
    }

    private Boolean isParameterNameEquals(PropertyModel parameter, PropertyModel variable) {
        return parameter.getName()
                .equals(variable.getName());
    }
}
