/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.educaflow.common.buildtools.viewprocessor;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.w3c.dom.Document;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author logongas
 */
public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Uso: java YourClass <ruta_origen> <ruta_destino>");
            return;
        }

        Path pathToSearch = Paths.get(args[0]);
        Path targetBaseDir = Paths.get(args[1]); // Renombrado para mayor claridad

        String targetRoot = "object-views";

        XMLFinder finder = new XMLFinder();

        List<Path> allXmlFiles = finder.findXmlFiles(pathToSearch);
        Files.createDirectories(targetBaseDir);

        for (Path filePath : allXmlFiles) {
            try {
                Path relativePath = pathToSearch.relativize(filePath);
                Path newFilePathInTarget = targetBaseDir.resolve(relativePath);
                Files.createDirectories(newFilePathInTarget.getParent());

                Document document = finder.parseAndValidateXml(filePath, targetRoot);
                if (document != null) {
                    Document newDocument = XMLPreprocesor.process(filePath,document);
                    guardarXML(newDocument, newFilePathInTarget);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Fallo al prerocesar el fichero: " + filePath,ex);
            }
        }

    }

    public static void guardarXML(Document doc, Path destino) throws Exception {
        // Crear los directorios padre si no existen
        Files.createDirectories(destino.getParent());

        // Crear el transformador para escribir el XML
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // para que sea legible
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        eliminarTextNodesVacios(doc);
        // Grabar el XML en disco
        try (OutputStream os = Files.newOutputStream(destino)) {
            transformer.transform(new DOMSource(doc), new StreamResult(os));
        }
    }

    public static void eliminarTextNodesVacios(Node node) {
        NodeList hijos = node.getChildNodes();
        for (int i = hijos.getLength() - 1; i >= 0; i--) {
            Node hijo = hijos.item(i);
            if (hijo.getNodeType() == Node.TEXT_NODE) {
                String texto = hijo.getTextContent().trim();
                if (texto.isEmpty()) {
                    node.removeChild(hijo);
                }
            } else if (hijo.hasChildNodes()) {
                eliminarTextNodesVacios(hijo);
            }
        }
    }

}
