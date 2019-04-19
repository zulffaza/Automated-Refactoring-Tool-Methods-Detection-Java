package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodStatementAnalysis;
import com.finalproject.automated.refactoring.tool.model.BlockModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.StatementModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 18 April 2019
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class MethodStatementAnalysisImplTest {

    @Autowired
    private MethodStatementAnalysis methodStatementAnalysis;

    private MethodModel methodModel;

    @Before
    public void setUp() {
        methodModel = MethodModel.builder()
                .body(createMethodBody())
                .build();
    }

    @Test
    public void analysis_success() {
        methodStatementAnalysis.analysis(methodModel);

        assertEquals(createMethodBody(), methodModel.getBody());
        assertEquals(createExpectedStatements(), methodModel.getStatements());
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_methodModelIsNull() {
        methodStatementAnalysis.analysis(null);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_methodBodyIsNull() {
        methodModel.setBody(null);
        methodStatementAnalysis.analysis(methodModel);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_methodStatementsIsNull() {
        methodModel.setStatements(null);
        methodStatementAnalysis.analysis(methodModel);
    }

    private String createMethodBody() {
        return "try {\n" +
                "            return user + \"-\" + name + extension;\n" +
                "        } catch (NullPointerException e) {\n" +
                "            return null;\n" +
                "        }\n";
    }

    private List<StatementModel> createExpectedStatements() {
        List<StatementModel> statements = new ArrayList<>();

        statements.add(createFirstStatement());
        statements.add(createSecondStatement());
        statements.add(createThirdStatement());
        statements.add(createSecondStatement());

        return statements;
    }

    private StatementModel createFirstStatement() {
        BlockModel blockModel = BlockModel.blockBuilder()
                .build();
        blockModel.setStatement("try {");
        blockModel.getStatements()
                .add(createFirstBlockStatement());

        return blockModel;
    }

    private StatementModel createFirstBlockStatement() {
        return StatementModel.statementBuilder()
                .statement("return user + \"-\" + name + extension;")
                .build();
    }

    private StatementModel createSecondStatement() {
        return StatementModel.statementBuilder()
                .statement("}")
                .build();
    }

    private StatementModel createThirdStatement() {
        BlockModel blockModel = BlockModel.blockBuilder()
                .build();
        blockModel.setStatement("catch (NullPointerException e) {");
        blockModel.getStatements()
                .add(createSecondBlockStatement());

        return blockModel;
    }

    private StatementModel createSecondBlockStatement() {
        return StatementModel.statementBuilder()
                .statement("return null;")
                .build();
    }
}