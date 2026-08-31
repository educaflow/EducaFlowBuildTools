package com.educaflow.common.buildtools.createstates;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Los perfiles del enum global {@code Profile} del dominio de expedientes.
 *
 * <p>Es contra esta lista contra la que se valida el atributo {@code profile} de cada estado
 * (regla 11): restaura la garantía que daba el data-init de {@code EstadoTipoExpediente}, que
 * desapareció al borrar la entidad, y ataja los typos que producirían una referencia
 * {@code Profile.<X>} que no compila.
 */
public final class ProfilesDelDominio {

    private static final String NOMBRE_ENUM = "Profile";

    private final Set<String> nombres;
    private final Path fichero;

    private ProfilesDelDominio(Set<String> nombres, Path fichero) {
        this.nombres = nombres;
        this.fichero = fichero;
    }

    public static ProfilesDelDominio leer(Path domainsTipoExpedienteXml) {
        if (Files.isRegularFile(domainsTipoExpedienteXml) == false) {
            throw new RuntimeException("No existe el dominio " + domainsTipoExpedienteXml
                    + ", del que sale el enum global " + NOMBRE_ENUM + " contra el que se validan los"
                    + " perfiles de los estados.");
        }

        try {
            Element raiz = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(domainsTipoExpedienteXml.toFile()).getDocumentElement();

            Set<String> nombres = new LinkedHashSet<>();
            NodeList enums = raiz.getElementsByTagName("enum");
            for (int i = 0; i < enums.getLength(); i++) {
                Element enumElement = (Element) enums.item(i);
                if (NOMBRE_ENUM.equals(enumElement.getAttribute("name")) == false) {
                    continue;
                }
                NodeList items = enumElement.getElementsByTagName("item");
                for (int j = 0; j < items.getLength(); j++) {
                    nombres.add(((Element) items.item(j)).getAttribute("name"));
                }
            }

            if (nombres.isEmpty()) {
                throw new RuntimeException("No se encontró el enum '" + NOMBRE_ENUM + "' (o está vacío) en "
                        + domainsTipoExpedienteXml + ".");
            }

            return new ProfilesDelDominio(nombres, domainsTipoExpedienteXml);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al leer el enum " + NOMBRE_ENUM + " de " + domainsTipoExpedienteXml, ex);
        }
    }

    public boolean contiene(String profile) {
        return nombres.contains(profile);
    }

    public Set<String> getNombres() {
        return nombres;
    }

    public Path getFichero() {
        return fichero;
    }
}
