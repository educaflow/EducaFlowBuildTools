/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.stateeventvalidator;

import com.educaflow.common.buildtools.common.SpoonUtil;
import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.common.TextUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.google.common.base.CaseFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;

/**
 *
 * @author logongas
 */
public class StateEventValidatorFile {
    
    private final TipoExpedienteInstanceFile tipoExpedienteFile;
    private final Path path;

    public StateEventValidatorFile(Path path, TipoExpedienteInstanceFile tipoExpedienteFile) {
        this.path = path;
        this.tipoExpedienteFile = tipoExpedienteFile;

    }

    public void createStateEventValidatorFileIfNotExists() {
        if (Files.exists(path) == false) {
            createEventManagerFile(path, tipoExpedienteFile);
        }
    }

    
    public String check() {
        StringBuilder messagesFaltanMetodosBeanValidationRulesForStateAndEvent = new StringBuilder();
        StringBuilder messagesSobranMetodosBeanValidationRulesForStateAndEvent = new StringBuilder();


//        String modelFQCN = getModelFQCN();
//        CtModel ctModel = SpoonUtil.getCtModel(path);
//        
//        List<String> allEvents = new ArrayList<>(TextUtil.getUpperCamelCase(tipoExpedienteFile.getEvents()));  
//        Set<String> allMethodNamesTriggerEvent = allEvents.stream().map(event -> getMethodNameTriggerEvent(event)).collect(Collectors.toSet());
//        
//        
//        
//
//        
//        for (String event : allEvents) {
//                boolean hasMethod = existsTrigerEvent(ctModel, event, modelFQCN);
//
//            if (hasMethod==false) {
//                messagesFaltanMetodosEventos.append(getSourceCodeTriggerMethod(event));
//            }
//        }
//
//        for (CtMethod method : SpoonUtil.getMethods(ctModel, null,getFQCNWhenEventAnnotation(), null, false)) {
//            String methodName=method.getSimpleName();
//            if (allMethodNamesTriggerEvent.contains(methodName)==false) {
//                messagesSobranMetodosEventos.append("@WhenEvent "+method.getSimpleName()+"\n");
//            }
//
//        }

        



        StringBuilder messages = new StringBuilder();
        if (messagesFaltanMetodosBeanValidationRulesForStateAndEvent.length()>0) {
            messages.append("\nFaltan métodos de las validaciones:\n"+ messagesFaltanMetodosBeanValidationRulesForStateAndEvent.toString());
        }
        if (messagesSobranMetodosBeanValidationRulesForStateAndEvent.length()>0) {
            messages.append("\nSobran métodos de las validaciones:\n"+ messagesSobranMetodosBeanValidationRulesForStateAndEvent.toString());
        }

        if (messages.length()>0) {
            return "--------Fichero "+path.toAbsolutePath().toString()+"\n"+messages.toString()+"\n";
        } else {
            return null;
        }
    }
    
    


    public void checkStates() {
//        if (Files.exists(path) == false) {
//            throw new RuntimeException("No existe el fichero con la clase java:" + path);
//
//        }
//
//        String modelFQCN = getModelFQCN();
//
//        CtModel ctModel = SpoonUtil.getCtModel(path);
//
//        for (String state : TextUtil.getUpperCamelCase(tipoExpedienteFile.getStates())) {
//
//            boolean exists = existsOnEnterState(ctModel, state, modelFQCN);
//            if (exists == false) {
//                throw new RuntimeException("No existe el método:" + getMethodNameOnEnterEvent(state) + " en el fichero " + path+"\n"+getSourceCodeOnEnterMethod(state));
//            }
//
//        }

    }

    private void createEventManagerFile(Path path, TipoExpedienteInstanceFile tipoExpedienteFile) {
        Map<String, Object> context = new HashMap<>();
        context.put("states", tipoExpedienteFile.getStates());
        context.put("caseStates", TextUtil.getUpperCamelCase(tipoExpedienteFile.getStates()));
        context.put("events", tipoExpedienteFile.getEvents());
        context.put("caseEvents", TextUtil.getUpperCamelCase(tipoExpedienteFile.getEvents()));
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", TextUtil.caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", getPackageName(path.getParent()));

        String content = TemplateUtil.evaluateTemplate("state-event-validator.template", context);

        TemplateUtil.createFileWithContent(path, content);
    }

    private String getSourceCodeTriggerMethod(String event) {
        Map<String, Object> context = new HashMap<>();
        context.put("event", event);
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", TextUtil.caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", getPackageName(path.getParent()));

        String content = TemplateUtil.evaluateTemplate("event-manager-trigger-method.template", context);
        
        return content;
    }

    
    private String getSourceCodeOnEnterMethod(String state) {
        Map<String, Object> context = new HashMap<>();
        context.put("state", state);
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", TextUtil.caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", getPackageName(path.getParent()));

        String content = TemplateUtil.evaluateTemplate("event-manager-onenter-method.template", context);
        
        return content;
    }    
    




    private String getPackageName(Path filePath) {
        String pathString = filePath.toString();

        pathString = pathString.replace("\\", "/");

        int javaIndex = pathString.indexOf("/java/");
        if (javaIndex == -1) {
            if (pathString.endsWith("/java")) {
                javaIndex = pathString.length() - "/java".length();
            } else {
                return "";
            }
        }

        String packagePath = pathString.substring(javaIndex + "/java/".length());

        int dotIndex = packagePath.lastIndexOf(".");
        if (dotIndex != -1) {
            packagePath = packagePath.substring(0, dotIndex);
        }

        return packagePath.replace("/", ".");
    }

    
    private String getModelFQCN() {
        return "com.educaflow.shared.expedientes.db." + tipoExpedienteFile.getCode();
    }
    
    private boolean existsTrigerEvent(CtModel ctModel, String event, String modelFQCN) {
        String methodName = getMethodNameTriggerEvent(event);

        boolean exists = SpoonUtil.hasOnlyMethod(ctModel, methodName, getFQCNWhenEventAnnotation(), "void",true,modelFQCN, modelFQCN, "com.educaflow.shared.expedientes.EventContext");
        return exists;
    }
    
    
    private boolean existsOnEnterState(CtModel ctModel, String state, String modelFQCN) {
        String methodName = getMethodNameOnEnterEvent(state);

        boolean exists = SpoonUtil.hasOnlyMethod(ctModel, methodName, getFQCNOnEnterAnnotation(), "void",true, modelFQCN, "com.educaflow.shared.expedientes.EventContext");

        return exists;
    }   
    
    private String getMethodNameTriggerEvent(String event) {
        return "trigger" + event;
    }
    
    private String getMethodNameOnEnterEvent(String state) {
        return "onEnter" + state;
    }    
    
    private String getFQCNWhenEventAnnotation() {
        return "com.educaflow.shared.expedientes.annotations.WhenEvent";
    }
    
    
    private String getFQCNOnEnterAnnotation() {
        return "com.educaflow.shared.expedientes.annotations.OnEnterState";
    }
    
    

}
