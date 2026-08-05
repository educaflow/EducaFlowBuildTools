package com.educaflow.common.buildtools.files.tramite;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Busca y deserializa los TramiteInstance.xml de los trámites.
 *
 * @author logongas
 */
public class TramiteInstanceFileFinder {

    private final TramitesLayout tramitesLayout;

    public TramiteInstanceFileFinder(TramitesLayout tramitesLayout) {
        this.tramitesLayout = tramitesLayout;
    }

    /**
     * Todos los trámites que hay bajo el paquete raíz. Asume que la raíz
     * existe: quien lo llame debe comprobar antes {@link TramitesLayout#existeRaiz()}.
     */
    public List<TramiteInstanceFile> findTramitesFile() {
        List<TramiteInstanceFile> tramiteInstanceFiles = new ArrayList<>();

        for (Path tramiteXmlFile : tramitesLayout.findTramitesXmlFiles()) {
            tramiteInstanceFiles.add(parse(tramiteXmlFile));
        }

        return tramiteInstanceFiles;
    }

    public TramiteInstanceFile parse(Path tramiteXmlFile) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TramiteInstanceFile.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            TramiteInstanceFile tramiteInstanceFile = (TramiteInstanceFile) unmarshaller.unmarshal(tramiteXmlFile.toFile());
            tramiteInstanceFile.setPath(tramiteXmlFile);
            tramiteInstanceFile.setTramitesLayout(tramitesLayout);

            return tramiteInstanceFile;
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al leer el fichero del trámite:" + tramiteXmlFile, ex);
        }
    }

}
