/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.domainmodel;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author logongas
 */
public class DomainModelFile {
    
    private final TipoExpedienteInstanceFile tipoExpedienteFile;
    private final Path path;
    
    public DomainModelFile(Path path,TipoExpedienteInstanceFile tipoExpedienteFile) {
        this.path = path;
        this.tipoExpedienteFile = tipoExpedienteFile;
        
        

        
    }
    
    public void createDomainModelIfNotExists() {
        if (Files.exists(path)==false) {
            createDomainModel(path,tipoExpedienteFile.getCode());
        }
    }
    

    private void createDomainModel(Path path, String name) {
        Map<String, Object> context = new HashMap<>();
        context.put("name", name);

        String content=TemplateUtil.evaluateTemplate("domain-model.template", context);
        
        TemplateUtil.createFileWithContent(path, content);
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }
    
}
