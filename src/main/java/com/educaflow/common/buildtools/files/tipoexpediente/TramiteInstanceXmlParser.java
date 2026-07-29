package com.educaflow.common.buildtools.files.tipoexpediente;

import com.educaflow.common.buildtools.common.XMLUtil;
import java.nio.file.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Lee el <code> y el <name> del TramiteInstance.xml de la carpeta del trámite.
 * Se usa para derivar los datos de un TipoExpedienteInstance.xml que no los declara.
 *
 * @author logongas
 */
public class TramiteInstanceXmlParser {

    static final public String TRAMITE_XML_NAME = "TramiteInstance.xml";

    private final String code;
    private final String name;

    private TramiteInstanceXmlParser(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TramiteInstanceXmlParser parse(Path tramiteInstanceXmlFile) {
        Document document = XMLUtil.getDocument(tramiteInstanceXmlFile);

        String code = getChildTextContent(document, "code", tramiteInstanceXmlFile);
        String name = getChildTextContent(document, "name", tramiteInstanceXmlFile);

        return new TramiteInstanceXmlParser(code, name);
    }

    private static String getChildTextContent(Document document, String tagName, Path tramiteInstanceXmlFile) {
        Element element = XMLUtil.getChildFilterByTagName(document.getDocumentElement(), tagName);

        if ((element == null) || (element.getTextContent().isBlank())) {
            throw new RuntimeException("No existe el tag <" + tagName + "> en el fichero del trámite:" + tramiteInstanceXmlFile);
        }

        return element.getTextContent().trim();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
