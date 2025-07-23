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

    public void createViewsFileIfNotExists() {
        if (Files.exists(path) == false) {
            createDomainModel(path, tipoExpedienteFile);
        }
    }

    private void createDomainModel(Path path, TipoExpedienteInstanceFile tipoExpedienteFile) {
        Map<String, Object> context = new HashMap<>();
        context.put("states", tipoExpedienteFile.getStates());
        context.put("profiles", tipoExpedienteFile.getProfiles());
        context.put("newLine", "\n");
        context.put("tab", "\t");
        context.put("code", tipoExpedienteFile.getCode());
        context.put("name", tipoExpedienteFile.getName());

        String content = TemplateUtil.evaluateTemplate("views.template", context);

        TemplateUtil.createFileWithContent(path, content);
    }
}
