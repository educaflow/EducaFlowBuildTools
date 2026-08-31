package com.educaflow.common.buildtools.viewprocessor;

import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.Fase;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Locale;

/**
 * Lo que hace falta saber para preprocesar el {@code views.xml} de <b>una fase</b> de un tipo de
 * expediente: a qué fase corresponde y de dónde salen sus paneles.
 *
 * <p>Un {@code views.xml} de fase no es autosuficiente. No lleva el form de plantilla
 * {@code exp-<Code>-Templates}, que está en el {@code views.xml} de la raíz de la versión para que
 * varias fases puedan compartir paneles; y en el atributo {@code state} de sus formularios va el
 * nombre <b>corto</b> del estado, que solo es único dentro de la fase. Las dos cosas se resuelven
 * aquí subiendo por las carpetas padre hasta el {@code TipoExpedienteInstance.xml}:
 *
 * <ul>
 *   <li>la <b>fase</b> es el nombre de la carpeta del fichero en mayúsculas
 *       ({@code recepcion/} → {@code RECEPCION}), y se valida contra las fases declaradas en el XML
 *       para que una carpeta mal escrita falle en el build en vez de generar vistas fantasma;</li>
 *   <li>el <b>form de plantilla</b> es el del {@code views.xml} hermano de ese XML.</li>
 * </ul>
 *
 * @author logongas
 */
public class TipoExpedienteViewsContext {

    private static final String VIEWS_XML_NAME = "views.xml";

    private static final Pattern PATRON_TEMPLATE = Pattern.compile("exp-([a-zA-Z0-9]+)-Templates");

    private final TipoExpedienteInstanceFile tipoExpediente;
    private final Fase fase;
    private final Element templateForm;

    private TipoExpedienteViewsContext(TipoExpedienteInstanceFile tipoExpediente, Fase fase, Element templateForm) {
        this.tipoExpediente = tipoExpediente;
        this.fase = fase;
        this.templateForm = templateForm;
    }

    public TipoExpedienteInstanceFile getTipoExpediente() {
        return tipoExpediente;
    }

    public Fase getFase() {
        return fase;
    }

    /** El form {@code exp-<Code>-Templates} de la raíz de la versión, de donde salen los paneles. */
    public Element getTemplateForm() {
        return templateForm;
    }

    /**
     * El contexto del {@code views.xml} de fase que hay en {@code viewsFilePath}.
     *
     * @throws RuntimeException si el fichero no está dentro de la carpeta de una fase de un tipo de
     *         expediente, o si la raíz de la versión no tiene su {@code views.xml} con el form de
     *         plantilla.
     */
    public static TipoExpedienteViewsContext of(Path viewsFilePath, TramitesLayout tramitesLayout) {
        Path carpetaFase = viewsFilePath.toAbsolutePath().normalize().getParent();
        Path tipoExpedienteXmlFile = findTipoExpedienteInstance(carpetaFase, tramitesLayout);

        if (tipoExpedienteXmlFile == null) {
            throw new RuntimeException("El fichero de vistas " + viewsFilePath + " tiene formularios con"
                    + " atributo 'state', pero no hay ningún " + TipoExpedienteInstanceFileFinder.TIPO_EXPEDIENTE_XML_NAME
                    + " en ninguna carpeta por encima de él: los formularios de estado solo pueden"
                    + " estar dentro de la carpeta de una fase de un tipo de expediente.");
        }

        Path carpetaVersion = tipoExpedienteXmlFile.getParent();

        if (carpetaVersion.equals(carpetaFase)) {
            throw new RuntimeException("El fichero de vistas " + viewsFilePath + " está en la raíz de la"
                    + " versión del tipo de expediente y tiene formularios con atributo 'state'."
                    + " En la raíz solo va el form de plantilla exp-<Code>-Templates con los paneles;"
                    + " los formularios de cada estado van en el views.xml de la carpeta de su fase.");
        }

        TipoExpedienteInstanceFile tipoExpediente =
                new TipoExpedienteInstanceFileFinder(tramitesLayout).parseTipoExpedienteXml(tipoExpedienteXmlFile);

        // Locale.ROOT a propósito: la fase se deduce del nombre de la carpeta y se compara con los
        // name del XML, que son ASCII en UPPER_SNAKE_CASE.
        String nombreFase = carpetaFase.getFileName().toString().toUpperCase(Locale.ROOT);
        Fase fase = tipoExpediente.getFase(nombreFase);

        if (fase == null) {
            throw new RuntimeException("La carpeta '" + carpetaFase.getFileName() + "' de " + viewsFilePath
                    + " no corresponde a ninguna fase de " + tipoExpediente.getCode()
                    + ". Sus fases son: " + tipoExpediente.getFases()
                    + " (la carpeta de una fase se llama como la fase en minúsculas).");
        }

        Element templateForm = findTemplateForm(carpetaVersion, viewsFilePath);
        checkCodeDelTemplateForm(templateForm, tipoExpediente, carpetaVersion.resolve(VIEWS_XML_NAME));

        return new TipoExpedienteViewsContext(tipoExpediente, fase, templateForm);
    }

    /**
     * El {@code <Code>} del form de plantillas MUST ser el del tipo de expediente al que pertenece la
     * carpeta.
     *
     * <p>Es la red contra el fallo típico de la receta de versionado: copiar la carpeta de la versión
     * anterior y no sustituir el {@code <Code>}. Los nombres de vista de Axelor son un espacio
     * <b>global</b>, así que sin esta comprobación el build generaría todas las vistas de la versión
     * nueva con el nombre de las viejas y las <b>pisaría</b>.
     *
     * <p><b>Hueco de cobertura conocido</b>: esta comprobación solo se dispara si alguna carpeta de
     * fase tiene formularios con atributo {@code state}. Un tipo de expediente sin forms de estado no
     * pasa por aquí y su {@code <Code>} no se contrasta con nada.
     */
    private static void checkCodeDelTemplateForm(Element templateForm, TipoExpedienteInstanceFile tipoExpediente, Path viewsRaiz) {
        String codeEncontrado = getNombreExpediente(templateForm);

        if (codeEncontrado.equals(tipoExpediente.getCode()) == false) {
            throw new RuntimeException("El form de plantillas de " + viewsRaiz + " se llama '"
                    + templateForm.getAttribute("name") + "', pero el tipo de expediente de esa carpeta"
                    + " es '" + tipoExpediente.getCode() + "': debería llamarse 'exp-"
                    + tipoExpediente.getCode() + "-Templates'. Encontrado '" + codeEncontrado
                    + "', esperado '" + tipoExpediente.getCode() + "'."
                    + " Los nombres de vista de Axelor son globales, así que un Code equivocado no da"
                    + " un error: genera las vistas con el nombre de OTRA versión y las pisa.");
        }
    }

    /**
     * Sube por las carpetas padre buscando el {@code TipoExpedienteInstance.xml}, con tope en el
     * paquete raíz de los trámites para no trepar hasta "/".
     */
    private static Path findTipoExpedienteInstance(Path desde, TramitesLayout tramitesLayout) {
        if (tramitesLayout.estaBajoLaRaiz(desde) == false) {
            return null;
        }

        Path tope = tramitesLayout.getRootPackagePath().toAbsolutePath().normalize();

        for (Path directorio = desde; (directorio != null) && (directorio.startsWith(tope)); directorio = directorio.getParent()) {
            Path candidato = directorio.resolve(TipoExpedienteInstanceFileFinder.TIPO_EXPEDIENTE_XML_NAME);
            if (Files.isRegularFile(candidato)) {
                return candidato;
            }
        }

        return null;
    }

    private static Element findTemplateForm(Path carpetaVersion, Path viewsFilePath) {
        Path viewsRaiz = carpetaVersion.resolve(VIEWS_XML_NAME);

        if (Files.isRegularFile(viewsRaiz) == false) {
            throw new RuntimeException("Falta el " + VIEWS_XML_NAME + " de la raíz de la versión ("
                    + viewsRaiz + "), que es donde va el form exp-<Code>-Templates con los paneles"
                    + " que incluye " + viewsFilePath + ".");
        }

        Element templateForm = findTemplateFormEnDocumento(parse(viewsRaiz), viewsRaiz);

        if (templateForm == null) {
            throw new RuntimeException("El " + viewsRaiz + " no tiene ningún form exp-<Code>-Templates,"
                    + " que es de donde salen los paneles de los formularios de " + viewsFilePath + ".");
        }

        return templateForm;
    }

    /**
     * El form {@code exp-<Code>-Templates} que haya en el propio documento, o null si no tiene
     * ninguno, que es lo normal en el {@code views.xml} de una fase.
     */
    public static Element findTemplateFormEnDocumento(Document document, Path filePath) {
        Element templateForm = null;

        for (Element element : XMLUtil.getChildsFilterByTagName(document.getDocumentElement(), "form")) {
            Matcher matcher = PATRON_TEMPLATE.matcher(element.getAttribute("name"));
            if (matcher.find()) {
                if (templateForm != null) {
                    throw new RuntimeException("Existen al menos 2 nodos de plantilla en " + filePath
                            + ":" + element.getAttribute("name"));
                }
                templateForm = element;
            }
        }

        return templateForm;
    }

    private static Document parse(Path path) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);

            return documentBuilderFactory.newDocumentBuilder().parse(path.toFile());
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al leer " + path, ex);
        }
    }

    /** El código del tipo de expediente que va en el nombre de las vistas, sacado de la plantilla. */
    public static String getNombreExpediente(Element templateForm) {
        Matcher matcher = PATRON_TEMPLATE.matcher(templateForm.getAttribute("name"));

        if (matcher.find() == false) {
            throw new RuntimeException("El atributo 'name' del templateForm no es válido:" + templateForm.getAttribute("name"));
        }

        return matcher.group(1);
    }

    /** Los form del documento que llevan atributo {@code state}, que son los de un views de fase. */
    public static List<Element> getFormElementsWithStateAttribute(Element parentElement) {
        return XMLUtil.getChildsFilterByTagName(parentElement, "form").stream()
                .filter(formElement -> formElement.hasAttribute("state"))
                .collect(java.util.stream.Collectors.toList());
    }

}
