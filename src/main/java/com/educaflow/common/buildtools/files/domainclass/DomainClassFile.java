/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.domainclass;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author logongas
 */
public class DomainClassFile {

    private final TipoExpedienteInstanceFile tipoExpedienteFile;
    private final Path pathDomainClass;

    public DomainClassFile(Path pathDomainClass, TipoExpedienteInstanceFile tipoExpedienteFile) {
        this.pathDomainClass = pathDomainClass;
        this.tipoExpedienteFile = tipoExpedienteFile;
    }

    public void addExtraCodeToDomainClass() {
        String extraCodeToDomainClass = getExtraCodeToDomainClass(tipoExpedienteFile);

        addExtraCodeToDomainClass(pathDomainClass, extraCodeToDomainClass);
    }

    private void addExtraCodeToDomainClass(Path domainClass, String extraCode) {
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

    private String getExtraCodeToDomainClass(TipoExpedienteInstanceFile tipoExpedienteFile) {

        Map<String, Object> context = new HashMap<>();
        context.put("states", tipoExpedienteFile.getStates());
        context.put("events", tipoExpedienteFile.getEvents());
        context.put("profiles", tipoExpedienteFile.getProfiles());
        context.put("newLine", "\n");
        context.put("tab", "\t");

        String extraCodeDomainClass=TemplateUtil.evaluateTemplate("extra-code-domain.template", context);


        return extraCodeDomainClass;

    }

}
