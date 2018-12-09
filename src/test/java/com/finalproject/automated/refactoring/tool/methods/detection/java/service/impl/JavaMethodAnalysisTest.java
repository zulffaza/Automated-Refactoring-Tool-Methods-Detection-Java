package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.files.detection.model.FileModel;
import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 23 October 2018
 */

public class JavaMethodAnalysisTest {

    private static final Integer FIRST_INDEX = 0;
    private static final Integer SECOND_INDEX = 1;
    private static final Integer ZERO = 0;
    private static final Integer ONE = 1;
    private static final Integer TWO = 2;

    private static final String METHOD_KEYWORD_ONE = "@SuppressWarnings()";
    private static final String METHOD_KEYWORD_TWO = "public";
    private static final String METHOD_PARAMETER_KEYWORD = "@NonNull";
    private static final String METHOD_PARAMETER_TYPE = "String";
    private static final String METHOD_PARAMETER_NAME_ONE = "name";
    private static final String METHOD_PARAMETER_NAME_TWO = "extension";
    private static final String METHOD_EXCEPTION_ONE = "Exception";
    private static final String METHOD_EXCEPTION_TWO = "IOException";

    private JavaMethodAnalysis methodAnalysis;

    private FileModel fileModel;

    private IndexModel indexModel;

    private IndexModel nonConstructorIndexModel;

    @Before
    public void setUp() {
        methodAnalysis = new JavaMethodAnalysis();
        fileModel = getFileModel();
        indexModel = IndexModel.builder()
                .start(241)
                .end(360)
                .build();
        nonConstructorIndexModel = IndexModel.builder()
                .start(503)
                .end(535)
                .build();
    }

    @Test
    public void analysis_success() {
        String methodName = "Filename";

        Map<String, List<MethodModel>> result = Collections.synchronizedMap(new HashMap<>());
        methodAnalysis.analysis(fileModel, indexModel, result);

        analysisSuccessCheckResult(result);
        analysisSuccessCheckResultKeywords(result);
        analysisSuccessCheckResultReturnType(result);
        analysisSuccessCheckResultName(methodName, result);
        analysisSuccessCheckResultParameters(result);
        analysisSuccessCheckResultExceptions(result);
        analysisSuccessCheckResultAnother(result);
    }

    @Test
    public void analysis_success_resultIsNotEmpty() {
        String methodName = "Filename";

        Map<String, List<MethodModel>> result = Collections.synchronizedMap(new HashMap<>());
        result.put(fileModel.getFilename(), new ArrayList<>());
        result.get(fileModel.getFilename()).add(MethodModel.builder().build());

        methodAnalysis.analysis(fileModel, indexModel, result);

        analysisSuccessResultIsNotEmptyCheckResult(result);
        analysisSuccessResultIsNotEmptyCheckResultKeywords(result);
        analysisSuccessResultIsNotEmptyCheckResultReturnType(result);
        analysisSuccessResultIsNotEmptyCheckResultName(methodName, result);
        analysisSuccessResultIsNotEmptyCheckResultParameters(result);
        analysisSuccessResultIsNotEmptyCheckResultExceptions(result);
        analysisSuccessResultIsNotEmptyCheckResultAnother(result);
    }

    @Test
    public void analysis_success_nonConstructorMethods() {
        String methodName = "setName";
        String returnType = "void";

        Map<String, List<MethodModel>> result = Collections.synchronizedMap(new HashMap<>());
        methodAnalysis.analysis(fileModel, nonConstructorIndexModel, result);

        analysisSuccessNonConstructorMethodsCheckResult(result);
        analysisSuccessNonConstructorMethodsCheckResultKeywords(result);
        analysisSuccessNonConstructorMethodsCheckResultReturnType(returnType, result);
        analysisSuccessNonConstructorMethodsCheckResultName(methodName, result);
        analysisSuccessNonConstructorMethodsCheckResultParameters(result);
        analysisSuccessNonConstructorMethodsCheckResultExceptions(result);
        analysisSuccessNonConstructorMethodsCheckResultAnother(result);
    }

    @Test
    public void analysis_failed_fileModelIsNull() {
        Map<String, List<MethodModel>> result = Collections.synchronizedMap(new HashMap<>());
        methodAnalysis.analysis(null, indexModel, result);

        assertEquals(ZERO.intValue(), result.size());
    }

    @Test
    public void analysis_failed_indexModelIsNull() {
        Map<String, List<MethodModel>> result = Collections.synchronizedMap(new HashMap<>());
        methodAnalysis.analysis(fileModel, null, result);

        assertEquals(ZERO.intValue(), result.size());
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
                "    public String getExtension() {\n" +
                "        return extension;\n" +
                "    }\n" +
                "\n" +
                "    public void setExtension(@NonNull String extension) {\n" +
                "        this.extension = extension;\n" +
                "    }\n" +
                "}";
    }

    private void analysisSuccessCheckResult(Map<String, List<MethodModel>> result) {
        assertEquals(ONE.intValue(), result.size());
        assertTrue(result.containsKey(fileModel.getFilename()));
        assertEquals(ONE.intValue(), result.get(fileModel.getFilename()).size());
    }

    private void analysisSuccessCheckResultKeywords(Map<String, List<MethodModel>> result) {
        assertEquals(TWO.intValue(), result.get(fileModel.getFilename()).get(FIRST_INDEX).getKeywords().size());
        assertEquals(METHOD_KEYWORD_ONE,
                result.get(fileModel.getFilename()).get(FIRST_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_KEYWORD_TWO,
                result.get(fileModel.getFilename()).get(FIRST_INDEX).getKeywords().get(SECOND_INDEX));
    }

    private void analysisSuccessCheckResultReturnType(Map<String, List<MethodModel>> result) {
        assertNull(result.get(fileModel.getFilename()).get(FIRST_INDEX).getReturnType());
    }

    private void analysisSuccessCheckResultName(String methodName, Map<String, List<MethodModel>> result) {
        assertEquals(methodName, result.get(fileModel.getFilename()).get(FIRST_INDEX).getName());
    }

    private void analysisSuccessCheckResultParameters(Map<String, List<MethodModel>> result) {
        assertEquals(TWO.intValue(), result.get(fileModel.getFilename()).get(FIRST_INDEX).getParameters().size());
        assertEquals(ONE.intValue(), result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(FIRST_INDEX).getKeywords().size());
        assertEquals(METHOD_PARAMETER_KEYWORD, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(FIRST_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_PARAMETER_NAME_ONE, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(FIRST_INDEX).getName());
        assertEquals(METHOD_PARAMETER_TYPE, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(FIRST_INDEX).getType());
        assertEquals(ONE.intValue(), result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(SECOND_INDEX).getKeywords().size());
        assertEquals(METHOD_PARAMETER_KEYWORD, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(SECOND_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_PARAMETER_NAME_TWO, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(SECOND_INDEX).getName());
        assertEquals(METHOD_PARAMETER_TYPE, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(SECOND_INDEX).getType());
    }

    private void analysisSuccessCheckResultExceptions(Map<String, List<MethodModel>> result) {
        assertEquals(TWO.intValue(), result.get(fileModel.getFilename()).get(FIRST_INDEX).getExceptions().size());
        assertEquals(METHOD_EXCEPTION_ONE, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getExceptions().get(FIRST_INDEX));
        assertEquals(METHOD_EXCEPTION_TWO, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getExceptions().get(SECOND_INDEX));
    }

    private void analysisSuccessCheckResultAnother(Map<String, List<MethodModel>> result) {
        assertNotNull(result.get(fileModel.getFilename()).get(FIRST_INDEX).getBody());
        assertNull(result.get(fileModel.getFilename()).get(FIRST_INDEX).getLoc());
    }

    private void analysisSuccessResultIsNotEmptyCheckResult(Map<String, List<MethodModel>> result) {
        assertEquals(ONE.intValue(), result.size());
        assertTrue(result.containsKey(fileModel.getFilename()));
        assertEquals(TWO.intValue(), result.get(fileModel.getFilename()).size());
    }

    private void analysisSuccessResultIsNotEmptyCheckResultKeywords(Map<String, List<MethodModel>> result) {
        assertEquals(TWO.intValue(), result.get(fileModel.getFilename()).get(SECOND_INDEX).getKeywords().size());
        assertEquals(METHOD_KEYWORD_ONE,
                result.get(fileModel.getFilename()).get(SECOND_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_KEYWORD_TWO,
                result.get(fileModel.getFilename()).get(SECOND_INDEX).getKeywords().get(SECOND_INDEX));
    }

    private void analysisSuccessResultIsNotEmptyCheckResultReturnType(Map<String, List<MethodModel>> result) {
        assertNull(result.get(fileModel.getFilename()).get(SECOND_INDEX).getReturnType());
    }

    private void analysisSuccessResultIsNotEmptyCheckResultName(String methodName, Map<String, List<MethodModel>> result) {
        assertEquals(methodName, result.get(fileModel.getFilename()).get(SECOND_INDEX).getName());
    }

    private void analysisSuccessResultIsNotEmptyCheckResultParameters(Map<String, List<MethodModel>> result) {
        assertEquals(TWO.intValue(), result.get(fileModel.getFilename()).get(SECOND_INDEX).getParameters().size());
        assertEquals(ONE.intValue(), result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getParameters().get(FIRST_INDEX).getKeywords().size());
        assertEquals(METHOD_PARAMETER_KEYWORD, result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getParameters().get(FIRST_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_PARAMETER_NAME_ONE, result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getParameters().get(FIRST_INDEX).getName());
        assertEquals(METHOD_PARAMETER_TYPE, result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getParameters().get(FIRST_INDEX).getType());
        assertEquals(ONE.intValue(), result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getParameters().get(SECOND_INDEX).getKeywords().size());
        assertEquals(METHOD_PARAMETER_KEYWORD, result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getParameters().get(SECOND_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_PARAMETER_NAME_TWO, result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getParameters().get(SECOND_INDEX).getName());
        assertEquals(METHOD_PARAMETER_TYPE, result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getParameters().get(SECOND_INDEX).getType());
    }

    private void analysisSuccessResultIsNotEmptyCheckResultExceptions(Map<String, List<MethodModel>> result) {
        assertEquals(TWO.intValue(), result.get(fileModel.getFilename()).get(SECOND_INDEX).getExceptions().size());
        assertEquals(METHOD_EXCEPTION_ONE, result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getExceptions().get(FIRST_INDEX));
        assertEquals(METHOD_EXCEPTION_TWO, result.get(fileModel.getFilename()).get(SECOND_INDEX)
                .getExceptions().get(SECOND_INDEX));
    }

    private void analysisSuccessResultIsNotEmptyCheckResultAnother(Map<String, List<MethodModel>> result) {
        assertNotNull(result.get(fileModel.getFilename()).get(SECOND_INDEX).getBody());
        assertNull(result.get(fileModel.getFilename()).get(SECOND_INDEX).getLoc());
    }

    private void analysisSuccessNonConstructorMethodsCheckResult(Map<String, List<MethodModel>> result) {
        assertEquals(ONE.intValue(), result.size());
        assertTrue(result.containsKey(fileModel.getFilename()));
        assertEquals(ONE.intValue(), result.get(fileModel.getFilename()).size());
    }

    private void analysisSuccessNonConstructorMethodsCheckResultKeywords(Map<String, List<MethodModel>> result) {
        assertEquals(ONE.intValue(), result.get(fileModel.getFilename()).get(FIRST_INDEX).getKeywords().size());
        assertEquals(METHOD_KEYWORD_TWO,
                result.get(fileModel.getFilename()).get(FIRST_INDEX).getKeywords().get(FIRST_INDEX));
    }

    private void analysisSuccessNonConstructorMethodsCheckResultReturnType(String returnType,
                                                                           Map<String, List<MethodModel>> result) {
        assertEquals(returnType, result.get(fileModel.getFilename()).get(FIRST_INDEX).getReturnType());
    }

    private void analysisSuccessNonConstructorMethodsCheckResultName(String methodName,
                                                                     Map<String, List<MethodModel>> result) {
        assertEquals(methodName, result.get(fileModel.getFilename()).get(FIRST_INDEX).getName());
    }

    private void analysisSuccessNonConstructorMethodsCheckResultParameters(Map<String, List<MethodModel>> result) {
        assertEquals(ONE.intValue(), result.get(fileModel.getFilename()).get(FIRST_INDEX).getParameters().size());
        assertEquals(ONE.intValue(), result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(FIRST_INDEX).getKeywords().size());
        assertEquals(METHOD_PARAMETER_KEYWORD, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(FIRST_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_PARAMETER_NAME_ONE, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(FIRST_INDEX).getName());
        assertEquals(METHOD_PARAMETER_TYPE, result.get(fileModel.getFilename()).get(FIRST_INDEX)
                .getParameters().get(FIRST_INDEX).getType());
    }

    private void analysisSuccessNonConstructorMethodsCheckResultExceptions(Map<String, List<MethodModel>> result) {
        assertEquals(ZERO.intValue(), result.get(fileModel.getFilename()).get(FIRST_INDEX).getExceptions().size());
    }

    private void analysisSuccessNonConstructorMethodsCheckResultAnother(Map<String, List<MethodModel>> result) {
        assertNotNull(result.get(fileModel.getFilename()).get(FIRST_INDEX).getBody());
        assertNull(result.get(fileModel.getFilename()).get(FIRST_INDEX).getLoc());
    }
}