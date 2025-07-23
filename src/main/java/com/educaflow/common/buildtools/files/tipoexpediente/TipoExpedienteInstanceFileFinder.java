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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author logongas
 */
public class TipoExpedienteInstanceFileFinder {
    
    static final private String TIPO_EXPEDIENTE_XML="TipoExpedienteInstance.xml";
        
        
    public static List<TipoExpedienteInstanceFile> findTiposExpedienteFile(Path rootPath) {
        List<TipoExpedienteInstanceFile> tiposExpedientes=new ArrayList<>();
        
        
        List<Path> expedienteXmlFiles = findTiposExpedienteXmlFiles(rootPath);
        for (Path expedienteXmlFile : expedienteXmlFiles) {
            try {
                TipoExpedienteInstanceFile tipoExpediente=parseTipoExpedienteXml(expedienteXmlFile);
                tiposExpedientes.add(tipoExpediente);
            } catch (Exception ex) {
                throw new RuntimeException("Fallo al obtener el tipo de expediente:"+expedienteXmlFile,ex);
            }

        }
        
        return tiposExpedientes;
    }
    
    

    public static TipoExpedienteInstanceFile parseTipoExpedienteXml(Path expedienteXmlFile) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TipoExpedienteInstanceFile.class, State.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            File xmlFile = expedienteXmlFile.toFile();
            TipoExpedienteInstanceFile tipoExpediente=(TipoExpedienteInstanceFile) unmarshaller.unmarshal(xmlFile);
            tipoExpediente.setPath(expedienteXmlFile);
            
            checkOnlyOneInitialState(tipoExpediente);
            
            
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

    private static void checkOnlyOneInitialState(TipoExpedienteInstanceFile tipoExpediente) {
        List<String> initialStates=new ArrayList<>();
        
        for(State state:tipoExpediente.getStates()) {
            if (state.isInitial()==true) {
                initialStates.add(state.getName());
            }
        }
        
        
        if (initialStates.isEmpty()) {
            throw new RuntimeException("No existe ningun estado inicial");
        } else if (initialStates.size()>1) {
            throw new RuntimeException("Existe más de un estado inicial:"+String.join(",", initialStates));
        }
        
    }
      
}
