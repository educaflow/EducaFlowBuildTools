package com.educaflow.common.buildtools.crap;

import com.educaflow.common.buildtools.common.XMLUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Lee el informe XML de JaCoCo ({@code report > package > class > method > counter}) y devuelve las
 * métricas de cada método con código.
 *
 * <p>No usa {@link XMLUtil#getDocument(Path)} porque el XML de JaCoCo declara el DOCTYPE
 * {@code report.dtd}, que no viene con el informe: sin desactivar la carga de DTD externos el parseo
 * falla o intenta ir a la red.
 *
 * @author logongas
 */
public class JacocoReportReader {

    private static final String CARGAR_DTD_EXTERNO = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    private final Path origen;

    /**
     * @param origen raíz de los fuentes (p.ej. {@code ./src/main/java}), con la que se construye la
     *               ruta del fichero de cada método
     */
    public JacocoReportReader(Path origen) {
        this.origen = origen;
    }

    public List<MetricasMetodo> leer(Path jacocoXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature(CARGAR_DTD_EXTERNO, false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(jacocoXml.toFile());

        List<MetricasMetodo> metricas = new ArrayList<>();
        for (Element paquete : XMLUtil.getChildsFilterByTagName(document.getDocumentElement(), "package")) {
            for (Element clase : XMLUtil.getChildsFilterByTagName(paquete, "class")) {
                String fichero = origen.resolve(paquete.getAttribute("name")).resolve(clase.getAttribute("sourcefilename")).toString();
                String nombreClase = clase.getAttribute("name").replace('/', '.');
                for (Element metodo : XMLUtil.getChildsFilterByTagName(clase, "method")) {
                    Contador instrucciones = contador(metodo, "INSTRUCTION");
                    if (instrucciones.total() == 0) {
                        continue;
                    }
                    Contador lineas = contador(metodo, "LINE");
                    Contador ramas = contador(metodo, "BRANCH");
                    metricas.add(new MetricasMetodo(
                            fichero,
                            XMLUtil.getIntegerAttribute(metodo, "line", 0),
                            nombreClase,
                            metodo.getAttribute("name"),
                            metodo.getAttribute("desc"),
                            contador(metodo, "COMPLEXITY").total(),
                            instrucciones.cubiertos(),
                            instrucciones.total(),
                            lineas.cubiertos(),
                            lineas.total(),
                            ramas.cubiertos(),
                            ramas.total()));
                }
            }
        }
        return metricas;
    }

    /** JaCoCo omite el {@code <counter>} de un tipo cuando el método no tiene nada que contar (p.ej. sin ramas). */
    private static Contador contador(Element metodo, String tipo) {
        for (Element counter : XMLUtil.getChildsFilterByTagName(metodo, "counter")) {
            if (tipo.equals(counter.getAttribute("type"))) {
                int cubiertos = Integer.parseInt(counter.getAttribute("covered"));
                int noCubiertos = Integer.parseInt(counter.getAttribute("missed"));
                return new Contador(cubiertos, cubiertos + noCubiertos);
            }
        }
        return new Contador(0, 0);
    }

    private record Contador(int cubiertos, int total) {
    }
}
