package com.educaflow.common.buildtools.xml2pdf;

import com.educaflow.common.buildtools.common.Traductor;
import com.educaflow.common.buildtools.files.tramite.TramiteInstanceFile;
import com.educaflow.common.buildtools.files.tramite.TramiteInstanceFileFinder;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Resuelve el XML de definición de un documento PDF de un trámite: lo valida
 * contra el esquema de su {@link TipoDocumento}, expande sus {@code <include>},
 * le pone el {@code <titulo>} del trámite si es un formulario y no trae
 * ninguno, completa con el traductor los
 * {@code <valenciano>} que falten, comprueba la estructura propia de su tipo y
 * escribe el XML resultante, autocontenido, para que la aplicación dibuje el
 * PDF en tiempo de ejecución (paquete
 * {@code com.educaflow.base.infrastructure.pdfgenerator} de secretaria-virtual).
 *
 * <p>Aquí NO se dibuja nada: el PDF se genera en runtime porque los atributos
 * {@code visible}/{@code siOculto} dependen de los datos del expediente y
 * colapsar un elemento exige maquetar con esos datos en la mano.
 */
public class DocumentoXmlResolver {

    static final int FULL = 1200;
    static final Pattern INLINE = Pattern.compile("\\$\\{([^{}]+)\\}");
    static final String TRADUCTOR_POR_DEFECTO = "apertium";

    /** Proceso traductor externo con el que se calcula el &lt;valenciano&gt; de
     * los textos que solo llevan &lt;castellano&gt;. */
    final String procesoTraductor;

    /** La estructura de carpetas de los trámites, con la que se busca el
     * TramiteInstance.xml del trámite al que pertenece el documento sin subir
     * nunca por encima del paquete raíz. Es null cuando se invoca a mano
     * (main): ahí no se sabe cuál es la raíz de fuentes y la búsqueda se hace
     * sin tope. */
    final TramitesLayout tramitesLayout;

    DocumentoXmlResolver(String procesoTraductor) {
        this(procesoTraductor, null);
    }

    DocumentoXmlResolver(String procesoTraductor, TramitesLayout tramitesLayout) {
        this.procesoTraductor = procesoTraductor;
        this.tramitesLayout = tramitesLayout;
    }

    // ------------------------------------------------------------------ main

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Uso: DocumentoXmlResolver entrada.xml salida.xml [rutaExecTraductor]");
            System.exit(1);
        }
        new DocumentoXmlResolver(args.length >= 3 ? args[2] : TRADUCTOR_POR_DEFECTO).run(args[0], args[1]);
    }

    void run(String in, String out) throws Exception {
        File entrada = new File(in);
        TipoDocumento tipo = TipoDocumento.delFichero(entrada.toPath());
        Element root = loadDocumento(entrada, tipo);
        validarEstructura(tipo, root);
        quitarSufijoNoTraducir(root);
        quitarEspaciosEntreElementos(root);
        escribir(root, new File(out));
        System.out.println("Resuelto " + out);
    }

    /** Carga el XML de un documento: valida el fichero contra el esquema de su
     * tipo, expande recursivamente sus <include href="_x.xml"/>, valida también el documento
     * resultante de la expansión (p.ej. un segundo <titulo> aportado por un
     * fragmento), le pone al formulario el <titulo> del trámite si no trae
     * ninguno y completa con el traductor los <valenciano> que falten. */
    Element loadDocumento(File in, TipoDocumento tipo) {
        Document dom = parseValidated(in, tipo.getRaiz(), tipo);
        expandIncludes(dom.getDocumentElement(),
                in.getAbsoluteFile().toPath().normalize(), new ArrayList<>(), tipo);
        validate(new DOMSource(dom), in + " (expandido con sus includes)", tipo);
        if (tipo == TipoDocumento.FORMULARIO) {
            addTituloDelTramiteIfNotExists(dom.getDocumentElement(), in);
        }
        new TraductorValenciano(procesoTraductor, in.toString())
                .completarValenciano(dom.getDocumentElement());
        return dom.getDocumentElement();
    }

    /** Si el documento (ya expandido) no trae <titulo>, se le pone uno al
     * principio del todo con el <name> del TramiteInstance.xml del trámite al
     * que pertenece. Solo se pone el castellano: el valenciano lo calcula
     * después el traductor, como el de cualquier otro texto. */
    void addTituloDelTramiteIfNotExists(Element raiz, File in) {
        for (Element e : children(raiz)) {
            if (e.getTagName().equals("titulo")) {
                return;
            }
        }

        Path tramiteInstance = tramitesLayout != null
                ? tramitesLayout.findTramiteInstanceAncestro(in.toPath())
                : TramitesLayout.buscarTramiteInstanceAncestroSinTope(in.toPath());
        if (tramiteInstance == null) {
            throw new RuntimeException("ERROR: " + in + " no lleva <titulo> y no hay ningún "
                    + TramiteInstanceFile.TRAMITE_XML_NAME + " en ninguna carpeta por encima de él:"
                    + " el título de un documento sin <titulo> es el <name> del trámite al que"
                    + " pertenece.");
        }

        Element titulo = raiz.getOwnerDocument().createElement("titulo");
        Element castellano = raiz.getOwnerDocument().createElement("castellano");
        castellano.setTextContent(new TramiteInstanceFileFinder(tramitesLayout).parse(tramiteInstance).getName());
        titulo.appendChild(castellano);
        raiz.insertBefore(titulo, raiz.getFirstChild());
    }

    // ------------------------------------------------------- estructura

    /** Comprueba lo que el XSD no puede expresar, que es distinto en cada tipo
     * de documento. Se hace sobre el documento COMPLETO, ignorando la
     * visibilidad: la que se aplica en runtime al colapsar puede dejar la
     * estructura incompleta, y eso es lícito. */
    static void validarEstructura(TipoDocumento tipo, Element raiz) {
        switch (tipo) {
            case FORMULARIO ->
                validarEstructuraFormulario(raiz);
            case TEXTO ->
                validarEstructuraTexto(raiz);
        }
    }

    /** Cada <fila> encaja en la rejilla de 12 columnas y siOculto solo
     * acompaña a visible. */
    static void validarEstructuraFormulario(Element raiz) {
        for (Element e : children(raiz)) {
            switch (e.getTagName()) {
                case "titulo":
                    break;
                case "seccion":
                    validarSiOcultoConVisible(e);
                    for (Element fila : children(e)) {
                        if (fila.getTagName().equals("valenciano")
                                || fila.getTagName().equals("castellano")) {
                            continue;
                        }
                        if (!fila.getTagName().equals("fila")) {
                            throw new RuntimeException("ERROR: <" + fila.getTagName()
                                    + "> desconocido dentro de <seccion>" + ubicacion(fila));
                        }
                        validarSiOcultoConVisible(fila);
                        validarFila(fila);
                    }
                    break;
                default:
                    throw new RuntimeException("ERROR: <" + e.getTagName()
                            + "> desconocido dentro de <" + TipoDocumento.FORMULARIO.getRaiz()
                            + ">" + ubicacion(e));
            }
        }
    }

    /** Cada <fila> de una <tabla> lleva exactamente un hijo por columna y
     * siOculto solo acompaña a visible. */
    static void validarEstructuraTexto(Element e) {
        validarSiOcultoConVisible(e);
        if (e.getTagName().equals("tabla")) {
            validarTabla(e);
        }
        for (Element hijo : children(e)) {
            validarEstructuraTexto(hijo);
        }
    }

    static void validarTabla(Element tabla) {
        int columnas = Integer.parseInt(tabla.getAttribute("columnas"));
        for (Element fila : children(tabla)) {
            int hijos = children(fila).size();
            if (hijos != columnas) {
                throw new RuntimeException("ERROR: una <fila> de una <tabla> de " + columnas
                        + " columnas tiene " + hijos + " hijos" + ubicacion(fila)
                        + ": cada fila debe llevar exactamente un hijo por columna:\n"
                        + toXml(fila));
            }
        }
    }

    static void validarFila(Element fila) {
        int cursor = 0;
        for (Element e : children(fila)) {
            String tag = e.getTagName();
            if (!tag.equals("campo") && !tag.equals("check") && !tag.equals("texto")) {
                throw new RuntimeException("ERROR: elemento <" + tag + "> desconocido dentro de <fila>"
                        + ubicacion(e) + ":\n" + toXml(fila));
            }
            validarSiOcultoConVisible(e);
            int units = (int) Math.round(Double.parseDouble(e.getAttribute("colspan")) * 100);
            if (cursor + units > FULL) {
                throw new RuntimeException("ERROR: un <" + tag + "> cruza el límite de 12 columnas"
                        + ubicacion(e) + ": su colspan es " + enColumnas(units)
                        + " y antes de él la línea ya lleva " + enColumnas(cursor)
                        + " de las 12 columnas:\n" + toXml(fila));
            }
            cursor += units;
            if (cursor == FULL) {
                cursor = 0;
            }
        }
        if (cursor != 0) {
            throw new RuntimeException("ERROR: los colspan de una <fila> no suman un múltiplo de 12"
                    + ubicacion(fila) + ": la última línea suma " + enColumnas(cursor)
                    + " de las 12 columnas:\n" + toXml(fila));
        }
    }

    static void validarSiOcultoConVisible(Element e) {
        if (!e.getAttribute("siOculto").isEmpty() && e.getAttribute("visible").isEmpty()) {
            throw new RuntimeException("ERROR: <" + e.getTagName() + "> lleva siOculto sin visible"
                    + ubicacion(e) + ": siOculto solo dice qué hacer cuando visible es false:\n" + toXml(e));
        }
    }

    /** Cuántas columnas de la rejilla de 12 son esas unidades, tal como se
     * escriben en el atributo colspan (1200 unidades = 12 columnas). */
    static String enColumnas(int units) {
        return units % 100 == 0 ? String.valueOf(units / 100) : String.valueOf(units / 100.0);
    }

    static List<Element> children(Element e) {
        List<Element> out = new ArrayList<>();
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                out.add((Element) nl.item(i));
            }
        }
        return out;
    }

    /** El sufijo con el que se marcan las palabras que el traductor no debe
     * traducir (siglas, nombres propios…) solo sirve para calcular el
     * valenciano: no debe llegar al XML resuelto que dibuja la aplicación. */
    static void quitarSufijoNoTraducir(Element e) {
        for (Element hijo : children(e)) {
            if (hijo.getTagName().equals("valenciano") || hijo.getTagName().equals("castellano")) {
                hijo.setTextContent(hijo.getTextContent().replace(Traductor.SUFIJO_NO_TRADUCIR, ""));
            } else {
                quitarSufijoNoTraducir(hijo);
            }
        }
    }

    /** Quita los nodos de texto que son solo espacio entre elementos, para que
     * la salida indentada no arrastre el formato original. Los textos de
     * &lt;valenciano&gt;/&lt;castellano&gt; no se tocan. */
    static void quitarEspaciosEntreElementos(Element e) {
        NodeList nl = e.getChildNodes();
        for (int i = nl.getLength() - 1; i >= 0; i--) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                quitarEspaciosEntreElementos((Element) n);
            } else if (n.getNodeType() == Node.TEXT_NODE && n.getTextContent().isBlank()
                    && !e.getTagName().equals("valenciano") && !e.getTagName().equals("castellano")) {
                e.removeChild(n);
            }
        }
    }

    static void escribir(Element raiz, File out) {
        try {
            out.getAbsoluteFile().getParentFile().mkdirs();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            t.transform(new DOMSource(raiz), new StreamResult(out));
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al escribir el XML resuelto: " + out, ex);
        }
    }

    // ------------------------------------------------------------ parseo

    /** Valida el fichero contra el esquema del tipo de documento y lo parsea
     * comprobando el elemento raíz: la del tipo para los documentos,
     * "fragmento" para los fragmentos _*.xml incluibles. */
    static Document parseValidated(File xml, String raiz, TipoDocumento tipo) {
        validate(new StreamSource(xml), xml.toString(), tipo);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document dom = dbf.newDocumentBuilder().parse(xml);
            anotarUbicaciones(dom, xml);
            if (!dom.getDocumentElement().getTagName().equals(raiz)) {
                throw new RuntimeException("ERROR: el elemento raíz de " + xml
                        + " debe ser <" + raiz + ">");
            }
            return dom;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al parsear el XML: " + xml, ex);
        }
    }

    /** Clave con la que se guarda en cada Element, como userData, el
     * "fichero:línea" del que salió. El DOM no conserva la posición y sin ella
     * un error de estructura no se sabe dónde hay que arreglarlo. */
    static final String UBICACION = "ubicacion";

    /** El " (fichero:línea)" de un elemento, para los mensajes de error, o ""
     * si no se pudo averiguar (elementos que no vienen de ningún fichero, como
     * el &lt;titulo&gt; que se le pone al documento con el nombre del trámite). */
    static String ubicacion(Element e) {
        String ubicacion = ubicacionO(e, null);
        return ubicacion == null ? "" : " (" + ubicacion + ")";
    }

    /** El "fichero:línea" pelado de un elemento, o {@code alternativa} si no
     * se sabe de dónde viene. */
    static String ubicacionO(Element e, String alternativa) {
        Object ubicacion = e.getUserData(UBICACION);
        return ubicacion == null ? alternativa : ubicacion.toString();
    }

    /** Anota en cada Element el fichero y la línea de los que viene. El DOM no
     * guarda la posición, así que se hace una segunda pasada con SAX (que sí
     * tiene Locator) y se emparejan los elementos: SAX los abre exactamente en
     * el mismo orden en el que los visita el recorrido en preorden del DOM. Si
     * algo no cuadra se deja sin anotar: esto es solo para los mensajes de
     * error, nunca debe romper la resolución del documento. */
    static void anotarUbicaciones(Document dom, File xml) {
        List<Integer> lineas = new ArrayList<>();
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.newSAXParser().parse(xml, new DefaultHandler() {
                private Locator locator;

                @Override
                public void setDocumentLocator(Locator locator) {
                    this.locator = locator;
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    lineas.add(locator == null ? -1 : locator.getLineNumber());
                }
            });
        } catch (Exception ex) {
            return;
        }
        List<Element> elementos = new ArrayList<>();
        recogerEnPreorden(dom.getDocumentElement(), elementos);
        if (elementos.size() != lineas.size()) {
            return;
        }
        for (int i = 0; i < elementos.size(); i++) {
            elementos.get(i).setUserData(UBICACION,
                    xml.getAbsoluteFile().toPath().normalize() + ":" + lineas.get(i), null);
        }
    }

    static void recogerEnPreorden(Element e, List<Element> out) {
        out.add(e);
        for (Element hijo : children(e)) {
            recogerEnPreorden(hijo, out);
        }
    }

    /** importNode no copia el userData, así que la ubicación de los elementos
     * que vienen de un fragmento hay que arrastrarla a mano al importarlos. */
    static void copiarUbicaciones(Element origen, Element destino) {
        destino.setUserData(UBICACION, origen.getUserData(UBICACION), null);
        List<Element> hijosOrigen = children(origen);
        List<Element> hijosDestino = children(destino);
        for (int i = 0; i < hijosOrigen.size() && i < hijosDestino.size(); i++) {
            copiarUbicaciones(hijosOrigen.get(i), hijosDestino.get(i));
        }
    }

    /** El XML de un elemento, tal cual, para enseñar en un mensaje de error
     * qué trozo del documento es el que está mal. */
    static String toXml(Element e) {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter out = new StringWriter();
            t.transform(new DOMSource(e), new StreamResult(out));
            return out.toString().trim();
        } catch (Exception ex) {
            return "<" + e.getTagName() + "> (no se pudo serializar: " + ex.getMessage() + ")";
        }
    }

    /** Sustituye cada <include href="..."/> (hijo directo de la raíz del
     * documento o de un <fragmento>) por los hijos de la raíz <fragmento> del
     * fichero incluido, recursivamente si el fragmento tiene a su vez otros
     * <include>. El href se resuelve relativo al fichero que lo incluye. */
    void expandIncludes(Element raiz, Path fichero, List<Path> cadena, TipoDocumento tipo) {
        cadena.add(fichero);
        for (Element e : children(raiz)) {
            if (!e.getTagName().equals("include")) {
                continue;
            }
            Path fragmento = fichero.getParent().resolve(e.getAttribute("href")).normalize();
            if (cadena.contains(fragmento)) {
                throw new RuntimeException("ERROR: ciclo de includes" + ubicacion(e) + ": "
                        + cadena + " -> " + fragmento);
            }
            if (!fragmento.toFile().isFile()) {
                throw new RuntimeException("ERROR: " + fichero
                        + " incluye un fragmento que no existe" + ubicacion(e) + ": " + fragmento);
            }
            Document dom = parseValidated(fragmento.toFile(), "fragmento", tipo);
            expandIncludes(dom.getDocumentElement(), fragmento, cadena, tipo);
            for (Element hijo : children(dom.getDocumentElement())) {
                Element importado = (Element) raiz.getOwnerDocument().importNode(hijo, true);
                copiarUbicaciones(hijo, importado);
                raiz.insertBefore(importado, e);
            }
            raiz.removeChild(e);
        }
        cadena.remove(cadena.size() - 1);
    }


    /** Valida contra el esquema del tipo de documento incluido en el jar (el
     * xsi:noNamespaceSchemaLocation del documento no se usa: la validación es
     * siempre contra el esquema local, sin acceso a red). */
    static void validate(Source xml, String descripcion, TipoDocumento tipo) {
        try (InputStream xsd = DocumentoXmlResolver.class.getResourceAsStream(tipo.getXsd())) {
            if (xsd == null) {
                throw new RuntimeException("ERROR: falta el recurso " + tipo.getXsd() + " en el jar");
            }
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(new StreamSource(xsd))
                    .newValidator().validate(xml);
        } catch (Exception ex) {
            throw new RuntimeException("ERROR: el XML no valida contra " + tipo.getXsd() + ": "
                    + descripcion + lineaYColumna(ex) + ": " + ex.getMessage(), ex);
        }
    }

    /** La ":línea:columna" que trae el error del validador, si la trae. Al
     * validar el documento ya expandido se valida un DOM, que no tiene
     * posiciones, y entonces no hay nada que añadir. */
    static String lineaYColumna(Exception ex) {
        if (!(ex instanceof SAXParseException spe) || spe.getLineNumber() <= 0) {
            return "";
        }
        return ":" + spe.getLineNumber()
                + (spe.getColumnNumber() > 0 ? ":" + spe.getColumnNumber() : "");
    }
}
