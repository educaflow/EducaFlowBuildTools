package com.educaflow.common.buildtools.files.ace;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.nio.file.Path;
import java.util.List;

/**
 * Un &lt;ace&gt; del bloque &lt;acl&gt; de un TramiteInstance.xml o de un
 * TipoExpedienteInstance.xml: da un perfil sobre ese trámite o ese tipo de
 * expediente al usuario que cumpla la condición de su &lt;usuario&gt; (un
 * tipo de usuario o un cargo). El trámite o el tipo no se escribe en el
 * &lt;ace&gt;: es el del propio fichero.
 *
 * <pre>
 * &lt;ace perfil="AUDITOR"&gt;
 *     &lt;usuario cargo="DIRECTOR"/&gt;
 * &lt;/ace&gt;
 * </pre>
 *
 * Se cargan con el data-init que se genera para el trámite
 * (AceProfileTramite) o para el tipo de expediente (AceProfileTipoExpediente).
 *
 * @author logongas
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Ace {

    @XmlAttribute
    private String perfil;

    @XmlElement(name = "usuario")
    private Usuario usuario;

    public String getPerfil() {
        return trimOrNull(perfil);
    }

    public String getTipoUsuario() {
        if (usuario == null) {
            return null;
        }

        return trimOrNull(usuario.tipoUsuario);
    }

    public String getCargo() {
        if (usuario == null) {
            return null;
        }

        return trimOrNull(usuario.cargo);
    }

    /**
     * Los &lt;ace&gt; ya validados, o una lista vacía si el fichero no tiene
     * &lt;acl&gt;. Cada &lt;ace&gt; tiene que llevar un perfil y un
     * &lt;usuario&gt; con exactamente uno de tipoUsuario o cargo, que es lo que
     * distingue en el data-init con qué clave se busca la fila.
     */
    public static List<Ace> check(List<Ace> aces, Path path) {
        if (aces == null) {
            return List.of();
        }

        for (Ace ace : aces) {
            if (ace.getPerfil() == null) {
                throw new RuntimeException("Hay un <ace> sin 'perfil' en el fichero:" + path);
            }
            if (ace.usuario == null) {
                throw new RuntimeException("El <ace perfil=\"" + ace.getPerfil() + "\"> tiene que llevar un <usuario> (p. ej. <usuario cargo=\"...\"/>) en el fichero:" + path);
            }
            if ((ace.getTipoUsuario() == null) == (ace.getCargo() == null)) {
                throw new RuntimeException("El <usuario> del <ace perfil=\"" + ace.getPerfil() + "\"> tiene que llevar exactamente uno de 'tipoUsuario' o 'cargo' en el fichero:" + path);
            }
        }

        return aces;
    }

    private static String trimOrNull(String valor) {
        if ((valor == null) || (valor.isBlank())) {
            return null;
        }

        return valor.trim();
    }

    /**
     * El &lt;usuario&gt; de un &lt;ace&gt;: a quién se da el perfil.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Usuario {

        @XmlAttribute
        private String tipoUsuario;

        @XmlAttribute
        private String cargo;

    }

}
