package com.educaflow.common.buildtools.viewprocessor;

import com.educaflow.common.buildtools.common.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ViewFileFinder {

    private final String templateViewFileName="common-template-views.xml";
    private final String formTemplateName="subsysExpedientes-common-templates-form";
    private final String systemViewFileName="system-template-views.xml";
    private final String systemFormTemplateName="subsysExpedientes-system-templates-form";
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
    
    /**
     * Los formularios plantilla de todos los {@code common-template-views.xml} que hay
     * bajo la carpeta {@code shared} de los trámites, indexados por su carpeta.
     *
     * La clave del mapa es la ruta relativa de la carpeta que contiene al
     * {@code common-template-views.xml} respecto de la carpeta que se pasa como
     * parámetro: la cadena vacía para el de la propia raíz y, por ejemplo,
     * {@code "bbb/ccc"} para el de {@code <shared>/bbb/ccc/common-template-views.xml}.
     * Los paneles de un {@code <include-panels>} se referencian con esa misma
     * ruta pero con puntos en lugar de barras.
     */
    public Map<String, Element> findTemplateViews(Path directoryPath) {
        Map<String, Element> templates = new LinkedHashMap<>();

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
                        
                        String relativeDirectoryPath = directoryPath.relativize(path.getParent()).toString().replace('\\', '/');
                        templates.put(relativeDirectoryPath, formTemplate);
                    }
                });
        } catch (Exception ex) {
            throw new RuntimeException("Error al recorrer el directorio " + directoryPath + ": " + ex.getMessage(), ex);
        }
        
        if (templates.isEmpty()) {
            throw new RuntimeException("No existe el " + templateViewFileName + " en:"+directoryPath);
        } 
        

        return templates;
    }

    /**
     * El form plantilla del {@code system-template-views.xml} de la raíz de la carpeta
     * {@code shared} de los trámites.
     *
     * Es el que contiene los paneles del subsistema de expedientes (los cuyo
     * nombre empieza por {@code subsysExpedientes}); el resto de paneles de un
     * {@code <include-panels>} viven en los {@code common-template-views.xml}.
     */
    public Element findSystemTemplateViews(Path sharedPath) {
        Path systemViewPath = sharedPath.resolve(systemViewFileName);

        if (!Files.isRegularFile(systemViewPath)) {
            throw new RuntimeException("No existe el " + systemViewFileName + " en:" + sharedPath);
        }

        Document systemViewDocument = parseAndValidateXml(systemViewPath);
        if (systemViewDocument == null) {
            throw new RuntimeException("El " + systemViewFileName + " de:" + sharedPath + " no es un fichero de vistas (raiz 'object-views')");
        }

        Element systemFormTemplate = XMLUtil.getChildFilterByTagNameAndAttributeName(systemViewDocument.getDocumentElement(), systemFormTemplateName, "form");
        if (systemFormTemplate == null) {
            throw new RuntimeException("No existe el form '" + systemFormTemplateName + "' en " + systemViewPath);
        }

        return systemFormTemplate;
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
