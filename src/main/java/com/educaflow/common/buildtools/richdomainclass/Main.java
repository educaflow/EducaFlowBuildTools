/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.richdomainclass;

import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteFile;
import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.files.domainclass.DomainClassFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteFileFinder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
/**
 *
 * @author logongas
 */
public class Main {


    public static void main(String[] args) {
        Path rootPathSourceFiles = Paths.get(args[0]);
        Path rootPathSrcGenJava = Paths.get(args[1]);
        
        List<TipoExpedienteFile> tiposExpedientes=TipoExpedienteFileFinder.findTiposExpedienteFile(rootPathSourceFiles);
        
        for (TipoExpedienteFile tipoExpediente : tiposExpedientes) {

            
            Path entityXmlFileName=getEntityXmlFileName(tipoExpediente.getPath(),tipoExpediente.getCode());
            String packageName=getPackageName(entityXmlFileName,tipoExpediente.getCode());
            Path pathDomainClass=getPathDomainClass(rootPathSrcGenJava,packageName,tipoExpediente.getCode());
            
            DomainClassFile domainClassFile=new DomainClassFile(pathDomainClass,tipoExpediente);
            
            domainClassFile.addExtraCodeToDomainClass();
            
        }

    }

    
    
    
    
    private static Path getPathDomainClass(Path rootDirectory,String packageName,String tipoExpedienteName) {
        
        if (Files.isDirectory(rootDirectory)==false) {
            throw new RuntimeException("No es un directorio:"+ rootDirectory);
        }
        
        String packgeDirectory=packageName.replace(".", "/");
        
        Path pathDomainClass=rootDirectory.resolve(packgeDirectory+"/"+tipoExpedienteName+".java");
        
        return pathDomainClass;
        
    }
    
    
    private static Path getEntityXmlFileName(Path expedienteXmlFile,String tipoExpedienteName) {
        Path directory = expedienteXmlFile.getParent();
        if (directory == null) {
            throw new RuntimeException("No hay directorio padre:"+expedienteXmlFile);
        }

        Path entityXmlFileName = directory.resolve(tipoExpedienteName+".xml");
        
        return entityXmlFileName;
    }
    
    
    private static String getPackageName(Path expedienteXmlFile,String entityName) {

        try {
            if (!java.nio.file.Files.exists(expedienteXmlFile)) {
                throw new RuntimeException("ERROR: No se encontró el fichero en el directorio: " + expedienteXmlFile.toAbsolutePath());
                
            }
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(false); 
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(expedienteXmlFile.toFile());
            
            
            Element module=XMLUtil.getChildFilterByTagName(doc.getDocumentElement(), "module");
            String packageName = module.getAttribute("package");
            
            Element entity=XMLUtil.getChildFilterByTagNameAndAttributeName(doc.getDocumentElement(), entityName, "entity");
            if (entity==null) {
                throw new RuntimeException("ADVERTENCIA: No se encontró el tag '<entity>' con name="+entityName);
            }
            
            return packageName;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    

    

}
