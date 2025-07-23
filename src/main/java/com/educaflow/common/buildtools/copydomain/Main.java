/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.copydomain;

import com.educaflow.common.buildtools.copyinitdata.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Main {
    
    public static void main(String[] args) {
        try {
        
        
            String origen = args[0];
            String destino = args[1];


            System.out.println("Iniciando tarea de copiar modelos de dominio....");
            System.out.println("origen="+origen);
            System.out.println("destino="+destino);


            File origenDir = new File(origen);
            File destinoDir = new File(destino);
            Path origenPath = Paths.get(origen);
            Path destinoPath = Paths.get(destino);
            
            if (!destinoDir.exists()) {
                destinoDir.mkdirs();
            }

            if (origenDir.exists() && origenDir.isDirectory()) {
                List<Path> origenDomainModelPath=findDomainModelXml(origenPath);

                for (Path origenDataInitPath : origenDomainModelPath) {

                    Path parteRelativa = origenPath.relativize(origenDataInitPath);
                    Path destinoDataInitPath = destinoPath.resolve(parteRelativa);

                    try {
                        System.out.println("Copiando: " + origenDataInitPath.toAbsolutePath()+ " a " + destinoDataInitPath.toAbsolutePath());
                        Files.createDirectories(destinoDataInitPath.getParent());
                        Files.copy(origenDataInitPath, destinoDataInitPath, StandardCopyOption.REPLACE_EXISTING);
                        
                    } catch (IOException ex) {
                        throw new RuntimeException("Error al copiar la carpeta " + origenDataInitPath.toAbsolutePath()+ " a " + destinoDataInitPath.toAbsolutePath(),ex);
                    }

                }

            } else {
                throw new RuntimeException("El directorio de origen '" + origenPath + "' no existe o no es un directorio.");
            }

            System.out.println("Finalizada tarea de copiar modelos de dominio");
        
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
    }


    public static List<Path> findDomainModelXml(Path basePath) throws IOException {
        List<Path> ficherosValidos = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try (Stream<Path> paths = Files.walk(basePath)) {
            List<Path> xmlFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .collect(Collectors.toList());

            DocumentBuilder builder = factory.newDocumentBuilder();

            for (Path xmlFile : xmlFiles) {
                try {
                    Document doc = builder.parse(xmlFile.toFile());
                    Element root = doc.getDocumentElement();

                    // Comprobación del nombre del nodo raíz
                    if (!"domain-models".equals(root.getLocalName())) continue;

                    // Comprobación de atributos
                    String xmlns = root.getAttribute("xmlns");
                    String xmlnsXsi = root.getAttribute("xmlns:xsi");
                    String schemaLocation = root.getAttribute("xsi:schemaLocation");

                    if ("http://axelor.com/xml/ns/domain-models".equals(xmlns)) {
                        ficherosValidos.add(xmlFile);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException("Fallo al leer el XML:"+xmlFile, ex);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error configurando el parser XML", ex);
        }

        return ficherosValidos;
    }   
    
    

}