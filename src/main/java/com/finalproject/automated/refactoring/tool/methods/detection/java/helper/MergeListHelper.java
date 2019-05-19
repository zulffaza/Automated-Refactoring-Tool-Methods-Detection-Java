package com.finalproject.automated.refactoring.tool.methods.detection.java.helper;

import java.util.List;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 19
 */

public class MergeListHelper {

    private static final Integer FIRST_INDEX = 0;
    private static final Integer SECOND_INDEX = 1;

    public static void mergeListOfString(List<String> listToMerge,
                                         List<Integer> mergeIndex,
                                         String delimiter) {
        Integer maxSize = mergeIndex.size() - SECOND_INDEX;

        for (Integer index = FIRST_INDEX; index < maxSize; index++) {
            Integer startPoint = mergeIndex.get(index);
            Integer endPoint = mergeIndex.get(++index);

            listToMerge.set(startPoint, String.join(delimiter, listToMerge.subList(startPoint, ++endPoint)));
        }

        for (Integer index = maxSize; index > FIRST_INDEX; index--) {
            Integer endPoint = mergeIndex.get(index);
            Integer startPoint = mergeIndex.get(--index) + SECOND_INDEX;

            for (Integer count = startPoint; count <= endPoint; count++) {
                listToMerge.remove(startPoint.intValue());
            }
        }
    }
}
