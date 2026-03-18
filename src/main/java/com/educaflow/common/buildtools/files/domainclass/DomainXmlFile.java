/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.domainclass;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author logongas
 */
public class DomainXmlFile {

    private final TipoExpedienteInstanceFile tipoExpedienteFile;
    private final Path pathDomainXml;

    public DomainXmlFile(Path pathDomainXml, TipoExpedienteInstanceFile tipoExpedienteFile) {
        this.pathDomainXml = pathDomainXml;
        this.tipoExpedienteFile = tipoExpedienteFile;
    }

    public void addExtraCodeToDomainXml() {
        String extraCodeToDomainXml = getExtraCodeToDomainXml(tipoExpedienteFile);

        addExtraCodeToDomainXml(pathDomainXml, extraCodeToDomainXml);
    }

    public Path getPathDomainXml() {
        return pathDomainXml;
    }
    
    private void addExtraCodeToDomainXml(Path filePath, String newExtraCodeModel) {
try {
    String content = Files.readString(filePath, StandardCharsets.UTF_8);

    String newCdata = "        <![CDATA[\n" + newExtraCodeModel + "\n        ]]>";
    String newExtraCodeModelTag = "    <extra-code-model>\n" + newCdata + "\n        </extra-code-model>";

    // Localizar la entity con extends="Expediente" tolerando espacios
    java.util.regex.Matcher extendsMatcher = java.util.regex.Pattern
            .compile("extends\\s*=\\s*\"Expediente\"")
            .matcher(content);
    if (!extendsMatcher.find()) {
        throw new RuntimeException("No se ha encontrado la entity con extends=\"Expediente\" en: " + filePath);
    }
    int extendsPos = extendsMatcher.start();

    java.util.regex.Matcher entityCloseMatcher = java.util.regex.Pattern
            .compile("<\\s*/\\s*entity\\s*>")
            .matcher(content);
    entityCloseMatcher.region(extendsPos, content.length());
    if (!entityCloseMatcher.find()) {
        throw new RuntimeException("No se ha encontrado el cierre </entity> de la entity con extends=\"Expediente\" en: " + filePath);
    }
    int entityClosePos = entityCloseMatcher.start();

    // Buscar <extra-code-model> acotado entre la entity con Expediente y su </entity>
    java.util.regex.Matcher extraCodeModelMatcher = java.util.regex.Pattern
            .compile("<\\s*extra-code-model\\s*>")
            .matcher(content);
    extraCodeModelMatcher.region(extendsPos, entityClosePos);
    if (extraCodeModelMatcher.find()) {
        // Existe dentro de la entity: sustituir el bloque completo
        int start = extraCodeModelMatcher.start();
        java.util.regex.Matcher extraCodeModelCloseMatcher = java.util.regex.Pattern
                .compile("<\\s*/\\s*extra-code-model\\s*>")
                .matcher(content);
        extraCodeModelCloseMatcher.region(start, content.length());
        if (!extraCodeModelCloseMatcher.find()) {
            throw new RuntimeException("No se ha encontrado el cierre </extra-code-model> en: " + filePath);
        }
        int end = extraCodeModelCloseMatcher.end();
        content = content.substring(0, start) + newExtraCodeModelTag + content.substring(end);
    } else {
        // No existe dentro de la entity: insertar antes de su </entity>
        content = content.substring(0, entityClosePos) + newExtraCodeModelTag + "\n    " + content.substring(entityClosePos);
    }

    Files.writeString(filePath, content, StandardCharsets.UTF_8);

} catch (Exception ex) {
    throw new RuntimeException("Error al serializar el XML en: " + filePath, ex);
}

    }

    private String getExtraCodeToDomainXml(TipoExpedienteInstanceFile tipoExpedienteFile) {

        Map<String, Object> context = new HashMap<>();
        context.put("states", tipoExpedienteFile.getStates());
        context.put("events", tipoExpedienteFile.getEvents());
        context.put("profiles", tipoExpedienteFile.getProfiles());
        context.put("tipoDocumentosPdf", tipoExpedienteFile.getTipoDocumentosPdf());
        context.put("newLine", "\n");
        context.put("tab", "\t");

        String extraCodeDomainXml=TemplateUtil.evaluateTemplate("extra-code-domain-xml.template", context);


        return extraCodeDomainXml;

    }

}
