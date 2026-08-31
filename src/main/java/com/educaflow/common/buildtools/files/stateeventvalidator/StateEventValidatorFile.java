/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.stateeventvalidator;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.common.TextUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.Fase;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Generador del fichero fuente Kotlin del {@code StateEventValidator} de <b>una fase</b> de un tipo
 * de expediente, que vive en {@code <vN>/<fase en minúsculas>/StateEventValidatorImpl.kt}.
 *
 * <p>Esta clase <b>solo genera</b>. La comprobación de que el validator escrito a mano tiene un
 * método por cada pareja (estado, evento) vive en los tests de {@code secretaria-virtual}
 * ({@code src/test/java/com/educaflow/tiposexpedientes}), que leen bytecode: por eso ahora sí
 * alcanza a un fichero Kotlin, cosa que la validación anterior basada en Spoon nunca pudo hacer.
 * Esos tests reutilizan de aquí el convenio de nombres
 * ({@link #getMethodNameBeanValidationRules}) y el renderizador de código fuente
 * ({@link #getSourceCodeBeanValidationRulesMethod}).
 *
 * @author logongas
 */
public class StateEventValidatorFile {

    /**
     * Evento, en UpperCamelCase, para el que <b>no</b> se genera método de reglas: {@code Tramitador}
     * excluye {@code DELETE} de la validación y borra sin copiar campos, así que su método nunca se
     * invocaría y solo podría contener un {@code rules { }} vacío.
     */
    private static final String EVENTO_SIN_VALIDACION = "Delete";

    private final Fase fase;
    private final TipoExpedienteInstanceFile tipoExpedienteFile;
    private final Path path;

    public StateEventValidatorFile(Path path, Fase fase) {
        this.path = path;
        this.fase = fase;
        this.tipoExpedienteFile = fase.getTipoExpediente();

    }

    /**
     * Crea el fichero solo si no existe (nunca pisa fuentes editadas a mano).
     *
     * @return true si lo ha creado, false si ya existía.
     */
    public boolean createStateEventValidatorFileIfNotExists() {
        if (Files.exists(path) == false) {
            createStateEventValidatorFile(path);
            return true;
        }
        return false;
    }

    /**
     * Solo los estados de <b>esta</b> fase: cada validator atiende las parejas (estado, evento) de
     * su propia fase, igual que el {@code PhaseEventManager}.
     */
    private void createStateEventValidatorFile(Path path) {
        Map<String, Object> context = new HashMap<>();
        context.put("states", fase.getStates());
        context.put("caseStates", TextUtil.getUpperCamelCase(fase.getStates()));
        context.put("events", fase.getEvents());
        context.put("caseEvents", TextUtil.getUpperCamelCase(fase.getEvents()));
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", TextUtil.caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", fase.getPackageName());
        context.put("stateEventValidatorClassName", tipoExpedienteFile.getStateEventValidatorClassName());
        context.put("eventoSinValidacion", EVENTO_SIN_VALIDACION);

        String content = TemplateUtil.evaluateTemplate("state-event-validator.template", context);

        TemplateUtil.createFileWithContent(path, content);
    }

    /**
     * Código fuente Kotlin del método de validación de una pareja (estado, evento), tal cual lo
     * escribiría el generador. Renderiza la misma sub-plantilla que incluye
     * {@code state-event-validator.template}, así que el snippet que un test ofrece para pegar es
     * literalmente el que este generador habría escrito.
     *
     * @param state nombre del estado en UpperCamelCase (p.ej. {@code EntradaDatos}).
     * @param event nombre del evento en UpperCamelCase (p.ej. {@code GuardarDatos}).
     */
    public String getSourceCodeBeanValidationRulesMethod(String state, String event) {
        Map<String, Object> context = new HashMap<>();
        context.put("stateUpperCamelCase", state);
        context.put("eventUpperCamelCase", event);
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", TextUtil.caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", fase.getPackageName());

        String content = TemplateUtil.evaluateTemplate("state-event-validator-method.template", context);

        return content;
    }

    public Fase getFase() {
        return fase;
    }

    /**
     * Convenio de nombre del método de validación de una pareja (estado, evento):
     * {@code getForState<Estado>InEvent<Evento>}. Es el mismo nombre que construye a mano
     * {@code Tramitador.getBeansValidationRules} para buscarlo por reflexión en runtime.
     *
     * @param state nombre del estado en UpperCamelCase.
     * @param event nombre del evento en UpperCamelCase.
     */
    public static String getMethodNameBeanValidationRules(String state, String event) {
        return "getForState" + state + "InEvent" + event;
    }

}
