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

import java.util.List;

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

    private static final Character COMMENTS_START_CHARACTER = '/';
    private static final Character MULTI_LINE_COMMENTS_START_CHARACTER = '*';
    private static final Character NEW_LINE = '\n';
    private static final Character OPEN_BRACES = '{';
    private static final Character CLOSED_BRACES = '}';
    private static final Character SEMICOLON = ';';

    private static final Integer SECOND_INDEX = 1;

    @Override
    public void analysis(MethodModel methodModel) {
        IsStatementVA isStatementVA = new IsStatementVA();
        SaveStatementVA saveStatementVA = createSaveStatementVA(methodModel);

        for (int index = 0; index < saveStatementVA.getBody().length(); index++) {
            saveStatementVA.setIterationIndex(index);
            checkStatement(saveStatementVA, isStatementVA);
        }
    }

    private SaveStatementVA createSaveStatementVA(MethodModel methodModel) {
        SaveStatementVA saveStatementVA = SaveStatementVA.builder()
                .body(methodModel.getBody())
                .index(0)
                .startStatementIndex(0)
                .endStatementIndex(0)
                .isOneLineComments(Boolean.FALSE)
                .isMultiLineComments(Boolean.FALSE)
                .build();

        saveStatementVA.getStatements()
                .push(methodModel.getStatements());

        return saveStatementVA;
    }

    private void checkStatement(SaveStatementVA saveStatementVA, IsStatementVA isStatementVA) {
        Character character = saveStatementVA.getBody()
                .charAt(saveStatementVA.getIterationIndex());
        saveStatementVA.setEndStatementIndex(saveStatementVA.getIterationIndex());

        if (isStatement(character, saveStatementVA, isStatementVA)) {
            saveStatement(character, saveStatementVA);
        } else if (isEndOfBlock(character, saveStatementVA, isStatementVA)) {
            changeBlock(saveStatementVA);
        }
    }

    private Boolean isStatement(Character character, SaveStatementVA saveStatementVA,
                                IsStatementVA isStatementVA) {
        Character nextCharacter = getNextCharacter(saveStatementVA);

        if (isComments(character, nextCharacter, isStatementVA)) {
            return flagComments(character, nextCharacter, saveStatementVA);
        } else if (isCloseComments(character, nextCharacter, isStatementVA)) {
            return removeFlagComments(saveStatementVA);
        } else {
            return checkIfIsStatement(character, saveStatementVA, isStatementVA);
        }
    }

    private Character getNextCharacter(SaveStatementVA saveStatementVA) {
        try {
            return saveStatementVA.getBody()
                    .charAt(saveStatementVA.getIterationIndex() + SECOND_INDEX);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean isComments(Character character, Character nextCharacter,
                               IsStatementVA isStatementVA) {
        return (isStatementVA.getStack().isEmpty() && !isStatementVA.getEscape().get()) &&
                (isOneLineCommentsOpening(character, nextCharacter) ||
                        isMultiLineCommentsOpening(character, nextCharacter));
    }

    private Boolean isOneLineCommentsOpening(Character character, Character nextCharacter) {
        return character.equals(COMMENTS_START_CHARACTER) &&
                nextCharacter != null &&
                nextCharacter.equals(COMMENTS_START_CHARACTER);
    }

    private Boolean isMultiLineCommentsOpening(Character character, Character nextCharacter) {
        return character.equals(COMMENTS_START_CHARACTER) &&
                nextCharacter != null &&
                nextCharacter.equals(MULTI_LINE_COMMENTS_START_CHARACTER);
    }

    private Boolean flagComments(Character character, Character nextCharacter,
                                 SaveStatementVA saveStatementVA) {
        saveStatementVA.setIsOneLineComments(isOneLineCommentsOpening(character, nextCharacter));
        saveStatementVA.setIsMultiLineComments(isMultiLineCommentsOpening(character, nextCharacter));

        return Boolean.FALSE;
    }

    private Boolean isCloseComments(Character character, Character nextCharacter,
                                    IsStatementVA isStatementVA) {
        return (isStatementVA.getStack().isEmpty() && !isStatementVA.getEscape().get()) &&
                (isOneLineCommentsClosing(character) ||
                        isMultiLineCommentsClosing(character, nextCharacter));
    }

    private Boolean isOneLineCommentsClosing(Character character) {
        return character.equals(NEW_LINE);
    }

    private Boolean isMultiLineCommentsClosing(Character character, Character nextCharacter) {
        return character.equals(MULTI_LINE_COMMENTS_START_CHARACTER) &&
                nextCharacter != null &&
                nextCharacter.equals(COMMENTS_START_CHARACTER);
    }

    private Boolean removeFlagComments(SaveStatementVA saveStatementVA) {
        if (saveStatementVA.getIsOneLineComments()) {
            saveStatementVA.setIsOneLineComments(Boolean.FALSE);
        }

        if (saveStatementVA.getIsMultiLineComments()) {
            saveStatementVA.setIsMultiLineComments(Boolean.FALSE);
        }

        return Boolean.FALSE;
    }

    private Boolean checkIfIsStatement(Character character, SaveStatementVA saveStatementVA,
                                       IsStatementVA isStatementVA) {
        if (!isInsideComments(saveStatementVA)) {
            return statementHelper.isStatement(character, isStatementVA);
        } else {
            return Boolean.FALSE;
        }
    }

    private Boolean isInsideComments(SaveStatementVA saveStatementVA) {
        return saveStatementVA.getIsOneLineComments() ||
                saveStatementVA.getIsMultiLineComments();
    }

    private void saveStatement(Character character, SaveStatementVA saveStatementVA) {
        String statement = createStatement(saveStatementVA);
        StatementModel statementModel = createStatementModel(character, statement, saveStatementVA);
        statementModel.setIndex(saveStatementVA.getIndex());

        saveStatementVA.getStatements()
                .peek()
                .add(statementModel);

        saveStatementVA.setIndex(saveStatementVA.getIndex() + ONE);
        saveNewBlock(statementModel, saveStatementVA);
    }

    private String createStatement(SaveStatementVA saveStatementVA) {
        int inclusiveEndIndex = saveStatementVA.getEndStatementIndex() + ONE;
        String statement = saveStatementVA.getBody()
                .substring(saveStatementVA.getStartStatementIndex(), inclusiveEndIndex)
                .trim();

        changeIntoRealStartIndex(statement, saveStatementVA);

        return statement;
    }

    private void changeIntoRealStartIndex(String statement, SaveStatementVA saveStatementVA) {
        int realStartIndex = saveStatementVA.getBody()
                .indexOf(statement, saveStatementVA.getStartStatementIndex());
        saveStatementVA.setStartStatementIndex(realStartIndex);
    }

    private StatementModel createStatementModel(Character character, String statement,
                                                SaveStatementVA saveStatementVA) {
        StatementModel statementModel = null;

        if (character.equals(SEMICOLON)) {
            statementModel = buildStatementModel(statement);
        }

        if (character.equals(OPEN_BRACES)) {
            statementModel = buildBlockModel(statement);
        }

        addIndexToStatementModel(statementModel, saveStatementVA);

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

    private void addIndexToStatementModel(StatementModel statementModel,
                                          SaveStatementVA saveStatementVA) {
        if (statementModel != null) {
            statementModel.setStartIndex(saveStatementVA.getStartStatementIndex());
            statementModel.setEndIndex(saveStatementVA.getEndStatementIndex());
        }

        saveStatementVA.setStartStatementIndex(saveStatementVA.getEndStatementIndex() + ONE);
    }

    private void saveNewBlock(StatementModel statementModel, SaveStatementVA saveStatementVA) {
        if (statementModel instanceof BlockModel) {
            saveStatementVA.getStatements()
                    .push(((BlockModel) statementModel).getStatements());
        }
    }

    private Boolean isEndOfBlock(Character character, SaveStatementVA saveStatementVA,
                                 IsStatementVA isStatementVA) {
        return !isInsideComments(saveStatementVA) &&
                !isStatementVA.getEscape().get() && isStatementVA.getStack().empty() &&
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
                .index(saveStatementVA.getIndex())
                .build();

        addIndexToStatementModel(statementModel, saveStatementVA);
        saveEndOfBlockStatement(statementModel, saveStatementVA);
    }

    private void saveEndOfBlockStatement(StatementModel statementModel, SaveStatementVA saveStatementVA) {
        List<StatementModel> lastBlock = saveStatementVA.getStatements()
                .peek();
        int lastBlockIndex = lastBlock.size() - ONE;

        ((BlockModel) lastBlock.get(lastBlockIndex))
                .setEndOfBlockStatement(statementModel);
    }
}
