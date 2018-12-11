package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 11 December 2018
 */

public class StartIndexAnalysisImplTest {

    private StartIndexAnalysisImpl startIndexAnalysis;

    private String content;

    private IndexModel indexModel;

    private IndexModel nonConstructorIndexModel;

    @Before
    public void setUp() {
        startIndexAnalysis = new StartIndexAnalysisImpl();
        content = getFileContent();
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
    public void analysis_success_constructorMethod() {
        startIndexAnalysis.analysis(content, indexModel);

        assertEquals(234, indexModel.getStart().intValue());
        assertEquals(360, indexModel.getEnd().intValue());
    }

    @Test
    public void analysis_success_nonConstructorMethod() {
        startIndexAnalysis.analysis(content, nonConstructorIndexModel);

        assertEquals(486, nonConstructorIndexModel.getStart().intValue());
        assertEquals(535, nonConstructorIndexModel.getEnd().intValue());
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_contentIsNull() {
        startIndexAnalysis.analysis(null, indexModel);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_indexModelIsNull() {
        startIndexAnalysis.analysis(content, null);
    }

    @Test(expected = NullPointerException.class)
    public void analysis_failed_paramsIsNull() {
        startIndexAnalysis.analysis(null, null);
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
}