package com.educaflow.common.buildtools.files.initialeventmanagerfile;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.common.TextUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Generador del fichero fuente del {@code InitialEventManager} de un tipo de expediente, que vive
 * en la <b>raíz de la versión</b>, {@code <vN>/InitialEventManagerImpl.java}.
 *
 * <p>Va en la raíz y no en una fase porque el evento inicial es del <b>tipo de expediente</b>: se
 * dispara cuando todavía no hay estado del que partir, así que no pertenece a ninguna fase. Hay
 * exactamente uno por tipo, mientras que del {@code PhaseEventManager} hay uno por fase
 * ({@code PhaseEventManagerFile}).
 *
 * <p>Esta clase <b>solo genera</b>. La comprobación de que existe y de que su método tiene la firma
 * correcta vive en los tests de {@code secretaria-virtual}
 * ({@code src/test/java/com/educaflow/tiposexpedientes}), que reutilizan de aquí el convenio de
 * nombres ({@link #getMethodNameTriggerInitialEvent}, {@link #getModelFQCN}).
 *
 * @author logongas
 */
public class InitialEventManagerFile {

    private final TipoExpedienteInstanceFile tipoExpedienteFile;
    private final Path path;

    public InitialEventManagerFile(Path path, TipoExpedienteInstanceFile tipoExpedienteFile) {
        this.path = path;
        this.tipoExpedienteFile = tipoExpedienteFile;
    }

    /**
     * Crea el fichero solo si no existe (nunca pisa fuentes editadas a mano).
     *
     * @return true si lo ha creado, false si ya existía.
     */
    public boolean createInitialEventManagerFileIfNotExists() {
        if (Files.exists(path) == false) {
            createInitialEventManagerFile(path);
            return true;
        }
        return false;
    }

    private void createInitialEventManagerFile(Path path) {
        Map<String, Object> context = new HashMap<>();
        context.put("code", tipoExpedienteFile.getCode());
        context.put("lowerCode", TextUtil.caseLowerFirstLetter(tipoExpedienteFile.getCode()));
        context.put("packageName", tipoExpedienteFile.getBasePackageName());
        context.put("initialEventManagerClassName", tipoExpedienteFile.getInitialEventManagerClassName());

        String content = TemplateUtil.evaluateTemplate("initial-event-manager.template", context);

        TemplateUtil.createFileWithContent(path, content);
    }

    public Path getPath() {
        return path;
    }

    /** FQCN de la entidad del tipo de expediente, que es el tipo del parámetro del modelo. */
    public String getModelFQCN() {
        return "com.educaflow.subsystem.expedientes.db." + tipoExpedienteFile.getCode();
    }

    /** El nombre del único método del {@code InitialEventManager}. */
    public static String getMethodNameTriggerInitialEvent() {
        return "triggerInitialEvent";
    }

}
