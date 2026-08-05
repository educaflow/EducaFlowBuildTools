package com.educaflow.common.buildtools.createdatainittramite;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.files.tramite.TramiteInstanceFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Los ficheros de data-init de un trámite:
 * <ul>
 * <li>&lt;code&gt;/definicion/data-init (priority="1"): el propio trámite.</li>
 * <li>&lt;code&gt;/tipo_expediente_activo/data-init (priority="-1"): su tipo de
 * expediente activo, solo si el trámite lo declara.</li>
 * </ul>
 *
 * @author logongas
 */
public class InitDataTramiteFiles {

    private TramiteInstanceFile tramiteInstanceFile;
    private Path rootPath;

    public InitDataTramiteFiles(TramiteInstanceFile tramiteInstanceFile,Path rootPath) {
        this.tramiteInstanceFile=tramiteInstanceFile;
        this.rootPath=rootPath;
    }


    public void generateInitData() {
        try {
            Path dataInitPath=rootPath.resolve(tramiteInstanceFile.getCode()).resolve("definicion").resolve("data-init");
            Files.createDirectories(dataInitPath);
            createFile(dataInitPath.resolve("input-config.xml"),"input-config-tramites-definicion.template");

            Path inputPath=dataInitPath.resolve("input");
            Files.createDirectories(inputPath);
            createFile(inputPath.resolve("Tramite.xml"),"input-config-tramites-definicion-input-data.template");

            if (tramiteInstanceFile.getDefaultTipoExpedienteCode()!=null) {
                Path dataInitActivoPath=rootPath.resolve(tramiteInstanceFile.getCode()).resolve("tipo_expediente_activo").resolve("data-init");
                Files.createDirectories(dataInitActivoPath);
                createFile(dataInitActivoPath.resolve("input-config.xml"),"input-config-tramites-tipo-expediente-activo.template");

                Path inputActivoPath=dataInitActivoPath.resolve("input");
                Files.createDirectories(inputActivoPath);
                createFile(inputActivoPath.resolve("TipoExpedienteActivo.xml"),"input-config-tramites-tipo-expediente-activo-input-data.template");
            }

        } catch(Exception ex) {
            throw new RuntimeException("Fallo al generar los data-init del trámite:"+tramiteInstanceFile.getPath(),ex);
        }
    }


    private void createFile(Path path,String templateName) {
        Map<String, Object> context = new HashMap<>();
        context.put("tramite", tramiteInstanceFile);

        String content = TemplateUtil.evaluateTemplate(templateName, context);

        TemplateUtil.createFileWithContent(path, content);
    }

}
