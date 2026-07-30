package com.educaflow.common.buildtools.xml2pdf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Genera los PDF de los documentos de los trámites en tiempo de compilación.
 *
 * Busca bajo &lt;ruta_origen&gt;/com/educaflow/tramites los XML que están en una
 * carpeta llamada "documentospdf" o "documentos", cuyo nombre no empieza por "_"
 * (convención tipo SASS: los _*.xml son fragmentos incluidos desde otros
 * documentos) y cuyo elemento raíz es &lt;documento&gt;, y genera con Xml2Pdf el
 * PDF de cada uno en &lt;ruta_destino&gt; replicando la ruta relativa a
 * &lt;ruta_origen&gt;, de forma que el PDF queda en el classpath con la misma
 * ruta de paquete que el XML.
 *
 * El tercer argumento (opcional) es el ejecutable del proceso traductor con el
 * que se calcula el &lt;valenciano&gt; de los textos que solo llevan
 * &lt;castellano&gt;; por omisión "apertium", el mismo que usa i18nprocessor.
 */
public class Main {

    static final String TRAMITES = "com/educaflow/tramites";

    public static void main(String[] args) throws Exception {

        if (args.length != 2 && args.length != 3) {
            System.out.println("Uso: java Main <ruta_origen> <ruta_destino> [rutaExecTraductor]");
            return;
        }

        Path sourceRoot = Paths.get(args[0]);
        Path targetBaseDir = Paths.get(args[1]);
        String procesoTraductor = args.length == 3 ? args[2] : Xml2Pdf.TRADUCTOR_POR_DEFECTO;

        Path tramitesDir = sourceRoot.resolve(TRAMITES);
        if (!Files.isDirectory(tramitesDir)) {
            System.out.println("No existe " + tramitesDir + "; nada que generar");
            return;
        }

        List<Path> xmls = findDocumentoXmls(tramitesDir);
        for (Path xml : xmls) {
            Path relativePath = sourceRoot.relativize(xml);
            String pdfName = xml.getFileName().toString().replaceAll("\\.xml$", ".pdf");
            Path pdfEnFuentes = xml.resolveSibling(pdfName);
            if (Files.exists(pdfEnFuentes)) {
                throw new RuntimeException("Existen a la vez " + pdfEnFuentes + " y " + xml
                        + " (con raíz <documento>): no se sabría si usar el " + pdfName
                        + " existente o el que generaría el XML. Borra uno de los dos.");
            }
            Path pdf = targetBaseDir.resolve(relativePath).resolveSibling(pdfName);
            try {
                if (Files.exists(pdf)
                        && Files.getLastModifiedTime(pdf).compareTo(latestModifiedConTramite(xml)) >= 0) {
                    continue;
                }
                Files.createDirectories(pdf.getParent());
                new Xml2Pdf(procesoTraductor).run(xml.toString(), pdf.toString());
            } catch (Exception ex) {
                throw new RuntimeException("Fallo al generar el PDF de: " + xml, ex);
            }
        }
    }

    static List<Path> findDocumentoXmls(Path tramitesDir) throws IOException {
        try (Stream<Path> walk = Files.walk(tramitesDir)) {
            return walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .filter(p -> !p.getFileName().toString().startsWith("_"))
                    .filter(p -> {
                        String parent = p.getParent().getFileName().toString();
                        return parent.equals("documentospdf") || parent.equals("documentos");
                    })
                    .filter(Main::isDocumento)
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    static boolean isDocumento(Path xml) {
        return parse(xml).getDocumentElement().getTagName().equals("documento");
    }

    /** Última modificación del XML, de sus fragmentos o del TramiteInstance.xml
     * de su trámite: su <name> es el título de los documentos que no llevan
     * <titulo>, así que cambiarlo también debe regenerar el PDF. */
    static FileTime latestModifiedConTramite(Path xml) throws IOException {
        FileTime latest = latestModified(xml, new HashSet<>());
        Path tramiteInstance = TramiteInstanceFile.buscarDesde(xml);
        if (tramiteInstance != null) {
            FileTime t = Files.getLastModifiedTime(tramiteInstance);
            if (t.compareTo(latest) > 0) {
                latest = t;
            }
        }
        return latest;
    }

    /** Última modificación del XML o del más reciente de sus fragmentos
     * incluidos (transitivamente): un cambio en un _*.xml debe regenerar los
     * PDF de los documentos que lo incluyen. Un fragmento inexistente fuerza
     * la regeneración para que Xml2Pdf dé su error con la ruta. */
    static FileTime latestModified(Path xml, Set<Path> visitados) throws IOException {
        xml = xml.toAbsolutePath().normalize();
        if (!visitados.add(xml)) {
            return FileTime.fromMillis(0);
        }
        FileTime latest = Files.getLastModifiedTime(xml);
        NodeList includes = parse(xml).getElementsByTagName("include");
        for (int i = 0; i < includes.getLength(); i++) {
            String href = ((Element) includes.item(i)).getAttribute("href");
            Path fragmento = xml.getParent().resolve(href).normalize();
            if (!Files.isRegularFile(fragmento)) {
                return FileTime.fromMillis(Long.MAX_VALUE);
            }
            FileTime t = latestModified(fragmento, visitados);
            if (t.compareTo(latest) > 0) {
                latest = t;
            }
        }
        return latest;
    }

    static Document parse(Path xml) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.toFile());
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al parsear el XML: " + xml, ex);
        }
    }
}
