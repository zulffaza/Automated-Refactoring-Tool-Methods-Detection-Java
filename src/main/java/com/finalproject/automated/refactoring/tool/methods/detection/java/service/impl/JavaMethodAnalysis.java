package com.finalproject.automated.refactoring.tool.methods.detection.java.service.impl;

import com.finalproject.automated.refactoring.tool.files.detection.model.FileModel;
import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodAttributesAnalysis;
import com.finalproject.automated.refactoring.tool.methods.detection.java.service.MethodBodyAnalysis;
import com.finalproject.automated.refactoring.tool.methods.detection.java.service.StartIndexAnalysis;
import com.finalproject.automated.refactoring.tool.methods.detection.model.IndexModel;
import com.finalproject.automated.refactoring.tool.methods.detection.service.MethodAnalysis;
import com.finalproject.automated.refactoring.tool.methods.detection.service.util.MethodsDetectionUtil;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 22 October 2018
 */

@Service
public class JavaMethodAnalysis implements MethodAnalysis {

    @Autowired
    private StartIndexAnalysis startIndexAnalysis;

    @Autowired
    private MethodAttributesAnalysis methodAttributesAnalysis;

    @Autowired
    private MethodBodyAnalysis methodBodyAnalysis;

    @Autowired
    private MethodsDetectionUtil methodsDetectionUtil;

    @Async
    @Override
    public Future analysis(FileModel fileModel, IndexModel indexModel, Map<String, List<MethodModel>> result) {
        MethodModel methodModel = MethodModel.builder().build();

        try {
            startIndexAnalysis.analysis(fileModel.getContent(), indexModel);
            methodAttributesAnalysis.analysis(fileModel, indexModel, methodModel);
            methodBodyAnalysis.analysis(fileModel.getContent(), indexModel, methodModel);
            saveResult(fileModel, methodModel, result);
        } catch (Exception e) {
            // Do nothing
            // Mark of non-method analysis
        }

        return null;
    }

    private void saveResult(FileModel fileModel, MethodModel methodModel,
                            Map<String, List<MethodModel>> result) {
        String key = methodsDetectionUtil.getMethodKey(fileModel);

        if (result.containsKey(key))
            result.get(key).add(methodModel);
        else {
            List<MethodModel> methodModels = new ArrayList<>();
            methodModels.add(methodModel);

            result.put(key, methodModels);
        }
    }
}
