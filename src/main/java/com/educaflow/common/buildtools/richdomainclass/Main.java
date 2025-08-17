/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.richdomainclass;

import com.educaflow.common.buildtools.common.FileUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.files.domainclass.DomainClassFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder;
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
        
        System.out.println("Iniciando tarea de enriquecer las clases de dominio....");
        System.out.println("rootPathSourceFiles="+rootPathSourceFiles);
        System.out.println("rootPathSrcGenJava="+rootPathSrcGenJava);
        
        
        
        List<TipoExpedienteInstanceFile> tipoExpedienteInstanceFiles=TipoExpedienteInstanceFileFinder.findTiposExpedienteFile(rootPathSourceFiles);
        
        for (TipoExpedienteInstanceFile tipoExpedienteInstanceFile : tipoExpedienteInstanceFiles) {
            System.out.println("Encontrado Tipo de expediente instancia:"+tipoExpedienteInstanceFile.getName()+ " en " + tipoExpedienteInstanceFile.getPath());
            
            Path entityXmlFileName=getEntityXmlFileName(tipoExpedienteInstanceFile.getPath(),"domains.xml");
            String packageName=getPackageName(entityXmlFileName,tipoExpedienteInstanceFile.getCode());
            Path pathDomainClass=getPathDomainClass(rootPathSrcGenJava,packageName,tipoExpedienteInstanceFile.getCode());
            System.out.println("Encontrado clase de domino es:"+pathDomainClass);
            DomainClassFile domainClassFile=new DomainClassFile(pathDomainClass,tipoExpedienteInstanceFile);
            
            try {
                domainClassFile.addExtraCodeToDomainClass();
            } catch (Exception ex) {
                FileUtil.ls(pathDomainClass.getParent());
            }
            
        }
        
        System.out.println("Finalizada tarea de enriquecer las clases de dominio");

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

        Path entityXmlFileName = directory.resolve(tipoExpedienteName);
        
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
