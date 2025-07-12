/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.createfiles;

import com.educaflow.common.buildtools.files.domainclass.DomainClassFile;
import com.educaflow.common.buildtools.files.domainmodel.DomainModelFile;
import com.educaflow.common.buildtools.files.eventmanagerfile.EventManagerFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteFileFinder;
import com.educaflow.common.buildtools.files.views.ViewsFile;
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
        
        List<TipoExpedienteFile> tipoExpedienteFiles=TipoExpedienteFileFinder.findTiposExpedienteFile(rootPathSourceFiles);
        
        StringBuilder messages=new StringBuilder();
        
        for (TipoExpedienteFile tipoExpedienteFile : tipoExpedienteFiles) {

            Path pathDomainModelFile=getPathDomainModelFile(tipoExpedienteFile);
            DomainModelFile domainModelFile=new DomainModelFile(pathDomainModelFile,tipoExpedienteFile);
            domainModelFile.createDomainModelIfNotExists();
            
            

            Path pathViewsFile=getPathViewsFile(tipoExpedienteFile);
            ViewsFile viewsFile=new ViewsFile(pathViewsFile,tipoExpedienteFile);
            viewsFile.createViewsFileIfNotExists();
            
                        
            Path pathEventManagerFile=getPathEventManagerFile(tipoExpedienteFile);
            EventManagerFile eventManagerFile=new EventManagerFile(pathEventManagerFile, tipoExpedienteFile);
            eventManagerFile.createEventManagerFileIfNotExists();
            String message=eventManagerFile.check();
            if (message!=null) {
                messages.append(message);
            }
            
        }
        

        if (messages.length()>0) {
            throw new RuntimeException("Alguno de las clases no se pudo precompilar:\n"+messages.toString());
        }

    }
    
        
    private static Path getPathDomainModelFile(TipoExpedienteFile tipoExpedienteFile) {
        return replaceFileName(tipoExpedienteFile.getPath(),tipoExpedienteFile.getCode()+".xml");
    } 
    
    private static Path getPathViewsFile(TipoExpedienteFile tipoExpedienteFile) {
        return replaceFileName(tipoExpedienteFile.getPath(),"views.xml");
    }  
    
    private static Path replaceFileName(Path path,String newFileName) {
        Path parentDirectory = path.getParent();
        Path newPath = parentDirectory.resolve(newFileName);
        
        return newPath;
    }

    private static Path getPathEventManagerFile(TipoExpedienteFile tipoExpedienteFile) {
        return replaceFileName(tipoExpedienteFile.getPath(),"EventManager.java");
    }

}
