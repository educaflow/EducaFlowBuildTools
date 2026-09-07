/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.createdatainittipoexpediente;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.common.TextUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author logongas
 */
public class InitDataTipoExpedienteFiles {

    private TipoExpedienteInstanceFile tipoExpedienteInstanceFile;
    private Path rootPath;
    
    public InitDataTipoExpedienteFiles(TipoExpedienteInstanceFile tipoExpedienteInstanceFile,Path rootPath) {
        this.tipoExpedienteInstanceFile=tipoExpedienteInstanceFile;
        this.rootPath=rootPath;
    }
 
    
    public void generateInitData() {
        try {
            Path dataInitPath=rootPath.resolve(tipoExpedienteInstanceFile.getCode()).resolve("data-init");
            Files.createDirectories(dataInitPath);
            createInputConfigFile(dataInitPath);
            
            Path inputPath=dataInitPath.resolve("input");
            Files.createDirectories(inputPath);
            createTipoExpedienteDataFile(inputPath);
            createAuthFile(inputPath);

        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    private void createInputConfigFile(Path path) {
        Map<String, Object> context = new HashMap<>();
        context.put("tipoExpediente", tipoExpedienteInstanceFile);


        String content = TemplateUtil.evaluateTemplate("input-config-tipos-expedientes.template", context);

        TemplateUtil.createFileWithContent(path.resolve("input-config.xml"), content);        
    }
    
    private void createTipoExpedienteDataFile(Path path) {
        Map<String, Object> context = new HashMap<>();
        context.put("tipoExpediente", tipoExpedienteInstanceFile);


        String content = TemplateUtil.evaluateTemplate("input-config-tipos-expedientes-input-data.template", context);

        TemplateUtil.createFileWithContent(path.resolve(tipoExpedienteInstanceFile.getCode()+ "-data.xml"), content);
    }

    /**
     * Los permisos de la subclase de {@code Expediente} del tipo. Se generan con su data-init en
     * vez de escribirse a mano en el {@code auth-expedientes.xml} del subsistema, porque son
     * mecánicos (siempre el mismo permiso sobre la entidad del tipo) y olvidarlos deja el trámite
     * en solo lectura para todo el mundo.
     */
    private void createAuthFile(Path path) {
        Map<String, Object> context = new HashMap<>();
        context.put("tipoExpediente", tipoExpedienteInstanceFile);


        String content = TemplateUtil.evaluateTemplate("input-config-tipos-expedientes-input-auth.template", context);

        TemplateUtil.createFileWithContent(path.resolve("auth-" + tipoExpedienteInstanceFile.getCode() + ".xml"), content);
    }
    
    
    
    
}
