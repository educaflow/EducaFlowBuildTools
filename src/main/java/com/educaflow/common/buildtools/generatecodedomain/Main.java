/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.generatecodedomain;

import com.educaflow.common.buildtools.common.XMLUtil;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.extension.escaper.EscapingStrategy;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
/**
 *
 * @author logongas
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        System.out.println("***********************");
        List<Path> expedienteXmlFiles = findExpedienteXmlFiles(args[0]);
        for (Path expedienteXmlFile : expedienteXmlFiles) {
            TipoExpediente tipoExpediente=parseTipoExpedienteXml(expedienteXmlFile);
            System.out.println(tipoExpediente.getCode());
            
            Path entityXmlFileName=getEntityXmlFileName(expedienteXmlFile,tipoExpediente.getCode());
            String packageName=getPackageName(entityXmlFileName,tipoExpediente.getCode());
            
            Path pathDomainClass=getPathDomainClass(Paths.get(args[1]),packageName,tipoExpediente.getCode());
            
            System.out.println(pathDomainClass.toAbsolutePath());
            String extraCodeToDomainClass=getExtraCodeToDomainClass(tipoExpediente);
            
            
            addExtraCodeToDomainClass(pathDomainClass,extraCodeToDomainClass);
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
    
    public static TipoExpediente parseTipoExpedienteXml(Path expedienteXmlFile) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TipoExpediente.class, State.class, Event.class, Profile.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            File xmlFile = expedienteXmlFile.toFile();
            return (TipoExpediente) unmarshaller.unmarshal(xmlFile);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<Path> findExpedienteXmlFiles(String rootPath) {
        try {
            Path start = Paths.get(rootPath);

            // Verifica si la ruta existe y es un directorio
            if (!Files.exists(start) || !Files.isDirectory(start)) {
                System.err.println("La ruta proporcionada no existe o no es un directorio: " + rootPath);
                return List.of(); // Retorna una lista vacía
            }

            try (Stream<Path> walk = Files.walk(start)) {
                return walk
                        .filter(Files::isRegularFile) // Nos aseguramos de que sea un archivo regular
                        .filter(path -> path.getFileName().toString().equals("tipoExpediente.xml")) // Filtramos por nombre
                        .collect(Collectors.toList()); // Recolectamos los resultados en una lista
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void addExtraCodeToDomainClass(Path domainClass, String extraCode) {
        try {
            String content = Files.readString(domainClass, StandardCharsets.UTF_8);

            int lastBraceIndex = content.lastIndexOf('}');

            if (lastBraceIndex == -1) {
                throw new RuntimeException("No se encontró la llave de cierre '}' en el archivo: " + domainClass.getFileName());
            }

            // Construir el nuevo contenido insertando el extraCode antes de la última '}'
            String newContent = content.substring(0, lastBraceIndex)
                    + System.lineSeparator()
                    + extraCode
                    + System.lineSeparator()
                    + "}";

            Files.writeString(domainClass, newContent, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    static String getExtraCodeToDomainClass(TipoExpediente tipoExpediente) {

        try {
            EscapingStrategy escapingStrategy=(input) -> {
                String escaped=input.replace("\\n", "\n");
                System.out.print(escaped);
                return escaped;
                
            };
            
            
            ClasspathLoader loader = new ClasspathLoader();
            PebbleEngine engine = new PebbleEngine.Builder().autoEscaping(false).newLineTrimming(false).loader(loader).addEscapingStrategy("none", escapingStrategy).defaultEscapingStrategy("none").build();

            PebbleTemplate compiledTemplate = engine.getTemplate("extra-code-domain.template");

            Map<String, Object> context = new HashMap<>();
            context.put("states", tipoExpediente.getStates());
            context.put("events", tipoExpediente.getEvents());
            context.put("profiles", tipoExpediente.getProfiles());
            context.put("newLine", "\n");
            context.put("tab", "\t");
            

            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, context);

            return writer.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
