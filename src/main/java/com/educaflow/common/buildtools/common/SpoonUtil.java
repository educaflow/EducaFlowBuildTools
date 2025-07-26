/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.common;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 *
 * @author logongas
 */
public class SpoonUtil {

    public static CtModel getCtModel(Path sourceCodeFile) {

        Launcher launcher = new Launcher();
        launcher.addInputResource(sourceCodeFile.toString());
        launcher.buildModel();
        CtModel ctModel = launcher.getModel();

        return ctModel;

    }

    public static List<CtMethod<?>> getMethods(CtModel ctModel, String methodName, String fqcnAnnotation, String fqcnReturnType, boolean checkParams, String... fqcnParams) {
        List<CtMethod<?>> methods = new ArrayList<>();

        for (CtMethod<?> method : ctModel.getElements(new TypeFilter<>(CtMethod.class))) {
            boolean meetsMethodNameCriteria;
            boolean meetsAnnotationCriteria;
            boolean meetsReturnTypeCriteria;
            boolean meetsArgumentsCriteria;

            if (methodName != null) {
                if (method.getSimpleName().equals(methodName) == true) {
                    meetsMethodNameCriteria = true;
                } else {
                    meetsMethodNameCriteria = false;
                }
            } else {
                meetsMethodNameCriteria = true;
            }

            if (fqcnAnnotation != null) {
                meetsAnnotationCriteria = hasAnnotation(method,fqcnAnnotation);
            } else {
                meetsAnnotationCriteria = true;
            }

            if (fqcnReturnType != null) {
                CtTypeReference type=method.getType();
                
                if (type==null ) {
                    meetsReturnTypeCriteria = false;
                } else {
                    String returnTypeName = type.getQualifiedName();
                    if (fqcnReturnType.equals(returnTypeName)) {
                        meetsReturnTypeCriteria = true;
                    } else {
                        meetsReturnTypeCriteria = false;
                    }
                }
            } else {
                meetsReturnTypeCriteria = true;
            }

            if (checkParams == true) {
                if (method.getParameters().size() == fqcnParams.length) {

                    if (fqcnParams.length == 0) {
                        meetsArgumentsCriteria = true;
                    } else {
                        meetsArgumentsCriteria = true;
                        for (int i = 0; i < fqcnParams.length; i++) {
                            CtParameter<?> param = method.getParameters().get(i);
                            String fqcnParam = fqcnParams[i];
                            if (param.getType().getQualifiedName().equals(fqcnParam) == false) {
                                meetsArgumentsCriteria = false;
                            }
                        }
                    }

                } else {
                    meetsArgumentsCriteria = false;
                }
            } else {
                meetsArgumentsCriteria = true;
            }

            if (meetsMethodNameCriteria && meetsAnnotationCriteria && meetsReturnTypeCriteria && meetsArgumentsCriteria) {
                methods.add(method);
            }
        }

        return methods;
    }

    public static boolean hasOnlyMethod(CtModel ctModel, String methodName, String fqcnAnnotation, String fqcnReturnType, boolean checkParams, String... fqcnParams) {
        List<CtMethod<?>> methods = getMethods(ctModel, methodName, fqcnAnnotation, fqcnReturnType, checkParams, fqcnParams);

        if (methods.size() == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasAnnotation(CtMethod<?> method,String fqcnAnnotation) {
        boolean meetsAnnotationCriteria = false;
        for (CtAnnotation<?> annotation : method.getAnnotations()) {
            if (annotation.getAnnotationType() != null && annotation.getAnnotationType().getQualifiedName().equals(fqcnAnnotation)) {
                meetsAnnotationCriteria = true;
            }
        }

        return meetsAnnotationCriteria;
    }

}
