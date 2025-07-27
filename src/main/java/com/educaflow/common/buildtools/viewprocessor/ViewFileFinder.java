package com.educaflow.common.buildtools.viewprocessor;

import com.educaflow.common.buildtools.common.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ViewFileFinder {

    private final String templateViewFileName="template-views.xml";
    private final String formTemplateName="exp-Expediente-Templates";
    
    private final DocumentBuilderFactory documentBuilderFactory;

    public ViewFileFinder() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
    }


    public Map<Document, Path>  findViews(Path directoryPath) {
        Map<Document, Path> result = new LinkedHashMap<>();

        if (!Files.isDirectory(directoryPath)) {
            throw new RuntimeException("La ruta proporcionada no es un directorio válido: " + directoryPath);
        }

        try (Stream<Path> walk = Files.walk(directoryPath)) {
            walk.filter(Files::isRegularFile) 
                .filter(p -> p.toString().toLowerCase().endsWith(".xml")) 
                .forEach(path -> {
                    Document viewElement = parseAndValidateXml(path);
                    if (viewElement != null) {
                        result.put(viewElement, path);
                    }
                });
        } catch (Exception ex) {
            throw new RuntimeException("Error al recorrer el directorio " + directoryPath + ": " + ex.getMessage(), ex);
        }

        return result;
    }
    
    public List<Element> findTemplateViews(Path directoryPath) {
        List<Element> templates = new ArrayList<>();

        if (!Files.isDirectory(directoryPath)) {
            throw new RuntimeException("La ruta proporcionada no es un directorio válido: " + directoryPath);
        }

        try (Stream<Path> walk = Files.walk(directoryPath)) {
            walk.filter(Files::isRegularFile)  
                .filter(p -> p.getFileName().toString().equalsIgnoreCase(templateViewFileName))
                .forEach(path -> {
                    Document viewDocument = parseAndValidateXml(path);
                    if (viewDocument != null) {
                        Element formTemplate=XMLUtil.getChildFilterByTagNameAndAttributeName(viewDocument.getDocumentElement(), formTemplateName,"form");
                        if (formTemplate==null) {
                            throw new RuntimeException("No existe el form '" + formTemplateName + "' en "+path);
                        }
                        
                        templates.add(formTemplate);
                    }
                });
        } catch (Exception ex) {
            throw new RuntimeException("Error al recorrer el directorio " + directoryPath + ": " + ex.getMessage(), ex);
        }
        
        if (templates.size()==0) {
            throw new RuntimeException("No existe el " + templateViewFileName + " en:"+directoryPath);
        } 
        

        return templates;
    }

    private Document parseAndValidateXml(Path filePath ) {
        String targetRootTagName="object-views";
        if (!Files.isRegularFile(filePath)) {
            throw new RuntimeException("La ruta no es un archivo XML válido o no existe: " + filePath);
        }

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(filePath.toFile());
            Element rootElement = document.getDocumentElement();

            String rootTagName = rootElement.getTagName();

            if (targetRootTagName.equals(rootTagName)) {
                return document;
            } else {
                return null;
            }

        } catch (Exception ex) {
            throw new RuntimeException(filePath.toString(), ex);
        }
    }

}
