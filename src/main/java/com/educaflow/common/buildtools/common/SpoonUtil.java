/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.common;

import java.nio.file.Path;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
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

    public static boolean existsMethod(CtModel ctModel,String methodName,String fqcnAnnotation,String...fqcnParams) {



        boolean methodFound = false;

        for (CtMethod<?> method : ctModel.getElements(new TypeFilter<>(CtMethod.class))) {

            if (method.getSimpleName().equals(methodName)==false) {
                continue;
            }

            if (fqcnAnnotation!=null) {
                boolean hasAnnotation = false;
                for (CtAnnotation<?> annotation : method.getAnnotations()) {
                    if (annotation.getAnnotationType() != null && annotation.getAnnotationType().getQualifiedName().equals(fqcnAnnotation)) {
                        hasAnnotation = true;
                        break;
                    }
                }

                if (hasAnnotation==false) {
                    continue;
                }
            }

            

            if (method.getParameters().size() == fqcnParams.length) {
                
                if (fqcnParams.length==0) {
                    methodFound = true;
                    break;
                } else {
                    for (int i=0;i<fqcnParams.length;i++) {
                        CtParameter<?> param=method.getParameters().get(i);
                        String fqcnParam=fqcnParams[i];
                        if (param.getType() != null && param.getType().getQualifiedName().equals(fqcnParam)) {
                            methodFound = true;
                            break;
                        }
                    }
                }

            } else {
                continue;
            }
        }

        return methodFound;

    }

}
