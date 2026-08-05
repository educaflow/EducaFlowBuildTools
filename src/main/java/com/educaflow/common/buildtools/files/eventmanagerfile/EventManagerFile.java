/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.eventmanagerfile;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.common.TextUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Generador del fichero fuente del {@code EventManager} de un tipo de expediente.
 *
 * <p>Esta clase <b>solo genera</b>. La comprobación de que el {@code EventManager} escrito a mano
 * concuerda con la máquina de estados del XML vive en los tests de {@code secretaria-virtual}
 * ({@code src/test/java/com/educaflow/tiposexpedientes}), que leen bytecode en vez de código fuente.
 * Esos tests reutilizan de aquí el convenio de nombres ({@link #getMethodNameTriggerEvent},
 * {@link #getMethodNameOnEnterEvent}, {@link #getModelFQCN}) y los renderizadores de código fuente
 * ({@link #getSourceCodeTriggerMethod}, {@link #getSourceCodeOnEnterMethod}), para que el método que
 * el test dice que falta sea literalmente el que este generador habría escrito.
 *
 * @author logongas
 */
public class EventManagerFile {

    private final TipoExpedienteInstanceFile tipoExpedienteFile;
    private final Path path;

    public EventManagerFile(Path path, TipoExpedienteInstanceFile tipoExpedienteFile) {
        this.path = path;
        this.tipoExpedienteFile = tipoExpedienteFile;

    }

    /**
     * Crea el fichero solo si no existe (nunca pisa fuentes editadas a mano).
     *
     * @return true si lo ha creado, false si ya existía.
     */
    public boolean createEventManagerFileIfNotExists() {
        if (Files.exists(path) == false) {
            createEventManagerFile(path, tipoExpedienteFile);
            return true;
        }
        return false;
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
        context.put("eventManagerClassName", tipoExpedienteFile.getEventManagerClassName());

        context.put("profiles", tipoExpedienteFile.getProfiles());
        context.put("tipoDocumentosPdf", tipoExpedienteFile.getTipoDocumentosPdf());        
        
        String content = TemplateUtil.evaluateTemplate("event-manager.template", context);

        TemplateUtil.createFileWithContent(path, content);
    }

    /**
     * Código fuente del método {@code trigger<Evento>}, tal cual lo escribiría el generador.
     *
     * @param event nombre del evento en UpperCamelCase (p.ej. {@code PresentarDocumentosFirmados}).
     */
    public String getSourceCodeTriggerMethod(String event) {
        Map<String, Object> context = new HashMap<>();
        context.put("event", event);
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", TextUtil.caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", getPackageName(path.getParent()));
        context.put("stateEventValidatorClassName", tipoExpedienteFile.getStateEventValidatorClassName());
        
        String content = TemplateUtil.evaluateTemplate("event-manager-trigger-method.template", context);
        
        return content;
    }

    
    /**
     * Código fuente del método {@code onEnter<Estado>}, tal cual lo escribiría el generador.
     *
     * @param state nombre del estado en UpperCamelCase (p.ej. {@code PendienteResolucion}).
     */
    public String getSourceCodeOnEnterMethod(String state) {
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

    /** FQCN de la entidad del tipo de expediente, que es el tipo de los parámetros del modelo. */
    public String getModelFQCN() {
        return "com.educaflow.subsystem.expedientes.db." + tipoExpedienteFile.getCode();
    }

    /** Convenio de nombre del método de un evento: {@code trigger<Evento>}. */
    public static String getMethodNameTriggerEvent(String event) {
        return "trigger" + event;
    }

    /** Convenio de nombre del método de un estado: {@code onEnter<Estado>}. */
    public static String getMethodNameOnEnterEvent(String state) {
        return "onEnter" + state;
    }

}
