/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.createdatainittipoexpediente;

import com.educaflow.common.buildtools.files.domainclass.DomainClassFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author logongas
 */
public class Main {

    public static void main(String[] args) {
        Path rootPathSourceFiles = Paths.get(args[0]);
        Path rootPathBuildResources = Paths.get(args[1]);
        
        System.out.println("Iniciando tarea de generar los data-init de los tipos de expedientes....");
        System.out.println("rootPathSourceFiles="+rootPathSourceFiles);
        System.out.println("rootPathBuildResources="+rootPathBuildResources);
        
        
        
        List<TipoExpedienteInstanceFile> tiposExpedientes=TipoExpedienteInstanceFileFinder.findTiposExpedienteFile(rootPathSourceFiles);
        
        for (TipoExpedienteInstanceFile tipoExpediente : tiposExpedientes) {
            System.out.println("Encontrado Tipo de expediente instancia:"+tipoExpediente.getName()+ " en " + tipoExpediente.getPath());
            
            InitDataTipoExpedienteFiles initDataTipoExpedienteFiles=new InitDataTipoExpedienteFiles(tipoExpediente, rootPathBuildResources.resolve("tiposExpedientes"));
            
            initDataTipoExpedienteFiles.generateInitData();
            
        }
        
        System.out.println("Finalizada tarea de generar los data-init de los tipos de expedientes");

    }    
    
}
