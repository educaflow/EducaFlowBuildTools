/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.axelorxmlpreprocesor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;



public class XMLFinder {

    private final DocumentBuilderFactory documentBuilderFactory;

    public XMLFinder() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false); 
    }

    /**
     * 1. Recorre un directorio dado y sus subdirectorios para encontrar
     * todos los archivos con extensión ".xml".
     *
     * @param directoryPath La ruta del directorio donde buscar.
     * @return Una lista de Paths a todos los archivos XML encontrados.
     */
    public List<Path> findXmlFiles(Path directoryPath) {
        List<Path> xmlFiles = new ArrayList<>();

        if (!Files.isDirectory(directoryPath)) {
            throw new RuntimeException("La ruta proporcionada no es un directorio válido: " + directoryPath);
        }

        try (Stream<Path> walk = Files.walk(directoryPath)) {
            walk.filter(Files::isRegularFile) // Solo archivos, no directorios
                .filter(p -> p.toString().toLowerCase().endsWith(".xml")) // Solo archivos .xml
                .forEach(xmlFiles::add); // Añade el Path a la lista
        } catch (Exception ex) {
            throw new RuntimeException("Error al recorrer el directorio " + directoryPath + ": " + ex.getMessage(),ex);
        }

        return xmlFiles;
    }

    /**
     * 2. Dada la ruta de un archivo XML, lo parsea, lo valida contra un
     * tag raíz y un namespace específicos, y retorna el objeto Document.
     *
     * @param filePath El Path del archivo XML a validar.
     * @param targetRootTagName El nombre del tag raíz esperado (ej. "object-views").
     * @return El objeto Document si el archivo cumple los criterios, o null si no los cumple o hay un error.
     */
    public Document parseAndValidateXml(Path filePath, String targetRootTagName) {
        if (!Files.isRegularFile(filePath)) {
            throw new RuntimeException("La ruta no es un archivo XML válido o no existe: " + filePath);
        }

        try {
            DocumentBuilder dBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(filePath.toFile());

            Element rootElement = doc.getDocumentElement();

            String rootTagName = rootElement.getTagName();

            if (targetRootTagName.equals(rootTagName)) {
                return doc; 
            } else {

                return null; 
            }

        } catch (Exception ex) {
            throw new RuntimeException(filePath.toString(),ex);
        }
    }




}