package com.finalproject.automated.refactoring.tool.methods.detection.java.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 24 Mei 2019
 */

@Data
@Builder
public class NormalizeGenericsVA {

    @Builder.Default
    private List<Integer> mergeIndex = new ArrayList<>();

    @Builder.Default
    private Stack<String> stack = new Stack<>();

    @Builder.Default
    private AtomicBoolean isGenerics = new AtomicBoolean();

    private String variable;

    private String nextVariable;
}
