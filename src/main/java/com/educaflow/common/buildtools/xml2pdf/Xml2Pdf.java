package com.educaflow.common.buildtools.xml2pdf;

import com.educaflow.common.buildtools.common.Traductor;
import com.educaflow.common.buildtools.files.tramite.TramiteInstanceFile;
import com.educaflow.common.buildtools.files.tramite.TramiteInstanceFileFinder;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.base14.Helvetica;
import org.apache.fop.fonts.base14.HelveticaBold;
import org.apache.fop.fonts.base14.HelveticaBoldOblique;
import org.apache.fop.fonts.base14.HelveticaOblique;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontDescriptor;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFont;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFStream;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Genera el formulario PDF rellenable directamente desde el XML de definición
 * (mismo formato que xml2odt.py) usando la librería PDF de Apache FOP
 * (org.apache.fop.pdf), sin pasar por el .odt ni necesitar LibreOffice.
 */
public class Xml2Pdf {

    static final double CM = 72.0 / 2.54;
    static final double PAGE_W = 21.0 * CM;
    static final double PAGE_H = 29.7 * CM;
    static final double MARGIN = 1.0 * CM;
    static final double TABLE_W = 19.0 * CM;
    static final int FULL = 1200;
    static final double LETRA_W_CM = 0.8;
    static final int LETRA_EDGE = (int) Math.round(LETRA_W_CM * FULL / 19.0);
    static final int LOGO_EDGE = 290;

    static final double CTL_H = 0.541 * CM;
    static final double EXTRA_H = 0.62 * CM;
    static final double LABEL_H = 0.31 * CM;
    static final double LABEL_VACIO = 0.1 * CM;
    static final double ROW_CHECK = 0.818 * CM;
    static final double ROW_TITULO = 1.901 * CM;
    static final double ROW_SECCION = 0.649 * CM;
    static final double PAD = 0.101 * CM;
    static final double LABEL_PAD_TOP = 0.09 * CM;   // aire entre la etiqueta de un campo y el borde superior
    static final double CHECK_SIDE = 0.35 * CM;
    static final double CHECK_COL_W = 0.9 * CM;      // espacio reservado a la casilla antes de la etiqueta
    static final double CHECK_LABEL_GAP = 0.25 * CM; // separación entre la casilla y sus etiquetas
    static final double LOGO_W = 4.343 * CM;
    static final double LOGO_H = 1.939 * CM;

    static final Pattern INLINE = Pattern.compile("\\$\\{([^;{}]+);([0-9]+(?:\\.[0-9]+)?)\\}");
    static final Locale ES = new Locale("es", "ES");
    static final String TRADUCTOR_POR_DEFECTO = "apertium";

    // 0=regular, 1=cursiva, 2=seminegrita, 3=seminegrita cursiva
    final Typeface[] faces = new Typeface[4];
    static final String[] FONT_RES = {"F1", "F2", "F3", "F4"};
    static final String[] ROBOTO_TTF = {"Roboto-Regular.ttf", "Roboto-Italic.ttf",
                                        "Roboto-SemiBold.ttf", "Roboto-SemiBoldItalic.ttf"};

    PDFDocument doc;
    PDFResources res;
    PDFPage page;
    final PDFFont[] pdfFonts = new PDFFont[4];
    StringBuilder sb;                     // content stream de la página actual
    int pageIndex;
    double cursorY;                       // borde superior libre (coordenadas PDF)
    final Map<String, List<PDFDictionary>> widgetsPorNombre = new LinkedHashMap<>();
    final Map<String, PDFName> tipoPorNombre = new LinkedHashMap<>();
    PDFStream apOff;
    PDFStream apYes;
    byte[] logoPng;

    /** Proceso traductor externo con el que se calcula el &lt;valenciano&gt; de
     * los textos que solo llevan &lt;castellano&gt;. */
    final String procesoTraductor;

    /** La estructura de carpetas de los trámites, con la que se busca el
     * TramiteInstance.xml del trámite al que pertenece el documento sin subir
     * nunca por encima del paquete raíz. Es null cuando se invoca a mano
     * (Xml2Pdf.main): ahí no se sabe cuál es la raíz de fuentes y la búsqueda
     * se hace sin tope. */
    final TramitesLayout tramitesLayout;

    Xml2Pdf(String procesoTraductor) {
        this(procesoTraductor, null);
    }

    Xml2Pdf(String procesoTraductor, TramitesLayout tramitesLayout) {
        this.procesoTraductor = procesoTraductor;
        this.tramitesLayout = tramitesLayout;
    }

    // ------------------------------------------------------------- utilidades

    static double width(Typeface f, String s, double size) {
        double w = 0;
        for (int i = 0; i < s.length(); i++) {
            char m = f.mapChar(s.charAt(i));
            w += f.getWidth(m, 1) / 1000.0 * size;
        }
        return w;
    }

    double width(int font, String s, double size) {
        return width(faces[font], s, size);
    }

    static String num(double d) {
        String s = String.format(Locale.ROOT, "%.2f", d);
        return s;
    }

    /** Codifica un texto como cadena PDF entre paréntesis usando la codificación
     * de la fuente (mapChar marca además los glifos como usados para incrustar). */
    static String pdfText(String s, Typeface f) {
        StringBuilder out = new StringBuilder("(");
        for (int i = 0; i < s.length(); i++) {
            int c = f.mapChar(s.charAt(i));
            if (c > 255) {
                c = '?';
            }
            if (c == '(' || c == ')' || c == '\\') {
                out.append('\\').append((char) c);
            } else if (c >= 32 && c < 127) {
                out.append((char) c);
            } else {
                out.append(String.format("\\%03o", c));
            }
        }
        return out.append(')').toString();
    }

    // --------------------------------------------------------------- tokens

    static final int T_WORD = 0;
    static final int T_SPACE = 1;
    static final int T_FIELD = 2;

    static class Tok {
        int type;
        String text;
        int font;
        double size;
        double w;
        String fieldName;
    }

    List<Tok> tokenize(String text, int font, double size, boolean upper) {
        List<Tok> toks = new ArrayList<>();
        Matcher m = INLINE.matcher(text);
        int last = 0;
        while (m.find()) {
            addWords(toks, text.substring(last, m.start()), font, size, upper);
            Tok t = new Tok();
            t.type = T_FIELD;
            t.fieldName = m.group(1);
            t.w = Double.parseDouble(m.group(2)) * TABLE_W / 12.0;
            toks.add(t);
            last = m.end();
        }
        addWords(toks, text.substring(last), font, size, upper);
        return toks;
    }

    void addWords(List<Tok> toks, String text, int font, double size, boolean upper) {
        if (upper) {
            text = text.toUpperCase(ES);
        }
        for (String part : text.split("(?<= )|(?= )")) {
            if (part.isEmpty()) {
                continue;
            }
            Tok t = new Tok();
            t.type = part.equals(" ") ? T_SPACE : T_WORD;
            t.text = part;
            t.font = font;
            t.size = size;
            t.w = width(font, part, size);
            toks.add(t);
        }
    }

    static class Placed {
        Tok tok;
        double x;
    }

    static class Line {
        List<Placed> items = new ArrayList<>();
        double w;
    }

    /** Ajuste greedy de tokens a líneas de ancho maxW. */
    static List<Line> layout(List<Tok> toks, double maxW) {
        List<Line> lines = new ArrayList<>();
        Line line = new Line();
        for (Tok t : toks) {
            if (t.type == T_SPACE && line.items.isEmpty()) {
                continue;
            }
            if (line.w + t.w > maxW + 0.1 && !line.items.isEmpty() && t.type != T_SPACE) {
                lines.add(line);
                line = new Line();
            }
            Placed p = new Placed();
            p.tok = t;
            p.x = line.w;
            line.items.add(p);
            line.w += t.w;
        }
        if (!line.items.isEmpty() || lines.isEmpty()) {
            lines.add(line);
        }
        return lines;
    }

    // --------------------------------------------------------------- dibujo

    void drawLines(List<Line> lines, double x, double topY, double lineH,
                   double maxW, char align) {
        double y = topY;
        for (Line line : lines) {
            double base = y - lineH * 0.82;
            double dx = align == 'c' ? (maxW - line.w) / 2 : 0;
            for (Placed p : line.items) {
                Tok t = p.tok;
                if (t.type == T_FIELD) {
                    double fy = base + lineH * 0.32 - CTL_H / 2;
                    addWidget(t.fieldName, new PDFName("Tx"), x + dx + p.x, fy, t.w, CTL_H);
                } else {
                    sb.append("BT /").append(FONT_RES[t.font]).append(' ')
                      .append(num(t.size)).append(" Tf ")
                      .append(num(x + dx + p.x)).append(' ').append(num(base))
                      .append(" Td ").append(pdfText(t.text, faces[t.font])).append(" Tj ET\n");
                }
            }
            y -= lineH;
        }
    }

    void strokeLine(double x1, double y1, double x2, double y2) {
        sb.append("0.5 w 0 G ").append(num(x1)).append(' ').append(num(y1)).append(" m ")
          .append(num(x2)).append(' ').append(num(y2)).append(" l S\n");
    }

    void fillRect(double x, double y, double w, double h, double gray) {
        sb.append(num(gray)).append(" g ").append(num(x)).append(' ').append(num(y)).append(' ')
          .append(num(w)).append(' ').append(num(h)).append(" re f 0 g\n");
    }

    void cellBorders(double x, double yTop, double w, double h, boolean top, boolean bottom) {
        strokeLine(x, yTop - h, x, yTop);
        strokeLine(x + w, yTop - h, x + w, yTop);
        if (top) {
            strokeLine(x, yTop, x + w, yTop);
        }
        if (bottom) {
            strokeLine(x, yTop - h, x + w, yTop - h);
        }
    }

    double unitsX(double units) {
        return MARGIN + units * TABLE_W / FULL;
    }

    // --------------------------------------------------------------- widgets

    void addWidget(String nombre, PDFName ft, double x, double y, double w, double h) {
        PDFDictionary a = new PDFDictionary();
        a.put("Type", new PDFName("Annot"));
        a.put("Subtype", new PDFName("Widget"));
        a.put("F", 4);
        PDFArray rect = new PDFArray();
        rect.add(Double.valueOf(num(x)));
        rect.add(Double.valueOf(num(y)));
        rect.add(Double.valueOf(num(x + w)));
        rect.add(Double.valueOf(num(y + h)));
        a.put("Rect", rect);
        if (ft.toString().equals("/Btn")) {
            PDFDictionary mk = new PDFDictionary();
            PDFArray bc = new PDFArray();
            bc.add(0);
            mk.put("BC", bc);
            PDFArray bg = new PDFArray();
            bg.add(1);
            mk.put("BG", bg);
            a.put("MK", mk);
            PDFDictionary bs = new PDFDictionary();
            bs.put("W", 0.6);
            bs.put("S", new PDFName("S"));
            a.put("BS", bs);
            PDFDictionary ap = new PDFDictionary();
            PDFDictionary n = new PDFDictionary();
            n.put("Off", apOff.makeReference());
            n.put("Yes", apYes.makeReference());
            ap.put("N", n);
            a.put("AP", ap);
            a.put("AS", new PDFName("Off"));
        }
        doc.registerObject(a);
        a.put("P", page.makeReference());
        page.addAnnotation(a);
        widgetsPorNombre.computeIfAbsent(nombre, k -> new ArrayList<>()).add(a);
        tipoPorNombre.put(nombre, ft);
    }

    void makeCheckboxAppearances() {
        double s = CHECK_SIDE;
        String box = "q 0.6 w 0 G 0.3 0.3 " + num(s - 0.6) + " " + num(s - 0.6) + " re S Q\n";
        String cross = "q 0.9 w 0 G 1.6 1.6 m " + num(s - 1.6) + " " + num(s - 1.6) + " l S "
                + num(s - 1.6) + " 1.6 m 1.6 " + num(s - 1.6) + " l S Q\n";
        apOff = makeFormXObject(box, s, s);
        apYes = makeFormXObject(box + cross, s, s);
    }

    PDFStream makeFormXObject(String content, double w, double h) {
        try {
            PDFStream st = new PDFStream();
            st.put("Type", new PDFName("XObject"));
            st.put("Subtype", new PDFName("Form"));
            PDFArray bbox = new PDFArray();
            bbox.add(0);
            bbox.add(0);
            bbox.add(Double.valueOf(num(w)));
            bbox.add(Double.valueOf(num(h)));
            st.put("BBox", bbox);
            st.add(content);
            doc.registerObject(st);
            return st;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------- imagen

    PDFStream makeLogoXObject() throws Exception {
        BufferedImage src;
        try (InputStream is = Xml2Pdf.class.getResourceAsStream("assets/logo-gva.png")) {
            if (is == null) {
                throw new RuntimeException("ERROR: falta el recurso assets/logo-gva.png en el jar");
            }
            src = ImageIO.read(is);
        }
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        byte[] raw = new byte[rgb.getWidth() * rgb.getHeight() * 3];
        int i = 0;
        for (int y = 0; y < rgb.getHeight(); y++) {
            for (int x = 0; x < rgb.getWidth(); x++) {
                int p = rgb.getRGB(x, y);
                raw[i++] = (byte) ((p >> 16) & 0xff);
                raw[i++] = (byte) ((p >> 8) & 0xff);
                raw[i++] = (byte) (p & 0xff);
            }
        }
        PDFStream st = new PDFStream();
        st.put("Type", new PDFName("XObject"));
        st.put("Subtype", new PDFName("Image"));
        st.put("Width", rgb.getWidth());
        st.put("Height", rgb.getHeight());
        st.put("ColorSpace", new PDFName("DeviceRGB"));
        st.put("BitsPerComponent", 8);
        st.setData(raw);
        doc.registerObject(st);
        return st;
    }

    // ------------------------------------------------------------ estructura

    static class Hoja {
        String tag;
        String nombreCampo;
        double rowSpan = 1;
        String val = "";
        String cast = "";
        int startUnits;
        int endUnits;
    }

    static List<List<Hoja>> partition(Element fila) {
        List<List<Hoja>> lines = new ArrayList<>();
        List<Hoja> line = new ArrayList<>();
        int cursor = 0;
        for (Element e : children(fila)) {
            String tag = e.getTagName();
            if (!tag.equals("campo") && !tag.equals("check") && !tag.equals("texto")) {
                throw new RuntimeException("ERROR: elemento <" + tag + "> desconocido dentro de <fila>");
            }
            Hoja h = new Hoja();
            h.tag = tag;
            h.nombreCampo = e.getAttribute("nombreCampo");
            if (!e.getAttribute("rowSpan").isEmpty()) {
                h.rowSpan = Double.parseDouble(e.getAttribute("rowSpan"));
            }
            h.val = childText(e, "valenciano");
            h.cast = childText(e, "castellano");
            int units = (int) Math.round(Double.parseDouble(e.getAttribute("colspan")) * 100);
            if (cursor + units > FULL) {
                throw new RuntimeException("ERROR: un <" + tag + "> cruza el límite de 12 columnas");
            }
            h.startUnits = cursor;
            h.endUnits = cursor + units;
            line.add(h);
            cursor += units;
            if (cursor == FULL) {
                lines.add(line);
                line = new ArrayList<>();
                cursor = 0;
            }
        }
        if (!line.isEmpty()) {
            throw new RuntimeException("ERROR: los colspan de una <fila> no suman un múltiplo de 12");
        }
        return lines;
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

    /** Texto de un hijo <valenciano>/<castellano>. El sufijo con el que se
     * marcan las palabras que el traductor no debe traducir (siglas, nombres
     * propios…) no se dibuja: solo sirve para calcular el valenciano. */
    static String childText(Element e, String name) {
        for (Element c : children(e)) {
            if (c.getTagName().equals(name)) {
                Node t = c.getFirstChild();
                return t == null ? "" : t.getTextContent().trim()
                        .replace(Traductor.SUFIJO_NO_TRADUCIR, "");
            }
        }
        if (!e.getAttribute(name).isEmpty()) {
            throw new RuntimeException("ERROR: <" + e.getTagName() + "> lleva '" + name
                    + "' como atributo (formato antiguo); usa el elemento hijo <" + name + ">");
        }
        return "";
    }

    // -------------------------------------------------------------- páginas

    void newPage() {
        page = doc.getFactory().makePage(res, pageIndex++,
                new Rectangle2D.Double(0, 0, PAGE_W, PAGE_H),
                new Rectangle2D.Double(0, 0, PAGE_W, PAGE_H),
                new Rectangle2D.Double(0, 0, PAGE_W, PAGE_H),
                new Rectangle2D.Double(0, 0, PAGE_W, PAGE_H));
        doc.addObject(page);
        sb = new StringBuilder();
        cursorY = PAGE_H - MARGIN;
    }

    void finishPage() throws Exception {
        PDFStream stream = new PDFStream();
        stream.add(sb.toString());
        doc.registerObject(stream);
        page.setContents(new PDFReference(stream));
        if (page.getAnnotations() != null) {
            doc.addObject(page.getAnnotations());
        }
    }

    void ensureRoom(double h) throws Exception {
        if (cursorY - h < MARGIN) {
            finishPage();
            newPage();
        }
    }

    // ------------------------------------------------------------------ main

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Uso: Xml2Pdf entrada.xml salida.pdf [rutaExecTraductor]");
            System.exit(1);
        }
        new Xml2Pdf(args.length >= 3 ? args[2] : TRADUCTOR_POR_DEFECTO).run(args[0], args[1]);
    }

    void run(String in, String out) throws Exception {
        Element root = loadDocumento(new File(in));

        doc = new PDFDocument("secretaria-virtual/xml2pdf (Apache FOP PDF library)");
        res = doc.getResources();
        setupFaces();
        makeCheckboxAppearances();
        PDFStream logo = makeLogoXObject();
        PDFDictionary xobj = new PDFDictionary();
        xobj.put("Im1", logo.makeReference());
        res.put("XObject", xobj);

        newPage();
        char letra = 'A';
        for (Element e : children(root)) {
            switch (e.getTagName()) {
                case "titulo":
                    drawTitulo(e);
                    break;
                case "seccion":
                    drawSeccionHeader(e, letra);
                    letra++;
                    for (Element fila : children(e)) {
                        if (fila.getTagName().equals("valenciano")
                                || fila.getTagName().equals("castellano")) {
                            continue;
                        }
                        if (!fila.getTagName().equals("fila")) {
                            throw new RuntimeException("ERROR: <" + fila.getTagName()
                                    + "> desconocido dentro de <seccion>");
                        }
                        List<List<Hoja>> lines = partition(fila);
                        for (int i = 0; i < lines.size(); i++) {
                            drawLinea(lines.get(i), i > 0, i < lines.size() - 1);
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("ERROR: <" + e.getTagName()
                            + "> desconocido dentro de <documento>");
            }
        }
        finishPage();
        buildFonts();
        buildAcroForm();

        try (OutputStream os = new FileOutputStream(out)) {
            doc.outputHeader(os);
            doc.output(os);
            doc.outputTrailer(os);
        }
        System.out.println("Generado " + out);
    }

    /** Carga el XML de un documento: valida el fichero contra el esquema,
     * expande recursivamente sus <include href="_x.xml"/>, valida también el
     * documento resultante de la expansión (p.ej. un segundo <titulo>
     * aportado por un fragmento), le pone el <titulo> del trámite si no trae
     * ninguno y completa con el traductor los <valenciano> que falten. */
    Element loadDocumento(File in) {
        Document dom = parseValidated(in, "documento");
        expandIncludes(dom.getDocumentElement(),
                in.getAbsoluteFile().toPath().normalize(), new ArrayList<>());
        validate(new DOMSource(dom), in + " (expandido con sus includes)");
        addTituloDelTramiteIfNotExists(dom.getDocumentElement(), in);
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

    /** Valida el fichero contra documento.xsd y lo parsea comprobando el
     * elemento raíz: "documento" para los documentos, "fragmento" para los
     * fragmentos _*.xml incluibles. */
    static Document parseValidated(File xml, String raiz) {
        validate(new StreamSource(xml), xml.toString());
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document dom = dbf.newDocumentBuilder().parse(xml);
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

    /** Sustituye cada <include href="..."/> (hijo directo de <documento> o de
     * <fragmento>) por los hijos de la raíz <fragmento> del fichero incluido,
     * recursivamente si el fragmento tiene a su vez otros <include>. El href
     * se resuelve relativo al fichero que lo incluye. */
    static void expandIncludes(Element raiz, Path fichero, List<Path> cadena) {
        cadena.add(fichero);
        for (Element e : children(raiz)) {
            if (!e.getTagName().equals("include")) {
                continue;
            }
            Path fragmento = fichero.getParent().resolve(e.getAttribute("href")).normalize();
            if (cadena.contains(fragmento)) {
                throw new RuntimeException("ERROR: ciclo de includes: " + cadena
                        + " -> " + fragmento);
            }
            if (!fragmento.toFile().isFile()) {
                throw new RuntimeException("ERROR: " + fichero
                        + " incluye un fragmento que no existe: " + fragmento);
            }
            Document dom = parseValidated(fragmento.toFile(), "fragmento");
            expandIncludes(dom.getDocumentElement(), fragmento, cadena);
            for (Element hijo : children(dom.getDocumentElement())) {
                raiz.insertBefore(raiz.getOwnerDocument().importNode(hijo, true), e);
            }
            raiz.removeChild(e);
        }
        cadena.remove(cadena.size() - 1);
    }

    /** Valida contra el esquema documento.xsd incluido en el jar (el
     * xsi:noNamespaceSchemaLocation del documento no se usa: la validación es
     * siempre contra el esquema local, sin acceso a red). */
    static void validate(Source xml, String descripcion) {
        try (InputStream xsd = Xml2Pdf.class.getResourceAsStream("documento.xsd")) {
            if (xsd == null) {
                throw new RuntimeException("ERROR: falta el recurso documento.xsd en el jar");
            }
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(new StreamSource(xsd))
                    .newValidator().validate(xml);
        } catch (Exception ex) {
            throw new RuntimeException("ERROR: el XML no valida contra documento.xsd: "
                    + descripcion + ": " + ex.getMessage(), ex);
        }
    }

    // ------------------------------------------------------------------ filas

    void drawTitulo(Element e) throws Exception {
        String val = childText(e, "valenciano");
        String cast = childText(e, "castellano");
        double titleW = (FULL - LOGO_EDGE) * TABLE_W / FULL - 2 * PAD;
        List<Line> lv = layout(tokenize(val, 2, 11, true), titleW);
        List<Line> lc = layout(tokenize(cast, 3, 11, true), titleW);
        double lineH = 11 * 1.15;
        double textH = (val.isEmpty() ? 0 : lv.size() * lineH) + (cast.isEmpty() ? 0 : lc.size() * lineH);
        // la fila crece para que el logo no pise los bordes (como hace LibreOffice)
        double h = Math.max(Math.max(ROW_TITULO, LOGO_H + 2 * PAD), textH + 2 * PAD);
        ensureRoom(h);
        double top = cursorY;
        double xLogo = unitsX(0);
        double wLogo = LOGO_EDGE * TABLE_W / FULL;
        cellBorders(xLogo, top, wLogo, h, true, true);
        cellBorders(unitsX(LOGO_EDGE), top, TABLE_W - wLogo, h, true, true);
        // logo centrado en su celda
        double ix = xLogo + (wLogo - LOGO_W) / 2;
        double iy = top - h + (h - LOGO_H) / 2;
        sb.append("q ").append(num(LOGO_W)).append(" 0 0 ").append(num(LOGO_H)).append(' ')
          .append(num(ix)).append(' ').append(num(iy)).append(" cm /Im1 Do Q\n");
        double ty = top - (h - textH) / 2;
        if (!val.isEmpty()) {
            drawLines(lv, unitsX(LOGO_EDGE) + PAD, ty, lineH, titleW, 'c');
            ty -= lv.size() * lineH;
        }
        if (!cast.isEmpty()) {
            drawLines(lc, unitsX(LOGO_EDGE) + PAD, ty, lineH, titleW, 'c');
        }
        cursorY -= h;
    }

    void drawSeccionHeader(Element e, char letra) throws Exception {
        String val = childText(e, "valenciano");
        String cast = childText(e, "castellano");
        double textW = (FULL - LETRA_EDGE) * TABLE_W / FULL - 2 * PAD;
        double lineH = 9 * 0.85;
        double padTop = 0.12 * CM;              // aire entre el valenciano y el borde superior
        double gapIdiomas = 0.02 * CM;          // separación entre valenciano y castellano
        // padTop + gapIdiomas suman 0.14: si se ajusta uno, compensar el otro
        // para no cambiar el alto de la fila
        List<Line> lv = layout(tokenize(val, 2, 9, true), textW);
        List<Line> lc = layout(tokenize(cast, 3, 9, true), textW);
        double textH = (val.isEmpty() ? 0 : lv.size() * lineH) + (cast.isEmpty() ? 0 : lc.size() * lineH)
                + (!val.isEmpty() && !cast.isEmpty() ? gapIdiomas : 0);
        double h = Math.max(ROW_SECCION, padTop + textH + 0.049 * CM);
        ensureRoom(h);
        double top = cursorY;
        double wLetra = LETRA_EDGE * TABLE_W / FULL;
        fillRect(unitsX(0), top - h, wLetra, h, 0.6);
        cellBorders(unitsX(0), top, wLetra, h, true, true);
        cellBorders(unitsX(LETRA_EDGE), top, TABLE_W - wLetra, h, true, true);
        String s = String.valueOf(letra);
        double lw = width(0, s, 16);
        sb.append("BT /F1 16 Tf ").append(num(unitsX(0) + (wLetra - lw) / 2)).append(' ')
          .append(num(top - h + (h - 16 * 0.72) / 2)).append(" Td ")
          .append(pdfText(s, faces[0])).append(" Tj ET\n");
        double ty = top - padTop;
        if (!val.isEmpty()) {
            drawLines(lv, unitsX(LETRA_EDGE) + PAD, ty, lineH, textW, 'l');
            ty -= lv.size() * lineH + gapIdiomas;
        }
        if (!cast.isEmpty()) {
            drawLines(lc, unitsX(LETRA_EDGE) + PAD, ty, lineH, textW, 'l');
        }
        cursorY -= h;
    }

    /** Separación valenciano/castellano en la etiqueta de un check: amplia si
     * lleva campos inline ${...} (para que sus huecos no se toquen), estrecha
     * si es solo texto. */
    static double gapIdiomas(Hoja h) {
        boolean tieneFields = INLINE.matcher(h.val).find() || INLINE.matcher(h.cast).find();
        return (tieneFields ? 0.2 : 0.06) * CM;
    }

    void drawLinea(List<Hoja> line, boolean noTop, boolean noBottom) throws Exception {
        double lineH7 = 7 * 1.16;
        boolean soloChecks = line.stream().allMatch(h -> h.tag.equals("check"));

        // --- primera pasada: altura de la fila y layouts por celda
        double rowH = 0;
        Map<Hoja, List<Line>[]> parrafos = new LinkedHashMap<>();
        for (Hoja h : line) {
            double cellW = (h.endUnits - h.startUnits) * TABLE_W / FULL;
            double extra = (h.rowSpan - 1) * EXTRA_H;
            if (h.tag.equals("campo")) {
                double ctlH = CTL_H + extra;
                if (!h.val.isEmpty() || !h.cast.isEmpty()) {
                    List<Tok> toks = tokenize(h.val, 0, 7, true);
                    if (!h.cast.isEmpty()) {
                        addWords(toks, h.val.isEmpty() ? "" : " / ", 0, 7, false);
                        toks.addAll(tokenize(h.cast, 1, 7, true));
                    }
                    List<Line> label = layout(toks, cellW - 2 * PAD);
                    parrafos.put(h, new List[]{label});
                    double labelH = LABEL_PAD_TOP + label.size() * 7 * 0.9 + 0.03 * CM;
                    rowH = Math.max(rowH, labelH + ctlH + 0.03 * CM);
                } else {
                    rowH = Math.max(rowH, ctlH + 2 * LABEL_VACIO);
                }
            } else if (h.tag.equals("check")) {
                double base = soloChecks ? ROW_CHECK : ROW_CHECK;
                List<Line> lv = layout(tokenize(h.val, 0, 7, false), cellW - CHECK_COL_W - CHECK_LABEL_GAP - PAD);
                List<Line> lc = layout(tokenize(h.cast, 1, 7, false), cellW - CHECK_COL_W - CHECK_LABEL_GAP - PAD);
                parrafos.put(h, new List[]{lv, lc});
                double textH = (h.val.isEmpty() ? 0 : lv.size() * lineH7)
                        + (h.cast.isEmpty() ? 0 : lc.size() * lineH7)
                        + (!h.val.isEmpty() && !h.cast.isEmpty() ? gapIdiomas(h) : 0);
                rowH = Math.max(rowH, Math.max(base + extra, textH + 2 * PAD));
            } else {
                List<Line> lv = layout(tokenize(h.val, 0, 7, false), cellW - 2 * PAD);
                List<Line> lc = layout(tokenize(h.cast, 1, 7, false), cellW - 2 * PAD);
                parrafos.put(h, new List[]{lv, lc});
                double textH = (h.val.isEmpty() ? 0 : lv.size() * lineH7)
                        + (h.cast.isEmpty() ? 0 : lc.size() * lineH7);
                double min = h.rowSpan > 1 ? 0.5 * CM * h.rowSpan : 0;
                rowH = Math.max(rowH, Math.max(textH + 2 * PAD, min));
            }
        }

        ensureRoom(rowH);
        double top = cursorY;

        // --- segunda pasada: dibujo
        for (Hoja h : line) {
            double x = unitsX(h.startUnits);
            double cellW = (h.endUnits - h.startUnits) * TABLE_W / FULL;
            double extra = (h.rowSpan - 1) * EXTRA_H;
            cellBorders(x, top, cellW, rowH, !noTop, !noBottom);
            if (h.tag.equals("campo")) {
                double ctlH = CTL_H + extra;
                double fx = x + PAD;
                double fw = cellW - 0.28 * CM;
                if (parrafos.containsKey(h)) {
                    List<Line> label = parrafos.get(h)[0];
                    drawLines(label, x + PAD, top - LABEL_PAD_TOP, 7 * 0.9, cellW - 2 * PAD, 'l');
                    addWidget(h.nombreCampo, new PDFName("Tx"), fx, top - rowH + 0.03 * CM, fw, ctlH);
                } else {
                    addWidget(h.nombreCampo, new PDFName("Tx"), fx,
                            top - rowH + (rowH - ctlH) / 2, fw, ctlH);
                }
            } else if (h.tag.equals("check")) {
                double bx = x + CHECK_COL_W - CHECK_SIDE;
                addWidget(h.nombreCampo, new PDFName("Btn"), bx,
                        top - rowH / 2 - CHECK_SIDE / 2, CHECK_SIDE, CHECK_SIDE);
                if (h.endUnits - h.startUnits > 100) {
                    List<Line>[] ps = parrafos.get(h);
                    double textH = (h.val.isEmpty() ? 0 : ps[0].size() * lineH7)
                            + (h.cast.isEmpty() ? 0 : ps[1].size() * lineH7)
                            + (!h.val.isEmpty() && !h.cast.isEmpty() ? gapIdiomas(h) : 0);
                    double ty = top - (rowH - textH) / 2;
                    if (!h.val.isEmpty()) {
                        drawLines(ps[0], x + CHECK_COL_W + CHECK_LABEL_GAP, ty, lineH7,
                                cellW - CHECK_COL_W - CHECK_LABEL_GAP - PAD, 'l');
                        ty -= ps[0].size() * lineH7 + gapIdiomas(h);
                    }
                    if (!h.cast.isEmpty()) {
                        drawLines(ps[1], x + CHECK_COL_W + CHECK_LABEL_GAP, ty, lineH7,
                                cellW - CHECK_COL_W - CHECK_LABEL_GAP - PAD, 'l');
                    }
                }
            } else {
                List<Line>[] ps = parrafos.get(h);
                double ty = top - PAD;
                if (!h.val.isEmpty()) {
                    drawLines(ps[0], x + PAD, ty, lineH7, cellW - 2 * PAD, 'l');
                    ty -= ps[0].size() * lineH7;
                }
                if (!h.cast.isEmpty()) {
                    drawLines(ps[1], x + PAD, ty, lineH7, cellW - 2 * PAD, 'l');
                }
            }
        }
        cursorY -= rowH;
    }

    // -------------------------------------------------------------- fuentes

    /** Carga las métricas: Roboto (TTF incluidos como recursos del jar) o
     * Helvetica como reserva. FontLoader solo acepta URIs de fichero, así que
     * los TTF se extraen a un directorio temporal que se borra al salir. */
    void setupFaces() throws Exception {
        Path dir = extractFontsToTempDir();
        if (dir != null) {
            InternalResourceResolver rr = ResourceResolverFactory
                    .createDefaultInternalResourceResolver(dir.toUri());
            for (int i = 0; i < 4; i++) {
                FontUris uris = new FontUris(dir.resolve(ROBOTO_TTF[i]).toUri(), null);
                // FULL (no subset): el formulario se rellena a posteriori y los
                // valores necesitan glifos que el texto estático quizá no usa.
                faces[i] = FontLoader.loadFont(uris, null, true, EmbeddingMode.FULL,
                        EncodingMode.SINGLE_BYTE, false, false, rr, false, false, false);
            }
        } else {
            System.err.println("AVISO: no se encuentran los TTF de Roboto en los recursos"
                    + " del jar; se usa Helvetica sin incrustar");
            faces[0] = new Helvetica();
            faces[1] = new HelveticaOblique();
            faces[2] = new HelveticaBold();
            faces[3] = new HelveticaBoldOblique();
        }
    }

    /** Extrae los TTF de Roboto del classpath a un directorio temporal.
     * MUST seguir existiendo hasta escribir el PDF: FontLoader lee los bytes
     * a incrustar en ese momento, no al cargar la fuente. */
    static Path extractFontsToTempDir() {
        try {
            Path dir = Files.createTempDirectory("xml2pdf-fonts");
            dir.toFile().deleteOnExit();
            for (String n : ROBOTO_TTF) {
                try (InputStream is = Xml2Pdf.class.getResourceAsStream("assets/fonts/" + n)) {
                    if (is == null) {
                        return null;
                    }
                    Path ttf = dir.resolve(n);
                    Files.copy(is, ttf);
                    ttf.toFile().deleteOnExit();
                }
            }
            return dir;
        } catch (Exception e) {
            return null;
        }
    }

    /** Crea los objetos PDF de las fuentes. MUST llamarse tras dibujar todo:
     * los glifos usados (mapChar) determinan el subset que se incrusta. */
    void buildFonts() {
        for (int i = 0; i < 4; i++) {
            Typeface f = faces[i];
            FontDescriptor desc = f instanceof FontDescriptor ? (FontDescriptor) f : null;
            PDFFont pf = doc.getFactory().makeFont(FONT_RES[i], f.getEmbedFontName(),
                    f.getEncodingName(), f, desc);
            pdfFonts[i] = pf;
            res.addFont(pf);
        }
    }

    // -------------------------------------------------------------- acroform

    void buildAcroForm() {
        PDFArray fields = new PDFArray();
        for (Map.Entry<String, List<PDFDictionary>> e : widgetsPorNombre.entrySet()) {
            String nombre = e.getKey();
            List<PDFDictionary> widgets = e.getValue();
            PDFName ft = tipoPorNombre.get(nombre);
            if (widgets.size() == 1) {
                PDFDictionary w = widgets.get(0);
                w.put("T", nombre);
                w.put("FT", ft);
                if (ft.toString().equals("/Tx")) {
                    w.put("DA", "/F1 9 Tf 0 g");
                }
                fields.add(w.makeReference());
            } else {
                PDFDictionary parent = new PDFDictionary();
                parent.put("T", nombre);
                parent.put("FT", ft);
                if (ft.toString().equals("/Tx")) {
                    parent.put("DA", "/F1 9 Tf 0 g");
                }
                PDFArray kids = new PDFArray();
                for (PDFDictionary w : widgets) {
                    kids.add(w.makeReference());
                }
                parent.put("Kids", kids);
                doc.registerObject(parent);
                for (PDFDictionary w : widgets) {
                    w.put("Parent", parent.makeReference());
                }
                fields.add(parent.makeReference());
            }
        }
        PDFDictionary acro = new PDFDictionary();
        acro.put("Fields", fields);
        acro.put("DA", "/F1 9 Tf 0 g");
        PDFDictionary dr = new PDFDictionary();
        PDFDictionary drFonts = new PDFDictionary();
        for (int i = 0; i < 4; i++) {
            drFonts.put(FONT_RES[i], pdfFonts[i].makeReference());
        }
        dr.put("Font", drFonts);
        acro.put("DR", dr);
        doc.registerObject(acro);
        doc.getRoot().put("AcroForm", acro.makeReference());
    }
}
