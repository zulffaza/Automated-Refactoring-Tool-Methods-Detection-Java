package com.finalproject.automated.refactoring.tool.methods.detection.java;

import com.finalproject.automated.refactoring.tool.files.detection.model.FileModel;
import com.finalproject.automated.refactoring.tool.methods.detection.service.MethodsDetection;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 9 December 2018
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    @Autowired
    private MethodsDetection methodsDetection;

    private static final Integer FIRST_INDEX = 0;
    private static final Integer SECOND_INDEX = 1;
    private static final Integer THIRD_INDEX = 2;
    private static final Integer ZERO = 0;
    private static final Integer ONE = 1;
    private static final Integer TWO = 2;
    private static final Integer THREE = 3;

    private static final String METHOD_KEYWORD_ONE = "@SuppressWarnings()";
    private static final String METHOD_KEYWORD_TWO = "public";
    private static final String METHOD_KEYWORD_THREE = "@GetMapping( value = \"/{filename}/change\", produces = MediaType.APPLICATION_JSON_VALUE )";
    private static final String METHOD_NAME = "changeFilename";
    private static final String METHOD_RETURN_TYPE = "Response<String, String>";
    private static final String METHOD_PARAMETER_KEYWORD_ONE = "@RequestParam(required = false, defaultValue = \"null\")";
    private static final String METHOD_PARAMETER_KEYWORD_TWO = "@RequestParam(required = false, defaultValue = \".java\")";
    private static final String METHOD_PARAMETER_KEYWORD_THREE = "@RequestParam(required = false)";
    private static final String METHOD_PARAMETER_TYPE = "String";
    private static final String METHOD_PARAMETER_NAME_ONE = "name";
    private static final String METHOD_PARAMETER_NAME_TWO = "extension";
    private static final String METHOD_PARAMETER_NAME_THREE = "user";
    private static final String METHOD_EXCEPTION_ONE = "Exception";
    private static final String METHOD_EXCEPTION_TWO = "IOException";

    private List<FileModel> fileModels;

    @Before
    public void setUp() {
        fileModels = createFileModels();
    }

    @Test
    public void methodsDetection_singleFile_success() {
        List<MethodModel> methodModels = methodsDetection.detect(fileModels.get(FIRST_INDEX));
        assertMethodModels(methodModels);
    }

    @Test(expected = NullPointerException.class)
    public void methodsDetection_singleFile_fileModelIsEmpty() {
        methodsDetection.detect(FileModel.builder().build());
    }

    @Test(expected = NullPointerException.class)
    public void methodsDetection_singleFile_fileModelIsNull() {
        FileModel fileModel = null;
        methodsDetection.detect(fileModel);
    }

    @Test
    public void methodsDetection_multiFile_success() {
        String key = fileModels.get(FIRST_INDEX).getPath() + File.separator + fileModels.get(FIRST_INDEX).getFilename();
        Map<String, List<MethodModel>> result = methodsDetection.detect(fileModels);

        assertEquals(ONE.intValue(), result.size());
        assertTrue(result.containsKey(key));

        List<MethodModel> methodModels = result.get(key);
        assertMethodModels(methodModels);
    }

    @Test(expected = NullPointerException.class)
    public void methodsDetection_multiFile_fileModelIsEmpty() {
        methodsDetection.detect(Collections.singletonList(FileModel.builder().build()));
    }

    @Test
    public void methodsDetection_multiFile_fileModelsIsEmpty() {
        Map<String, List<MethodModel>> result = methodsDetection.detect(Collections.emptyList());
        assertEquals(ZERO.intValue(), result.size());
    }

    @Test(expected = NullPointerException.class)
    public void methodsDetection_multiFile_fileModelsIsNull() {
        List<FileModel> fileModels = null;
        methodsDetection.detect(fileModels);
    }

    private List<FileModel> createFileModels() {
        List<FileModel> fileModels = new ArrayList<>();

        fileModels.add(FileModel.builder()
                .path("path")
                .filename("Filename.java")
                .content(createFileContent())
                .build());

        return fileModels;
    }

    private static String createFileContent() {
        return "package path;\n" +
                "\n" +
                "import java.io.Serializable;\n" +
                "\n" +
                "public class Filename implements Serializable {\n" +
                "\n" +
                "    @GetMapping(\n" +
                "               value = \"/{filename}/change\",\n" +
                "               produces = MediaType.APPLICATION_JSON_VALUE\n" +
                "    )\n" +
                "    @SuppressWarnings()\n" +
                "    public Response<String, String> changeFilename(@RequestParam(required = false, defaultValue = \"null\") String name,\n" +
                "                                              @RequestParam(required = false, defaultValue = \".java\") String extension,\n" +
                "                                              @RequestParam(required = false) String user) throws Exception, IOException {\n" +
                "        try {\n" +
                "            return user + \"-\" + name + extension;\n" +
                "        } catch (NullPointerException e) {\n" +
                "            return null;\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    private void assertMethodModels(List<MethodModel> methodModels) {
        assertEquals(ONE.intValue(), methodModels.size());

        assertMethodKeywords(methodModels);
        assertMethodNameAndReturnType(methodModels);
        assertMethodParameters(methodModels);
        assertMethodExceptions(methodModels);
        assertMethodAnother(methodModels);
    }

    private void assertMethodKeywords(List<MethodModel> methodModels) {
        assertEquals(THREE.intValue(), methodModels.get(FIRST_INDEX).getKeywords().size());
        assertEquals(METHOD_KEYWORD_THREE, methodModels.get(FIRST_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_KEYWORD_ONE, methodModels.get(FIRST_INDEX).getKeywords().get(SECOND_INDEX));
        assertEquals(METHOD_KEYWORD_TWO, methodModels.get(FIRST_INDEX).getKeywords().get(THIRD_INDEX));
    }

    private void assertMethodNameAndReturnType(List<MethodModel> methodModels) {
        assertEquals(METHOD_NAME, methodModels.get(FIRST_INDEX).getName());
        assertEquals(METHOD_RETURN_TYPE, methodModels.get(FIRST_INDEX).getReturnType());
    }

    private void assertMethodParameters(List<MethodModel> methodModels) {
        assertEquals(THREE.intValue(), methodModels.get(FIRST_INDEX).getParameters().size());

        assertMethodParametersFirstIndex(methodModels);
        assertMethodParametersSecondIndex(methodModels);
        assertMethodParametersThirdIndex(methodModels);
    }

    private void assertMethodParametersFirstIndex(List<MethodModel> methodModels) {
        assertEquals(ONE.intValue(), methodModels.get(FIRST_INDEX).getParameters()
                .get(FIRST_INDEX).getKeywords().size());
        assertEquals(METHOD_PARAMETER_KEYWORD_ONE, methodModels.get(FIRST_INDEX).getParameters()
                .get(FIRST_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_PARAMETER_TYPE, methodModels.get(FIRST_INDEX).getParameters()
                .get(FIRST_INDEX).getType());
        assertEquals(METHOD_PARAMETER_NAME_ONE, methodModels.get(FIRST_INDEX).getParameters()
                .get(FIRST_INDEX).getName());
    }

    private void assertMethodParametersSecondIndex(List<MethodModel> methodModels) {
        assertEquals(ONE.intValue(), methodModels.get(FIRST_INDEX).getParameters()
                .get(SECOND_INDEX).getKeywords().size());
        assertEquals(METHOD_PARAMETER_KEYWORD_TWO, methodModels.get(FIRST_INDEX).getParameters()
                .get(SECOND_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_PARAMETER_TYPE, methodModels.get(FIRST_INDEX).getParameters()
                .get(SECOND_INDEX).getType());
        assertEquals(METHOD_PARAMETER_NAME_TWO, methodModels.get(FIRST_INDEX).getParameters()
                .get(SECOND_INDEX).getName());
    }

    private void assertMethodParametersThirdIndex(List<MethodModel> methodModels) {
        assertEquals(ONE.intValue(), methodModels.get(FIRST_INDEX).getParameters()
                .get(THIRD_INDEX).getKeywords().size());
        assertEquals(METHOD_PARAMETER_KEYWORD_THREE, methodModels.get(FIRST_INDEX).getParameters()
                .get(THIRD_INDEX).getKeywords().get(FIRST_INDEX));
        assertEquals(METHOD_PARAMETER_TYPE, methodModels.get(FIRST_INDEX).getParameters()
                .get(THIRD_INDEX).getType());
        assertEquals(METHOD_PARAMETER_NAME_THREE, methodModels.get(FIRST_INDEX).getParameters()
                .get(THIRD_INDEX).getName());
    }

    private void assertMethodExceptions(List<MethodModel> methodModels) {
        assertEquals(TWO.intValue(), methodModels.get(FIRST_INDEX).getExceptions().size());
        assertEquals(METHOD_EXCEPTION_ONE, methodModels.get(FIRST_INDEX).getExceptions().get(FIRST_INDEX));
        assertEquals(METHOD_EXCEPTION_TWO, methodModels.get(FIRST_INDEX).getExceptions().get(SECOND_INDEX));
    }

    private void assertMethodAnother(List<MethodModel> methodModels) {
        assertNotNull(methodModels.get(FIRST_INDEX).getBody());
        assertNull(methodModels.get(FIRST_INDEX).getLoc());
    }
}
