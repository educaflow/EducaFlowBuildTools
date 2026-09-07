/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.phaseeventmanagerfile;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.Fase;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.common.TextUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Generador del fichero fuente del {@code PhaseEventManager} de <b>una fase</b> de un tipo de
 * expediente, que vive en {@code <vN>/<fase en minúsculas>/PhaseEventManagerImpl.java}.
 *
 * <p>Esta clase <b>solo genera</b>. La comprobación de que el {@code PhaseEventManager} escrito a mano
 * concuerda con la máquina de estados del XML vive en los tests de {@code secretaria-virtual}
 * ({@code src/test/java/com/educaflow/tiposexpedientes}), que leen bytecode en vez de código fuente.
 * Esos tests reutilizan de aquí el convenio de nombres ({@link #getMethodNameTriggerEvent},
 * {@link #getMethodNameOnEnterEvent}, {@link #getModelFQCN}) y los renderizadores de código fuente
 * ({@link #getSourceCodeTriggerMethod}, {@link #getSourceCodeOnEnterMethod}), para que el método que
 * el test dice que falta sea literalmente el que este generador habría escrito.
 *
 * @author logongas
 */
public class PhaseEventManagerFile {

    private final Fase fase;
    private final TipoExpedienteInstanceFile tipoExpedienteFile;
    private final Path path;

    public PhaseEventManagerFile(Path path, Fase fase) {
        this.path = path;
        this.fase = fase;
        this.tipoExpedienteFile = fase.getTipoExpediente();

    }

    /**
     * Crea el fichero solo si no existe (nunca pisa fuentes editadas a mano).
     *
     * @return true si lo ha creado, false si ya existía.
     */
    public boolean createPhaseEventManagerFileIfNotExists() {
        if (Files.exists(path) == false) {
            createPhaseEventManagerFile(path);
            return true;
        }
        return false;
    }

    /**
     * Los <b>métodos</b> que lleva la clase son solo los de esta fase (de ahí {@code caseStates} y
     * {@code caseEvents}), porque cada {@code PhaseEventManager} solo atiende los estados de su propia
     * fase. Los estados a los que puede saltar, en cambio, son todos los del tipo, y no hacen falta
     * aquí: viven en la clase {@code States} que genera el build, de la que basta con importar el
     * nombre ({@code basePackageName}).
     *
     * <p>El <b>evento inicial</b> no sale de aquí: no es de ninguna fase, sino del tipo de
     * expediente entero, y lo genera {@code InitialEventManagerFile} en la raíz de la versión.
     */
    private void createPhaseEventManagerFile(Path path) {
        Map<String, Object> context = new HashMap<>();
        context.put("caseStates", TextUtil.getUpperCamelCase(fase.getStates()));
        context.put("caseEvents", TextUtil.getUpperCamelCase(fase.getEvents()));
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", TextUtil.caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", fase.getPackageName());
        context.put("basePackageName", tipoExpedienteFile.getBasePackageName());
        context.put("faseUpperCamelCase", fase.getNameUpperCamelCase());
        context.put("phaseEventManagerClassName", tipoExpedienteFile.getPhaseEventManagerClassName());

        context.put("tipoDocumentosPdf", tipoExpedienteFile.getTipoDocumentosPdf());

        String content = TemplateUtil.evaluateTemplate("phase-event-manager.template", context);

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
        context.put("packageName", fase.getPackageName());
        context.put("faseUpperCamelCase", fase.getNameUpperCamelCase());
        context.put("stateEventValidatorClassName", tipoExpedienteFile.getStateEventValidatorClassName());

        String content = TemplateUtil.evaluateTemplate("phase-event-manager-trigger-method.template", context);
        
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
        context.put("packageName", fase.getPackageName());

        String content = TemplateUtil.evaluateTemplate("phase-event-manager-onenter-method.template", context);
        
        return content;
    }    
    




    /** FQCN de la entidad del tipo de expediente, que es el tipo de los parámetros del modelo. */
    public String getModelFQCN() {
        return tipoExpedienteFile.getFqcnExpediente();
    }

    public Fase getFase() {
        return fase;
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
