package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodVariableAnalysis;
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

import static org.junit.Assert.*;

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

    private MethodModel methodModel;

    @Before
    public void setUp() {
        methodModel = MethodModel.builder()
                .statements(createExpectedStatements())
                .build();
    }

    @Test
    public void analysis() {
        methodVariableAnalysis.analysis(methodModel);

        System.out.println("Global variables : ");

        methodModel.getGlobalVariables()
                .forEach(System.out::println);

        System.out.println();
        System.out.println("Local variables : ");

        methodModel.getLocalVariables()
                .forEach(propertyModel -> {
                    System.out.println("Type --> " + propertyModel.getType());
                    System.out.println("Name --> " + propertyModel.getName());
                    System.out.println();
                });
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
        blockModel.setStatement("try {");
        blockModel.setStartIndex(0);
        blockModel.setEndIndex(4);
        blockModel.getStatements()
                .add(createFirstBlockStatement());
        blockModel.setEndOfBlockStatement(createFirstBlockEndStatement());

        return blockModel;
    }

    private StatementModel createFirstBlockStatement() {
        return StatementModel.statementBuilder()
                .statement("return user + \"-\" + name + extension;")
                .startIndex(18)
                .endIndex(54)
                .build();
    }

    private StatementModel createFirstBlockEndStatement() {
        return StatementModel.statementBuilder()
                .statement("}")
                .startIndex(64)
                .endIndex(64)
                .build();
    }

    private StatementModel createSecondStatement() {
        BlockModel blockModel = BlockModel.blockBuilder()
                .build();
        blockModel.setStatement("catch (NullPointerException e) {");
        blockModel.setStartIndex(66);
        blockModel.setEndIndex(97);
        blockModel.getStatements()
                .add(createSecondBlockStatement());
        blockModel.setEndOfBlockStatement(createSecondBlockEndStatement());

        return blockModel;
    }

    private StatementModel createSecondBlockStatement() {
        return StatementModel.statementBuilder()
                .statement("return null;")
                .startIndex(111)
                .endIndex(122)
                .build();
    }

    private StatementModel createSecondBlockEndStatement() {
        return StatementModel.statementBuilder()
                .statement("}")
                .startIndex(132)
                .endIndex(132)
                .build();
    }
}