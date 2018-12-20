package com.finalproject.automated.refactoring.tool.methods.detection.java.service;

import com.finalproject.automated.refactoring.tool.files.detection.model.FileModel;
import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import lombok.NonNull;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 12 December 2018
 */

public interface MethodAttributesAnalysis {

    void analysis(@NonNull FileModel fileModel, @NonNull IndexModel indexModel,
                  @NonNull MethodModel methodModel);
}
