package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 12 December 2018
 */

public class MethodBodyAnalysisImplTest {

    private MethodBodyAnalysisImpl methodBodyAnalysis;

    private String content;

    private IndexModel indexModel;

    private IndexModel nonConstructorIndexModel;

    @Before
    public void setUp() {
        methodBodyAnalysis = new MethodBodyAnalysisImpl();
        content = getFileContent();
        indexModel = IndexModel.builder()
                .start(234)
                .end(360)
                .build();
        nonConstructorIndexModel = IndexModel.builder()
                .start(486)
                .end(535)
                .build();
    }

    @Test
    public void analysis_success_constructorMethod() {
        MethodModel methodModel = MethodModel.builder().build();
        MethodModel expectedMethodModel = getConstructorExpectedMethodModel();

        methodBodyAnalysis.analysis(content, indexModel, methodModel);

        assertEquals(expectedMethodModel, methodModel);
    }

    @Test
    public void analysis_success_nonConstructorMethod() {
        MethodModel methodModel = MethodModel.builder().build();
        MethodModel expectedMethodModel = getNonConstructorExpectedMethodModel();

        methodBodyAnalysis.analysis(content, nonConstructorIndexModel, methodModel);

        assertEquals(expectedMethodModel, methodModel);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_contentIsNull() {
        MethodModel methodModel = MethodModel.builder().build();
        methodBodyAnalysis.analysis(null, indexModel, methodModel);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_indexModelIsNull() {
        MethodModel methodModel = MethodModel.builder().build();
        methodBodyAnalysis.analysis(content, null, methodModel);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_methodModelIsNull() {
        methodBodyAnalysis.analysis(content, indexModel, null);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_paramsIsNull() {
        methodBodyAnalysis.analysis(null, null, null);
    }

    private static String getFileContent() {
        return "package path;\n" +
                "\n" +
                "import java.io.Serializable;\n" +
                "\n" +
                "public class Filename implements Serializable {\n" +
                "\n" +
                "    private String name;\n" +
                "    private String extension;\n" +
                "\n" +
                "    @SuppressWarnings()\n" +
                "    public Filename() throws Exception, IOException {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @SuppressWarnings()\n" +
                "    public Filename(@NonNull String name, @NonNull String extension) throws Exception, IOException {\n" +
                "        this.name = name;\n" +
                "        this.extension = extension;\n" +
                "    }\n" +
                "\n" +
                "    public String getName() {\n" +
                "        return name;\n" +
                "    }\n" +
                "\n" +
                "    public void setName(@NonNull String name) {\n" +
                "        this.name = name;\n" +
                "    }\n" +
                "\n" +
                "    public String getExtension() {\n" +
                "        return extension;\n" +
                "    }\n" +
                "\n" +
                "    public void setExtension(@NonNull String extension) {\n" +
                "        this.extension = extension;\n" +
                "    }\n" +
                "}";
    }


    private MethodModel getConstructorExpectedMethodModel() {
        return MethodModel.builder()
                .body("this.name = name;\n" +
                        "        this.extension = extension;")
                .build();
    }

    private MethodModel getNonConstructorExpectedMethodModel() {
        return MethodModel.builder()
                .body("this.name = name;")
                .build();
    }
}