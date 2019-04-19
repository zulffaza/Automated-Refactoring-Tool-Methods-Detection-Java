package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.model.SaveStatementVA;
import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodStatementAnalysis;
import com.finalproject.automated.refactoring.tool.model.BlockModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.StatementModel;
import com.finalproject.automated.refactoring.tool.utils.model.request.IsStatementVA;
import com.finalproject.automated.refactoring.tool.utils.service.StatementHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 18 April 2019
 */

@Service
public class MethodStatementAnalysisImpl implements MethodStatementAnalysis {

    @Autowired
    private StatementHelper statementHelper;

    private static final Integer ONE = 1;

    private static final Character OPEN_BRACES = '{';
    private static final Character CLOSED_BRACES = '}';
    private static final Character SEMICOLON = ';';

    @Override
    public void analysis(MethodModel methodModel) {
        IsStatementVA isStatementVA = new IsStatementVA();
        SaveStatementVA saveStatementVA = createSaveStatementVA(methodModel);

        for (int index = 0; index < saveStatementVA.getBody().length(); index++) {
            checkStatement(saveStatementVA, index, isStatementVA);
        }
    }

    private SaveStatementVA createSaveStatementVA(MethodModel methodModel) {
        SaveStatementVA saveStatementVA = SaveStatementVA.builder()
                .body(methodModel.getBody())
                .startStatementIndex(0)
                .endStatementIndex(0)
                .build();

        saveStatementVA.getStatements()
                .push(methodModel.getStatements());

        return saveStatementVA;
    }

    private void checkStatement(SaveStatementVA saveStatementVA,
                                Integer index, IsStatementVA isStatementVA) {
        Character character = saveStatementVA.getBody().charAt(index);
        saveStatementVA.setEndStatementIndex(index);

        if (statementHelper.isStatement(character, isStatementVA)) {
            saveStatement(character, saveStatementVA);
        } else if (isEndOfBlock(character, isStatementVA)) {
            changeBlock(saveStatementVA);
        }
    }

    private void saveStatement(Character character, SaveStatementVA saveStatementVA) {
        String statement = createStatement(saveStatementVA);
        StatementModel statementModel = createStatementModel(character, statement);

        saveStatementVA.getStatements()
                .peek()
                .add(statementModel);

        saveNewBlock(statementModel, saveStatementVA);
    }

    private String createStatement(SaveStatementVA saveStatementVA) {
        Integer endIndex = saveStatementVA.getEndStatementIndex() + ONE;
        String statement = saveStatementVA.getBody()
                .substring(saveStatementVA.getStartStatementIndex(), endIndex)
                .trim();
        saveStatementVA.setStartStatementIndex(endIndex);

        return statement;
    }

    private StatementModel createStatementModel(Character character, String statement) {
        StatementModel statementModel = null;

        if (character.equals(SEMICOLON)) {
            statementModel = buildStatementModel(statement);
        }

        if (character.equals(OPEN_BRACES)) {
            statementModel = buildBlockModel(statement);
        }

        return statementModel;
    }

    private StatementModel buildStatementModel(String statement) {
        return StatementModel.statementBuilder()
                .statement(statement)
                .build();
    }

    private BlockModel buildBlockModel(String statement) {
        BlockModel blockModel = BlockModel.blockBuilder()
                .build();
        blockModel.setStatement(statement);

        return blockModel;
    }

    private void saveNewBlock(StatementModel statementModel, SaveStatementVA saveStatementVA) {
        if (statementModel instanceof BlockModel) {
            saveStatementVA.getStatements()
                    .push(((BlockModel) statementModel).getStatements());
        }
    }

    private Boolean isEndOfBlock(Character character, IsStatementVA isStatementVA) {
        return !isStatementVA.getEscape().get() && isStatementVA.getStack().empty() &&
                character.equals(CLOSED_BRACES);
    }

    private void changeBlock(SaveStatementVA saveStatementVA) {
        saveStatementVA.getStatements().pop();
        saveClosedBraces(saveStatementVA);
    }

    private void saveClosedBraces(SaveStatementVA saveStatementVA) {
        String statement = createStatement(saveStatementVA);
        StatementModel statementModel = StatementModel.statementBuilder()
                .statement(statement)
                .build();

        saveStatementVA.getStatements()
                .peek()
                .add(statementModel);
    }
}
