package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.files.detection.model.FileModel;
import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodAttributesAnalysis;
import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.PropertyModel;
import com.finalproject.automated.refactoring.tool.utils.service.MergeListHelper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 12 December 2018
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class MethodAttributesAnalysisImplTest {

    @Autowired
    private MethodAttributesAnalysis methodAttributesAnalysis;

    @MockBean
    private MergeListHelper mergeListHelper;

    private FileModel fileModel;

    private IndexModel indexModel;

    private IndexModel nonConstructorIndexModel;

    private IndexModel invalidIndexModel;

    @Before
    public void setUp() {
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

        mockingMergeListOfStringConstructorMethod();
        mockingMergeListOfStringNonConstructorMethod();
    }

    @Test
    public void analysis_success_constructorMethod() {
        MethodModel methodModel = MethodModel.builder().build();
        MethodModel expectedMethodModel = getConstructorExpectedMethodModel();

        methodAttributesAnalysis.analysis(fileModel, indexModel, methodModel);

        assertEquals(expectedMethodModel, methodModel);

        verifyMergeListOfStringConstructorMethod();
    }

    @Test
    public void analysis_success_nonConstructorMethod() {
        MethodModel methodModel = MethodModel.builder().build();
        MethodModel expectedMethodModel = getNonConstructorExpectedMethodModel();

        methodAttributesAnalysis.analysis(fileModel, nonConstructorIndexModel, methodModel);

        assertEquals(expectedMethodModel, methodModel);

        verifyMergeListOfStringNonConstructorMethod();
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
                "    (\n" +
                "        name = \"FileExtension\"\n" +
                "    )\n" +
                "    public String @getExtension() {\n" +
                "        return extension;\n" +
                "    }\n" +
                "\n" +
                "    public void setExtension(@NonNull String extension) {\n" +
                "        this.extension = extension;\n" +
                "    }\n" +
                "}";
    }

    private void mockingMergeListOfStringConstructorMethod() {
        doNothing()
                .when(mergeListHelper)
                .mergeListOfString(
                        eq(Arrays.asList("@SuppressWarnings()", "public", "Filename")),
                        eq(new ArrayList<>()),
                        eq(" "));
        doNothing()
                .when(mergeListHelper)
                .mergeListOfString(
                        eq(Arrays.asList("@NonNull String name", "@NonNull String extension")),
                        eq(new ArrayList<>()),
                        eq(", "));
        doNothing()
                .when(mergeListHelper)
                .mergeListOfString(
                        eq(Arrays.asList("@NonNull", "String", "name")),
                        eq(new ArrayList<>()),
                        eq(" "));
        doNothing()
                .when(mergeListHelper)
                .mergeListOfString(
                        eq(Arrays.asList("@NonNull", "public", "extension")),
                        eq(new ArrayList<>()),
                        eq(" "));
    }

    private void mockingMergeListOfStringNonConstructorMethod() {
        doNothing()
                .when(mergeListHelper)
                .mergeListOfString(
                        eq(Arrays.asList("public", "void", "setName")),
                        eq(new ArrayList<>()),
                        eq(" "));
        doNothing()
                .when(mergeListHelper)
                .mergeListOfString(
                        eq(Collections.singletonList("@NonNull String name")),
                        eq(new ArrayList<>()),
                        eq(", "));
        doNothing()
                .when(mergeListHelper)
                .mergeListOfString(
                        eq(Arrays.asList("@NonNull", "String", "name")),
                        eq(new ArrayList<>()),
                        eq(" "));
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

    private void verifyMergeListOfStringConstructorMethod() {
        verifyMergeListOfStringOneTime(
                Arrays.asList("@SuppressWarnings()", "public", "Filename"),
                new ArrayList<>(),
                " ");
        verifyMergeListOfStringOneTime(
                Arrays.asList("@NonNull String name", "@NonNull String extension"),
                new ArrayList<>(),
                ", ");
        verify(mergeListHelper, times(2))
                .mergeListOfString(Collections.singletonList("@NonNull"),
                        new ArrayList<>(), " ");

        verifyNoMoreInteractions(mergeListHelper);
    }

    private void verifyMergeListOfStringNonConstructorMethod() {
        verifyMergeListOfStringOneTime(
                Arrays.asList("public", "void", "setName"),
                new ArrayList<>(),
                " ");
        verifyMergeListOfStringOneTime(
                Collections.singletonList("@NonNull String name"),
                new ArrayList<>(),
                ", ");
        verifyMergeListOfStringOneTime(
                Collections.singletonList("@NonNull"),
                new ArrayList<>(),
                " ");

        verifyNoMoreInteractions(mergeListHelper);
    }

    private void verifyMergeListOfStringOneTime(List<String> listOfString,
                                                List<Integer> mergeIndex,
                                                String delimiter) {
        verify(mergeListHelper)
                .mergeListOfString(listOfString, mergeIndex, delimiter);
    }
}