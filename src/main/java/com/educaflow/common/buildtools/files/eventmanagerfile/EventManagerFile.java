/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.eventmanagerfile;

import com.educaflow.common.buildtools.common.SpoonUtil;
import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteFile;
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
public class EventManagerFile {

    private final TipoExpedienteFile tipoExpedienteFile;
    private final Path path;

    public EventManagerFile(Path path, TipoExpedienteFile tipoExpedienteFile) {
        this.path = path;
        this.tipoExpedienteFile = tipoExpedienteFile;

    }

    public void createEventManagerFileIfNotExists() {
        if (Files.exists(path) == false) {
            createEventManagerFile(path, tipoExpedienteFile);
        }
    }

    
    public String check() {
        StringBuilder messagesFaltanMetodosEventos = new StringBuilder();
        StringBuilder messagesSobranMetodosEventos = new StringBuilder();
        StringBuilder messagesFaltanMetodosEstados = new StringBuilder();
        StringBuilder messagesSobranMetodosEstados = new StringBuilder();

        String modelFQCN = getModelFQCN();
        CtModel ctModel = SpoonUtil.getCtModel(path);
        
        List<String> allEvents = new ArrayList<>(getUpperCamelCase(tipoExpedienteFile.getEvents()));  
        Set<String> allMethodNamesTriggerEvent = allEvents.stream().map(event -> getMethodNameTriggerEvent(event)).collect(Collectors.toSet());
        
        
        

        
        for (String event : allEvents) {
                boolean hasMethod = existsTrigerEvent(ctModel, event, modelFQCN);

            if (hasMethod==false) {
                messagesFaltanMetodosEventos.append(getSourceCodeTriggerMethod(event));
            }
        }

        for (CtMethod method : SpoonUtil.getMethods(ctModel, null,getFQCNWhenEventAnnotation(), null, false)) {
            String methodName=method.getSimpleName();
            if (allMethodNamesTriggerEvent.contains(methodName)==false) {
                messagesSobranMetodosEventos.append("@WhenEvent "+method.getSimpleName()+"\n");
            }

        }

        List<String> allStates=getUpperCamelCase(tipoExpedienteFile.getStates());
        Set<String> allMethodNamesOnEnter = allStates.stream().map(state -> getMethodNameOnEnterEvent(state)).collect(Collectors.toSet());
        for (String state : allStates) {
                boolean hasMethod = existsOnEnterState(ctModel, state, modelFQCN);

            if (hasMethod==false) {
                messagesFaltanMetodosEstados.append(getSourceCodeOnEnterMethod(state));
            }
        }

        for (CtMethod method : SpoonUtil.getMethods(ctModel, null,getFQCNOnEnterAnnotation(), null, false)) {
            
            if (allMethodNamesOnEnter.contains(method.getSimpleName())==false) {
                messagesSobranMetodosEstados.append("@OnEnter "+method.getSimpleName()+"\n");
            }

        }
        
        



        StringBuilder messages = new StringBuilder();
        if (messagesFaltanMetodosEventos.length()>0) {
            messages.append("\nFaltan métodos para los eventos:\n"+ messagesFaltanMetodosEventos.toString());
        }
        if (messagesSobranMetodosEventos.length()>0) {
            messages.append("\nSobran métodos para los eventos:\n"+ messagesSobranMetodosEventos.toString());
        }
        if (messagesFaltanMetodosEstados.length()>0) {
            messages.append("\nFaltan métodos para los estados:\n"+ messagesFaltanMetodosEstados.toString());
        }
        if (messagesSobranMetodosEstados.length()>0) {
            messages.append("\nSobran métodos para los estados:\n"+ messagesSobranMetodosEstados.toString());
        }

        if (messages.length()>0) {
            return "--------Fichero "+path.toAbsolutePath().toString()+"\n"+messages.toString()+"\n";
        } else {
            return null;
        }
    }
    
    


    public void checkStates() {
        if (Files.exists(path) == false) {
            throw new RuntimeException("No existe el fichero con la clase java:" + path);

        }

        String modelFQCN = getModelFQCN();

        CtModel ctModel = SpoonUtil.getCtModel(path);

        for (String state : getUpperCamelCase(tipoExpedienteFile.getStates())) {

            boolean exists = existsOnEnterState(ctModel, state, modelFQCN);
            if (exists == false) {
                throw new RuntimeException("No existe el método:" + getMethodNameOnEnterEvent(state) + " en el fichero " + path+"\n"+getSourceCodeOnEnterMethod(state));
            }

        }

    }

    private void createEventManagerFile(Path path, TipoExpedienteFile tipoExpedienteFile) {
        Map<String, Object> context = new HashMap<>();
        context.put("states", tipoExpedienteFile.getStates());
        context.put("caseStates", getUpperCamelCase(tipoExpedienteFile.getStates()));
        context.put("events", tipoExpedienteFile.getEvents());
        context.put("caseEvents", getUpperCamelCase(tipoExpedienteFile.getEvents()));
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", getPackageName(path.getParent()));

        String content = TemplateUtil.evaluateTemplate("event-manager.template", context);

        TemplateUtil.createFileWithContent(path, content);
    }

    private String getSourceCodeTriggerMethod(String event) {
        Map<String, Object> context = new HashMap<>();
        context.put("event", event);
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", caseLowerFirstLetter(tipoExpedienteFile.getCode()));
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
        context.put("lowerCode", caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", getPackageName(path.getParent()));

        String content = TemplateUtil.evaluateTemplate("event-manager-onenter-method.template", context);
        
        return content;
    }    
    


    private static String caseLowerFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    private List<String> getUpperCamelCase(List<? extends Object> list) {
        List<String> upperCamelCaseList = new ArrayList<>();

        for (Object obj : list) {
            upperCamelCaseList.add(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, obj + ""));
        }

        return upperCamelCaseList;

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
        return "com.educaflow.apps.expedientes.db." + tipoExpedienteFile.getCode();
    }
    
    private boolean existsTrigerEvent(CtModel ctModel, String event, String modelFQCN) {
        String methodName = getMethodNameTriggerEvent(event);

        boolean exists = SpoonUtil.hasOnlyMethod(ctModel, methodName, getFQCNWhenEventAnnotation(), "void",true,modelFQCN, modelFQCN, "com.educaflow.apps.expedientes.common.EventContext");
        return exists;
    }
    
    
    private boolean existsOnEnterState(CtModel ctModel, String state, String modelFQCN) {
        String methodName = getMethodNameOnEnterEvent(state);

        boolean exists = SpoonUtil.hasOnlyMethod(ctModel, methodName, getFQCNOnEnterAnnotation(), "void",true, modelFQCN, "com.educaflow.apps.expedientes.common.EventContext");

        return exists;
    }   
    
    private String getMethodNameTriggerEvent(String event) {
        return "trigger" + event;
    }
    
    private String getMethodNameOnEnterEvent(String state) {
        return "onEnter" + state;
    }    
    
    private String getFQCNWhenEventAnnotation() {
        return "com.educaflow.apps.expedientes.common.annotations.WhenEvent";
    }
    
    
    private String getFQCNOnEnterAnnotation() {
        return "com.educaflow.apps.expedientes.common.annotations.OnEnterState";
    }
    
    

}
