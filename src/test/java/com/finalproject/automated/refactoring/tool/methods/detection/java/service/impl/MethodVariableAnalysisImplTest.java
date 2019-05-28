package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodVariableAnalysis;
import com.finalproject.automated.refactoring.tool.model.BlockModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.PropertyModel;
import com.finalproject.automated.refactoring.tool.model.StatementModel;
import com.finalproject.automated.refactoring.tool.model.VariablePropertyModel;
import com.finalproject.automated.refactoring.tool.utils.service.VariableHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 14 May 2019
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class MethodVariableAnalysisImplTest {

    @Autowired
    private MethodVariableAnalysis methodVariableAnalysis;

    @MockBean
    private VariableHelper variableHelper;

    private static final String FIRST_STATEMENT = "try {";
    private static final String FIRST_BLOCK_STATEMENT = "return user + \"-\" + this.name + extension;";
    private static final String FIRST_BLOCK_END_STATEMENT = "}";
    private static final String SECOND_STATEMENT = "catch (NullPointerException e) {";
    private static final String SECOND_BLOCK_STATEMENT = "return null;";
    private static final String SECOND_BLOCK_END_STATEMENT = "}";
    private static final String FIRST_READ_VARIABLE = "user";
    private static final String SECOND_READ_VARIABLE_CLEAN = "name";
    private static final String SECOND_READ_VARIABLE = "this." + SECOND_READ_VARIABLE_CLEAN;
    private static final String THIRD_READ_VARIABLE = "extension";
    private static final String FOURTH_READ_VARIABLE = "NullPointerException";
    private static final String FIFTH_READ_VARIABLE = "e";

    private MethodModel methodModel;

    @Before
    public void setUp() {
        methodModel = MethodModel.builder()
                .parameters(Collections.singletonList(PropertyModel.builder()
                        .type("User")
                        .name("user")
                        .build()))
                .statements(createExpectedStatements())
                .build();

        mockingReadVariable();
        mockingIsClassName();
    }

    @Test
    public void analysis_success() {
        methodVariableAnalysis.analysis(methodModel);

        assertEquals(createExpectedGlobalVariables(), methodModel.getGlobalVariables());
        assertEquals(createExpectedLocalVariables(), methodModel.getLocalVariables());

        verifyReadVariable();
        verifyIsClassName();

        verifyNoMoreInteractions(variableHelper);
    }

    @Test
    public void analysis_success_emptyStatements() {
        methodModel.setParameters(Collections.emptyList());
        methodModel.setStatements(Collections.emptyList());

        methodVariableAnalysis.analysis(methodModel);

        assertEquals(new ArrayList<>(), methodModel.getGlobalVariables());
        assertEquals(new ArrayList<>(), methodModel.getLocalVariables());

        verifyNoMoreInteractions(variableHelper);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_statementsIsNull() {
        methodModel.setParameters(Collections.emptyList());
        methodModel.setStatements(null);

        methodVariableAnalysis.analysis(null);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_methodModelIsNull() {
        methodVariableAnalysis.analysis(null);
    }

    private void mockingReadVariable() {
        when(variableHelper.readVariable(FIRST_BLOCK_STATEMENT))
                .thenReturn(createFirstBlockStatementVariables());
        when(variableHelper.readVariable(SECOND_STATEMENT))
                .thenReturn(createSecondStatementVariables());

        mockingReadVariableReturnEmpty(FIRST_STATEMENT);
        mockingReadVariableReturnEmpty(SECOND_BLOCK_STATEMENT);
    }

    private void mockingReadVariableReturnEmpty(String statement) {
        when(variableHelper.readVariable(statement))
                .thenReturn(new ArrayList<>());
    }

    private void mockingIsClassName() {
        when(variableHelper.isClassName(FOURTH_READ_VARIABLE))
                .thenReturn(Boolean.TRUE);

        mockingIsClassNameReturnFalse(FIRST_READ_VARIABLE);
        mockingIsClassNameReturnFalse(SECOND_READ_VARIABLE);
        mockingIsClassNameReturnFalse(THIRD_READ_VARIABLE);
        mockingIsClassNameReturnFalse(FIFTH_READ_VARIABLE);
    }

    private void mockingIsClassNameReturnFalse(String variable) {
        when(variableHelper.isClassName(variable))
                .thenReturn(Boolean.FALSE);
    }

    private List<StatementModel> createExpectedStatements() {
        List<StatementModel> statements = new ArrayList<>();

        statements.add(createFirstStatement());
        statements.add(createSecondStatement());

        return statements;
    }

    private StatementModel createFirstStatement() {
        BlockModel blockModel = BlockModel.blockBuilder()
                .build();
        blockModel.setStatement(FIRST_STATEMENT);
        blockModel.setIndex(0);
        blockModel.setStartIndex(0);
        blockModel.setEndIndex(4);
        blockModel.getStatements()
                .add(createFirstBlockStatement());
        blockModel.setEndOfBlockStatement(createFirstBlockEndStatement());

        return blockModel;
    }

    private StatementModel createFirstBlockStatement() {
        return StatementModel.statementBuilder()
                .statement(FIRST_BLOCK_STATEMENT)
                .index(1)
                .startIndex(18)
                .endIndex(54)
                .build();
    }

    private StatementModel createFirstBlockEndStatement() {
        return StatementModel.statementBuilder()
                .statement(FIRST_BLOCK_END_STATEMENT)
                .startIndex(64)
                .endIndex(64)
                .build();
    }

    private StatementModel createSecondStatement() {
        BlockModel blockModel = BlockModel.blockBuilder()
                .build();
        blockModel.setStatement(SECOND_STATEMENT);
        blockModel.setIndex(2);
        blockModel.setStartIndex(66);
        blockModel.setEndIndex(97);
        blockModel.getStatements()
                .add(createSecondBlockStatement());
        blockModel.setEndOfBlockStatement(createSecondBlockEndStatement());

        return blockModel;
    }

    private StatementModel createSecondBlockStatement() {
        return StatementModel.statementBuilder()
                .statement(SECOND_BLOCK_STATEMENT)
                .index(3)
                .startIndex(111)
                .endIndex(122)
                .build();
    }

    private StatementModel createSecondBlockEndStatement() {
        return StatementModel.statementBuilder()
                .statement(SECOND_BLOCK_END_STATEMENT)
                .startIndex(132)
                .endIndex(132)
                .build();
    }

    private List<String> createFirstBlockStatementVariables() {
        List<String> firstBlockStatementVariables = Arrays.asList(
                "user", "+", "+", "this.name", "+", "extension"
        );
        return new ArrayList<>(firstBlockStatementVariables);
    }

    private List<String> createSecondStatementVariables() {
        List<String> secondStatementVariables = Arrays.asList(
                "NullPointerException", "e"
        );
        return new ArrayList<>(secondStatementVariables);
    }

    private List<String> createExpectedGlobalVariables() {
        List<String> expectedGlobalVariables = Arrays.asList(
                SECOND_READ_VARIABLE_CLEAN, THIRD_READ_VARIABLE
        );
        return new ArrayList<>(expectedGlobalVariables);
    }

    private List<VariablePropertyModel> createExpectedLocalVariables() {
        VariablePropertyModel variablePropertyModel = VariablePropertyModel.variablePropertyBuilder()
                .statementIndex(2)
                .build();

        variablePropertyModel.setType(FOURTH_READ_VARIABLE);
        variablePropertyModel.setName(FIFTH_READ_VARIABLE);

        List<VariablePropertyModel> expectedLocalVariables = Collections
                .singletonList(variablePropertyModel);
        return new ArrayList<>(expectedLocalVariables);
    }

    private void verifyReadVariable() {
        verifyReadVariableOneTimes(FIRST_STATEMENT);
        verifyReadVariableOneTimes(FIRST_BLOCK_STATEMENT);
        verifyReadVariableOneTimes(SECOND_STATEMENT);
        verifyReadVariableOneTimes(SECOND_BLOCK_STATEMENT);
    }

    private void verifyReadVariableOneTimes(String statement) {
        verify(variableHelper).readVariable(statement);
    }

    private void verifyIsClassName() {
        verifyIsClassNameOneTimes(FIRST_READ_VARIABLE);
        verifyIsClassNameOneTimes(SECOND_READ_VARIABLE);
        verifyIsClassNameOneTimes(THIRD_READ_VARIABLE);
        verifyIsClassNameOneTimes(FOURTH_READ_VARIABLE);
        verifyIsClassNameOneTimes(FIFTH_READ_VARIABLE);
    }

    private void verifyIsClassNameOneTimes(String variable) {
        verify(variableHelper).isClassName(variable);
    }
}