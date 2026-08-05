package com.educaflow.common.buildtools.files.tramite;

import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * El modelo del TramiteInstance.xml de un trámite.
 *
 * Es el único modelo del trámite: de él salen tanto los data-init del trámite
 * (createdatainittramite) como el code/name que hereda un tipo de expediente
 * que no los declara, y el name que es el título de los documentos PDF que no
 * llevan &lt;titulo&gt;.
 *
 * Ojo: files/tramite y files/tipoexpediente quedan mutuamente referenciados (el
 * trámite resuelve el code de su tipo de expediente activo, y el tipo resuelve
 * el name/code de su trámite). No hay recursión infinita porque
 * {@link #getDefaultTipoExpedienteCode()} no se llama al derivar los datos del
 * tipo de expediente.
 *
 * @author logongas
 */
@XmlRootElement(name = "Tramite")
@XmlAccessorType(XmlAccessType.FIELD)
public class TramiteInstanceFile {

    static final public String TRAMITE_XML_NAME = "TramiteInstance.xml";

    @XmlElement(name = "code")
    private String code;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "tipoTramite")
    private String tipoTramite;

    @XmlElement(name = "publico")
    private String publico;

    @XmlElement(name = "privado")
    private String privado;

    @XmlElement(name = "defaultTipoExpediente")
    private String defaultTipoExpediente;

    @XmlElement(name = "help")
    private String help;

    @XmlTransient
    private Path path;

    @XmlTransient
    private TramitesLayout tramitesLayout;

    @XmlTransient
    private String defaultTipoExpedienteCode;

    @XmlTransient
    private boolean defaultTipoExpedienteCodeResuelto = false;

    /**
     * Fuerza las validaciones de los campos obligatorios y del &lt;help&gt;
     * antes de generar nada.
     */
    public void check() {
        getCode();
        getName();
        getTipoTramite();
        getHelp();
    }

    public String getCode() {
        return getTagObligatorio(code, "code");
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return getTagObligatorio(name, "name");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTipoTramite() {
        return getTagObligatorio(tipoTramite, "tipoTramite");
    }

    public void setTipoTramite(String tipoTramite) {
        this.tipoTramite = tipoTramite;
    }

    /**
     * El &lt;publico&gt; tal cual lo declara el XML, o null si no lo declara.
     * Es un String y no un boolean a propósito: lo que decide si el atributo se
     * emite en el data-init es que el tag exista, así que hay que poder
     * distinguir "no declarado" (null) de "declarado y vacío" ("").
     */
    public String getPublico() {
        return trimOrNull(publico);
    }

    public void setPublico(String publico) {
        this.publico = publico;
    }

    /**
     * El &lt;privado&gt; tal cual lo declara el XML, o null si no lo declara.
     * Ver {@link #getPublico()}.
     */
    public String getPrivado() {
        return trimOrNull(privado);
    }

    public void setPrivado(String privado) {
        this.privado = privado;
    }

    /**
     * El texto del &lt;help&gt;, <b>sin trim()</b>: va dentro de un CDATA y hay
     * que conservar los saltos de línea y la indentación tal cual. Si no hay
     * &lt;help&gt; devuelve la cadena vacía, para que se emita un CDATA vacío.
     */
    public String getHelp() {
        if (help == null) {
            return "";
        }

        if (help.contains("]]>")) {
            throw new RuntimeException("El <help> no puede contener ']]>' porque va dentro de un CDATA:" + path);
        }

        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public void setDefaultTipoExpediente(String defaultTipoExpediente) {
        this.defaultTipoExpediente = defaultTipoExpediente;
    }

    /**
     * El code del tipo de expediente activo del trámite, o null si el trámite
     * no declara &lt;defaultTipoExpediente&gt; o lo declara en blanco (en ese
     * caso no hay tipo de expediente activo y no se genera su data-init).
     *
     * El valor declarado puede ser directamente un code o el nombre de la
     * carpeta del tipo (v1, v2...), que se busca <b>recursivamente</b> bajo la
     * carpeta del trámite:
     * <ul>
     * <li>0 coincidencias: se asume que ya es un code y se devuelve tal cual.</li>
     * <li>1 coincidencia: se resuelve a su code con la misma regla que el resto
     * de las herramientas (el &lt;code&gt; declarado en su
     * TipoExpedienteInstance.xml o, si falta, el code del trámite más el nombre
     * de la carpeta en mayúsculas).</li>
     * <li>más de una: es ambiguo y falla.</li>
     * </ul>
     */
    public String getDefaultTipoExpedienteCode() {
        if (defaultTipoExpedienteCodeResuelto == false) {
            defaultTipoExpedienteCode = resolveDefaultTipoExpedienteCode();
            defaultTipoExpedienteCodeResuelto = true;
        }

        return defaultTipoExpedienteCode;
    }

    private String resolveDefaultTipoExpedienteCode() {
        if ((defaultTipoExpediente == null) || (defaultTipoExpediente.isBlank())) {
            return null;
        }

        String valor = defaultTipoExpediente.trim();

        List<Path> tiposExpedienteXmlFiles = findTiposExpedienteXmlFilesEnCarpeta(valor);

        if (tiposExpedienteXmlFiles.isEmpty()) {
            return valor;
        }

        if (tiposExpedienteXmlFiles.size() > 1) {
            throw new RuntimeException("El <defaultTipoExpediente> '" + valor + "' del trámite " + path
                    + " es ambiguo: hay más de una carpeta '" + valor + "' con un "
                    + TipoExpedienteInstanceFileFinder.TIPO_EXPEDIENTE_XML_NAME + " bajo el trámite:"
                    + tiposExpedienteXmlFiles.stream().map(Path::toString).collect(Collectors.joining(", ")));
        }

        return new TipoExpedienteInstanceFileFinder(tramitesLayout)
                .parseTipoExpedienteXml(tiposExpedienteXmlFiles.get(0))
                .getCode();
    }

    /**
     * Los TipoExpedienteInstance.xml que hay bajo la carpeta del trámite dentro
     * de una carpeta con ese nombre, a cualquier profundidad.
     */
    private List<Path> findTiposExpedienteXmlFilesEnCarpeta(String nombreCarpeta) {
        Path carpetaTramite = path.getParent();

        try (Stream<Path> walk = Files.walk(carpetaTramite)) {
            return walk
                    .filter(Files::isDirectory)
                    .filter(directorio -> directorio.getFileName().toString().equals(nombreCarpeta))
                    .map(directorio -> directorio.resolve(TipoExpedienteInstanceFileFinder.TIPO_EXPEDIENTE_XML_NAME))
                    .filter(Files::isRegularFile)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al buscar el tipo de expediente '" + nombreCarpeta
                    + "' del trámite:" + path, ex);
        }
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(Path path) {
        this.path = path;
    }

    public void setTramitesLayout(TramitesLayout tramitesLayout) {
        this.tramitesLayout = tramitesLayout;
    }

    private String getTagObligatorio(String valor, String tagName) {
        if ((valor == null) || (valor.isBlank())) {
            throw new RuntimeException("No existe el tag <" + tagName + "> en el fichero del trámite:" + path);
        }

        return valor.trim();
    }

    private String trimOrNull(String valor) {
        if (valor == null) {
            return null;
        }

        return valor.trim();
    }

}
