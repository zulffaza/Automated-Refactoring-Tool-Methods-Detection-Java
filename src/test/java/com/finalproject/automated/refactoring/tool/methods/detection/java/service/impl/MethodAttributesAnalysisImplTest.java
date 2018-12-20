package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.files.detection.model.FileModel;
import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.PropertyModel;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 12 December 2018
 */

public class MethodAttributesAnalysisImplTest {

    private MethodAttributesAnalysisImpl methodAttributesAnalysis;

    private FileModel fileModel;

    private IndexModel indexModel;

    private IndexModel nonConstructorIndexModel;

    private IndexModel invalidIndexModel;

    @Before
    public void setUp() {
        methodAttributesAnalysis = new MethodAttributesAnalysisImpl();
        fileModel = getFileModel();
        indexModel = IndexModel.builder()
                .start(234)
                .end(360)
                .build();
        nonConstructorIndexModel = IndexModel.builder()
                .start(486)
                .end(535)
                .build();
        invalidIndexModel = IndexModel.builder()
                .start(567)
                .end(657)
                .build();
    }

    @Test
    public void analysis_success_constructorMethod() {
        MethodModel methodModel = MethodModel.builder().build();
        MethodModel expectedMethodModel = getConstructorExpectedMethodModel();

        methodAttributesAnalysis.analysis(fileModel, indexModel, methodModel);

        assertEquals(expectedMethodModel, methodModel);
    }

    @Test
    public void analysis_success_nonConstructorMethod() {
        MethodModel methodModel = MethodModel.builder().build();
        MethodModel expectedMethodModel = getNonConstructorExpectedMethodModel();

        methodAttributesAnalysis.analysis(fileModel, nonConstructorIndexModel, methodModel);

        assertEquals(expectedMethodModel, methodModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void analysis_failed_unstructuredMethodName() {
        MethodModel methodModel = MethodModel.builder().build();
        methodAttributesAnalysis.analysis(fileModel, invalidIndexModel, methodModel);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_fileModelIsNull() {
        MethodModel methodModel = MethodModel.builder().build();
        methodAttributesAnalysis.analysis(null, indexModel, methodModel);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_indexModelIsNull() {
        MethodModel methodModel = MethodModel.builder().build();
        methodAttributesAnalysis.analysis(fileModel, null, methodModel);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_methodModelIsNull() {
        methodAttributesAnalysis.analysis(fileModel, indexModel, null);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_paramsIsNull() {
        methodAttributesAnalysis.analysis(null, null, null);
    }

    private static FileModel getFileModel() {
        return FileModel.builder()
                .path("path")
                .filename("Filename.java")
                .content(getFileContent())
                .build();
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
                "    @Bean\n" +
                "    (\n"+
                "        name = \"FileExtension\"\n"+
                "    )\n"+
                "    public String @getExtension() {\n" +
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
                .keywords(Arrays.asList("@SuppressWarnings()", "public"))
                .name("Filename")
                .parameters(Arrays.asList(
                        PropertyModel.builder()
                                .keywords(Arrays.asList("@NonNull"))
                                .type("String")
                                .name("name")
                                .build(),
                        PropertyModel.builder()
                                .keywords(Arrays.asList("@NonNull"))
                                .type("String")
                                .name("extension")
                                .build()
                ))
                .exceptions(Arrays.asList("Exception", "IOException"))
                .build();
    }

    private MethodModel getNonConstructorExpectedMethodModel() {
        return MethodModel.builder()
                .keywords(Arrays.asList("public"))
                .returnType("void")
                .name("setName")
                .parameters(Arrays.asList(
                        PropertyModel.builder()
                                .keywords(Arrays.asList("@NonNull"))
                                .type("String")
                                .name("name")
                                .build()
                ))
                .build();
    }
}