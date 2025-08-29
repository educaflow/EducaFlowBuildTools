/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.tipoexpediente;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author logongas
 */
public class TipoExpedienteInstanceFileFinder {
    
    static final public String TIPO_EXPEDIENTE_XML_NAME="TipoExpedienteInstance.xml";
        
        
    public static List<TipoExpedienteInstanceFile> findTiposExpedienteFile(Path rootPath) {
        List<TipoExpedienteInstanceFile> tipoExpedienteInstanceFiles=new ArrayList<>();
        
        
        List<Path> expedienteXmlFiles = findTiposExpedienteXmlFiles(rootPath);
        for (Path expedienteXmlFile : expedienteXmlFiles) {
            try {
                TipoExpedienteInstanceFile tipoExpedienteInstanceFile=parseTipoExpedienteXml(expedienteXmlFile);
                tipoExpedienteInstanceFiles.add(tipoExpedienteInstanceFile);
            } catch (Exception ex) {
                throw new RuntimeException("Fallo al obtener el tipo de expediente:"+expedienteXmlFile,ex);
            }

        }
        
        return tipoExpedienteInstanceFiles;
    }
    
    

    public static TipoExpedienteInstanceFile parseTipoExpedienteXml(Path expedienteXmlFile) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TipoExpedienteInstanceFile.class, State.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            File xmlFile = expedienteXmlFile.toFile();
            TipoExpedienteInstanceFile tipoExpediente=(TipoExpedienteInstanceFile) unmarshaller.unmarshal(xmlFile);
            tipoExpediente.setPath(expedienteXmlFile);
            
            checkOnlyOneInitialState(tipoExpediente);
            
            
            List<TipoDocumentoPdf> tipoDocumentosPdf=getDocumentosPdf(expedienteXmlFile,tipoExpediente);
            tipoExpediente.setTipoDocumentosPdf(tipoDocumentosPdf);
            
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
                        .filter(path -> path.getFileName().toString().equals(TIPO_EXPEDIENTE_XML_NAME)) 
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

    private static List<TipoDocumentoPdf> getDocumentosPdf(Path expedienteXmlFile,TipoExpedienteInstanceFile tipoExpediente) {
        try {
            Path directorioDocumentosPdf=expedienteXmlFile.getParent().resolve("documentospdf");
            
            List<TipoDocumentoPdf> lista = new ArrayList<>();



            if (!Files.isDirectory(directorioDocumentosPdf)) {
                System.out.print("No existen documentos pdf");
                return lista; 
            }

            // 3. Buscar solo ficheros .pdf en esa carpeta
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directorioDocumentosPdf, "*.pdf")) {
                for (Path entry : stream) {
                    if (Files.isRegularFile(entry)) {
                        String nombre = entry.getFileName().toString();
                        String enumValue = nombre.substring(0, nombre.length() - 4);

                        
                        String filePathName="/documentospdf/" + tipoExpediente.getPackageName().replace(".", "/") + "/documentospdf/" + nombre;
                        
                        TipoDocumentoPdf tipoDocumentoPdf=new TipoDocumentoPdf(toUpperSnakeCase(enumValue),filePathName);                        
                        
                        lista.add(tipoDocumentoPdf);
                        System.out.println("Documento PDF:"+tipoDocumentoPdf.getFileName()+"-->"+tipoDocumentoPdf.getEnumValue());
                    }
                }
            }

            return lista;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static String toUpperSnakeCase(String s) {
        String withUnderscores = s.replaceAll("(?<!^)(?=[A-Z])", "_");

        return withUnderscores.toUpperCase();
    }    
      
}
