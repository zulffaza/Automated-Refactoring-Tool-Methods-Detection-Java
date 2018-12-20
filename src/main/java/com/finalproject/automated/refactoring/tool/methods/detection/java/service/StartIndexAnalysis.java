package com.finalproject.automated.refactoring.tool.methods.detection.java.service;

import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import lombok.NonNull;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 9 December 2018
 */

public interface StartIndexAnalysis {

    void analysis(@NonNull String content, @NonNull IndexModel indexModel);
}
