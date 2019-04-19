package com.finalproject.automated.refactoring.tool.methods.detection.java.service;

import com.finalproject.automated.refactoring.tool.model.MethodModel;
import lombok.NonNull;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 13 April 2019
 */

public interface MethodStatementAnalysis {

    void analysis(@NonNull MethodModel methodModel);
}
