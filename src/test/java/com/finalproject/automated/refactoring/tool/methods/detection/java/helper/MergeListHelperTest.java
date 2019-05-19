package com.finalproject.automated.refactoring.tool.methods.detection.java.helper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 19 May 2019
 */

public class MergeListHelperTest {

    @Test
    public void mergeListOfString_success() {
        List<String> listOfString = createListOfString();
        MergeListHelper.mergeListOfString(listOfString, createMergeIndex(), " ");

        assertEquals(createExpectedListOfString(), listOfString);
    }

    private List<String> createListOfString() {
        List<String> strings = Arrays.asList("Merge", "List", "Helper", "Test");
        return new ArrayList<>(strings);
    }

    private List<Integer> createMergeIndex() {
        List<Integer> index = Arrays.asList(1, 2);
        return new ArrayList<>(index);
    }

    private List<String> createExpectedListOfString() {
        List<String> strings = Arrays.asList("Merge", "List Helper", "Test");
        return new ArrayList<>(strings);
    }
}