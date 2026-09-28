package com.educaflow.common.buildtools.xml2pdf;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Los tipos de documento PDF de un trámite. Cada uno se reconoce por su
 * elemento raíz y se valida contra su propio esquema, que declara además el
 * &lt;fragmento&gt; con el contenido de ese tipo: incluir en un documento un
 * fragmento del otro tipo no valida.
 */
public enum TipoDocumento {

    FORMULARIO("documentoFormulario"),
    TEXTO("documentoTexto");

    private final String raiz;

    TipoDocumento(String raiz) {
        this.raiz = raiz;
    }

    public String getRaiz() {
        return raiz;
    }

    public String getXsd() {
        return raiz + ".xsd";
    }

    public static TipoDocumento delFichero(Path xml) {
        String raiz = raizDe(xml);
        return porRaiz(raiz).orElseThrow(() -> new RuntimeException("ERROR: el elemento raíz de "
                + xml + " es <" + raiz + ">, que no es ningún tipo de documento:"
                + " las raíces admitidas son " + raicesAdmitidas() + "."));
    }

    public static boolean esDocumento(Path xml) {
        return porRaiz(raizDe(xml)).isPresent();
    }

    public static String raicesAdmitidas() {
        return Arrays.stream(values())
                .map(tipo -> "<" + tipo.raiz + ">")
                .collect(Collectors.joining(", "));
    }

    static Optional<TipoDocumento> porRaiz(String raiz) {
        return Arrays.stream(values()).filter(tipo -> tipo.raiz.equals(raiz)).findFirst();
    }

    private static String raizDe(Path xml) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(xml.toFile()).getDocumentElement().getTagName();
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al parsear el XML: " + xml, ex);
        }
    }
}
