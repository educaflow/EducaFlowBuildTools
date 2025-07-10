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
import spoon.reflect.CtModel;

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
    
    public void checkEvents() {
        if (Files.exists(path) == false) {
            throw new RuntimeException("No existe el fichero con la clase java:"+path);

        }
        
        
        String modelFQCN="com.educaflow.apps.expedientes.db."+tipoExpedienteFile.getCode();
        
        CtModel ctModel=SpoonUtil.getCtModel(path);
        
        for(String event:getUpperCamelCase(tipoExpedienteFile.getEvents())) {
            String methodName="trigger"+event;
            
            boolean exists=SpoonUtil.existsMethod(ctModel, methodName, "com.educaflow.apps.expedientes.common.annotations.WhenEvent", modelFQCN,modelFQCN,"com.educaflow.apps.expedientes.common.EventContext");
            
            if (exists==false) {
                throw new RuntimeException("No existe el método:"+methodName +" en el fichero " + path );
            }
            
        }
        
        
    }   
    
    public void checkStates() {
        if (Files.exists(path) == false) {
            throw new RuntimeException("No existe el fichero con la clase java:"+path);

        }
        
        
        String modelFQCN="com.educaflow.apps.expedientes.db."+tipoExpedienteFile.getCode();
        
        CtModel ctModel=SpoonUtil.getCtModel(path);
        
        for(String state:getUpperCamelCase(tipoExpedienteFile.getStates())) {
            String methodName="onEnter"+state;
            
            boolean exists=SpoonUtil.existsMethod(ctModel, methodName, "com.educaflow.apps.expedientes.common.annotations.OnEnterState", modelFQCN,"com.educaflow.apps.expedientes.common.EventContext");
            
            if (exists==false) {
                throw new RuntimeException("No existe el método:"+methodName +" en el fichero " + path );
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
    
    
    private static String caseLowerFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    } 
    
    private List<String> getUpperCamelCase(List<? extends Object> list) {
        List<String> upperCamelCaseList=new ArrayList<>();
        
        for (Object obj : list) {
            upperCamelCaseList.add(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,obj+""));
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
    
    
    
    
    
}
