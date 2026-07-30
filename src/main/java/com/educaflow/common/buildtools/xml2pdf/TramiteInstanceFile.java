package com.educaflow.common.buildtools.xml2pdf;

import com.educaflow.common.buildtools.common.XMLUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import org.w3c.dom.Element;

/**
 * El TramiteInstance.xml del trámite "padre" del documento: el que está en la
 * carpeta del trámite, por encima de la del tipo de expediente en cuya carpeta
 * documentospdf/documentos está el XML del documento.
 *
 * De él se saca el nombre del trámite (&lt;name&gt;, en castellano), que es el
 * título de los documentos que no traen &lt;titulo&gt; propio.
 */
class TramiteInstanceFile {

    static final String NOMBRE_FICHERO = "TramiteInstance.xml";

    /** Busca el TramiteInstance.xml del trámite al que pertenece el documento
     * subiendo por las carpetas padre desde la del propio documento. Devuelve
     * null si no hay ninguno. */
    static Path buscarDesde(Path documento) {
        Path directorio = documento.toAbsolutePath().normalize().getParent();
        while (directorio != null) {
            Path tramiteInstance = directorio.resolve(NOMBRE_FICHERO);
            if (Files.isRegularFile(tramiteInstance)) {
                return tramiteInstance;
            }
            directorio = directorio.getParent();
        }
        return null;
    }

    /** El &lt;name&gt; del trámite, en castellano. */
    static String getName(Path tramiteInstance) {
        Element name = XMLUtil.getChildFilterByTagName(
                XMLUtil.getDocument(tramiteInstance).getDocumentElement(), "name");
        if (name == null || name.getTextContent().isBlank()) {
            throw new RuntimeException("ERROR: " + tramiteInstance
                    + " no tiene <name>, que es el título de los documentos del trámite"
                    + " que no llevan <titulo>.");
        }
        return name.getTextContent().trim();
    }
}