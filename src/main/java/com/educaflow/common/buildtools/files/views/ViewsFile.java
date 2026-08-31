/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.views;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Generador del {@code views.xml} de la <b>raíz de la versión</b> de un tipo de expediente, que
 * contiene únicamente el form de plantilla {@code exp-<Code>-Templates}: el catálogo de paneles del
 * que tiran los {@code <include-panels>} de todas las fases.
 *
 * <p>Vive en la raíz y no en cada fase porque los paneles se comparten entre fases (los datos del
 * interesado, el visor del PDF de la solicitud…) y duplicarlos en cada subcarpeta obligaría a
 * mantenerlos sincronizados a mano. Los {@code <form state="...">} sí van repartidos, uno por fase,
 * y los genera {@link ViewsFaseFile}.
 *
 * @author logongas
 */
public class ViewsFile {

    private final TipoExpedienteInstanceFile tipoExpedienteFile;
    private final Path path;

    public ViewsFile(Path path, TipoExpedienteInstanceFile tipoExpedienteFile) {
        this.path = path;
        this.tipoExpedienteFile = tipoExpedienteFile;

    }

    /**
     * Crea el fichero solo si no existe (nunca pisa fuentes editadas a mano).
     *
     * @return true si lo ha creado, false si ya existía.
     */
    public boolean createViewsFileIfNotExists() {
        if (Files.exists(path) == false) {
            createViewsFile(path, tipoExpedienteFile);
            return true;
        }
        return false;
    }

    public Path getPath() {
        return path;
    }

    private void createViewsFile(Path path, TipoExpedienteInstanceFile tipoExpedienteFile) {
        Map<String, Object> context = new HashMap<>();
        context.put("code", tipoExpedienteFile.getCode());
        context.put("name", tipoExpedienteFile.getName());

        String content = TemplateUtil.evaluateTemplate("views-templates.template", context);

        TemplateUtil.createFileWithContent(path, content);
    }
}
