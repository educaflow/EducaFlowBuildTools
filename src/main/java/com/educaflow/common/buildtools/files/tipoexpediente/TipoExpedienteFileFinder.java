/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.tipoexpediente;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author logongas
 */
public class TipoExpedienteFileFinder {
    
    static final private String TIPO_EXPEDIENTE_XML="tipoExpediente.xml";
        
        
    public static List<TipoExpedienteFile> findTiposExpedienteFile(Path rootPath) {
        List<TipoExpedienteFile> tiposExpedientes=new ArrayList<>();
        
        
        List<Path> expedienteXmlFiles = findTiposExpedienteXmlFiles(rootPath);
        for (Path expedienteXmlFile : expedienteXmlFiles) {
            TipoExpedienteFile tipoExpediente=parseTipoExpedienteXml(expedienteXmlFile);
            tiposExpedientes.add(tipoExpediente);
            

        }
        
        return tiposExpedientes;
    }
    
    

    public static TipoExpedienteFile parseTipoExpedienteXml(Path expedienteXmlFile) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TipoExpedienteFile.class, State.class, Event.class, Profile.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            File xmlFile = expedienteXmlFile.toFile();
            TipoExpedienteFile tipoExpediente=(TipoExpedienteFile) unmarshaller.unmarshal(xmlFile);
            tipoExpediente.setPath(expedienteXmlFile);
            
            return tipoExpediente;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<Path> findTiposExpedienteXmlFiles(Path rootPath) {
        try {

            if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
                throw new RuntimeException("El directorio no existe o no es un directorio:"+rootPath);
            }

            try (Stream<Path> walk = Files.walk(rootPath)) {
                return walk
                        .filter(Files::isRegularFile) 
                        .filter(path -> path.getFileName().toString().equals(TIPO_EXPEDIENTE_XML)) 
                        .collect(Collectors.toList()); 
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }    
    
}
