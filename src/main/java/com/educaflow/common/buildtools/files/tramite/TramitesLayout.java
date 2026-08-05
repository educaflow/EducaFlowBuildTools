package com.educaflow.common.buildtools.files.tramite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * La estructura de carpetas de los trámites dentro del árbol de fuentes.
 *
 * Los trámites y los tipos de expediente se descubren por la presencia de su
 * fichero maestro ({@code TramiteInstance.xml} y {@code TipoExpedienteInstance.xml})
 * <b>a cualquier profundidad</b> bajo un paquete raíz configurable (por omisión
 * {@link #PAQUETE_RAIZ_POR_DEFECTO}), de forma que las carpetas intermedias
 * sirven solo de agrupación.
 *
 * Esta clase concentra todo lo que depende de esa estructura: la ruta de la
 * raíz, la de la carpeta {@code shared}, la búsqueda del trámite al que
 * pertenece un fichero (subiendo por las carpetas padre, <b>siempre con tope en
 * la raíz</b>) y las validaciones de trámite anidado y de tipo huérfano o fuera
 * de la raíz.
 *
 * @author logongas
 */
public class TramitesLayout {

    static final public String PAQUETE_RAIZ_POR_DEFECTO = "com.educaflow.tramites";

    private final Path origen;
    private final String paqueteRaiz;

    public TramitesLayout(Path origen, String paqueteRaiz) {
        if (origen == null) {
            throw new RuntimeException("El origen no puede ser null");
        }
        if ((paqueteRaiz == null) || (paqueteRaiz.isBlank())) {
            throw new RuntimeException("El paquete raíz de los trámites no puede estar vacío");
        }
        this.origen = origen;
        this.paqueteRaiz = paqueteRaiz.trim();
    }

    /**
     * El paquete raíz de los trámites que va como último argumento (opcional)
     * de los {@code Main}: si no viene, {@link #PAQUETE_RAIZ_POR_DEFECTO}.
     */
    public static String paqueteRaizFromArgs(String[] args, int index) {
        if ((args != null) && (args.length > index) && (args[index] != null) && (args[index].isBlank() == false)) {
            return args[index].trim();
        }

        return PAQUETE_RAIZ_POR_DEFECTO;
    }

    /**
     * @return la raíz del árbol de fuentes (p.ej. ./src/main/java)
     */
    public Path getOrigen() {
        return origen;
    }

    /**
     * @return el paquete raíz de los trámites, con puntos (p.ej. com.educaflow.tramites)
     */
    public String getPaqueteRaiz() {
        return paqueteRaiz;
    }

    /**
     * @return la carpeta del paquete raíz de los trámites
     */
    public Path getRootPackagePath() {
        return origen.resolve(paqueteRaiz.replace('.', '/'));
    }

    /**
     * @return la carpeta "shared" de la raíz, de donde salen los TipoDocumentoPdf compartidos
     */
    public Path getSharedPath() {
        return getRootPackagePath().resolve("shared");
    }

    public boolean existeRaiz() {
        return Files.isDirectory(getRootPackagePath());
    }

    /**
     * ¿El fichero o carpeta está dentro del paquete raíz de los trámites?
     *
     * Es la comprobación previa obligatoria de {@link #findTramiteInstanceAncestro(Path)}
     * y de {@link #getTramiteInstanceDelTipo(Path)}: sin ella, algo que esté
     * fuera de la raíz nunca la encontraría como ancestro y la subida por las
     * carpetas padre treparía hasta "/".
     */
    public boolean estaBajoLaRaiz(Path path) {
        return path.toAbsolutePath().normalize().startsWith(getRootPackagePath().toAbsolutePath().normalize());
    }

    /**
     * Busca el TramiteInstance.xml del trámite al que pertenece el fichero
     * subiendo por sus carpetas padre. La raíz se incluye en la búsqueda y ahí
     * se corta: nunca se sube por encima de ella. Devuelve null si no hay
     * ninguno, incluido el caso de que el fichero ni siquiera esté bajo la raíz.
     */
    public Path findTramiteInstanceAncestro(Path desde) {
        if (estaBajoLaRaiz(desde) == false) {
            return null;
        }

        return buscarTramiteInstanceAncestro(desde, getRootPackagePath().toAbsolutePath().normalize());
    }

    /**
     * La subida por las carpetas padre <b>sin tope</b>, hasta "/".
     *
     * Es el único punto de entrada para quien no tiene layout: el
     * {@code Xml2Pdf.main} autónomo, donde a mano no se sabe cuál es la raíz de
     * fuentes. No la usa ninguna tarea del build: todas van por
     * {@link #findTramiteInstanceAncestro(Path)}, que sí tiene tope.
     */
    public static Path buscarTramiteInstanceAncestroSinTope(Path desde) {
        return buscarTramiteInstanceAncestro(desde, null);
    }

    private static Path buscarTramiteInstanceAncestro(Path desde, Path tope) {
        Path directorio = desde.toAbsolutePath().normalize().getParent();

        while (directorio != null) {
            if ((tope != null) && (directorio.startsWith(tope) == false)) {
                return null;
            }

            Path tramiteInstance = directorio.resolve(TramiteInstanceFile.TRAMITE_XML_NAME);
            if (Files.isRegularFile(tramiteInstance)) {
                return tramiteInstance;
            }

            directorio = directorio.getParent();
        }

        return null;
    }

    /**
     * El TramiteInstance.xml del trámite al que pertenece un tipo de expediente.
     * Falla si el tipo está fuera del paquete raíz o si no hay ningún trámite
     * por encima de él (tipo de expediente huérfano).
     */
    public Path getTramiteInstanceDelTipo(Path tipoExpedienteXmlFile) {
        if (estaBajoLaRaiz(tipoExpedienteXmlFile) == false) {
            throw new RuntimeException("El tipo de expediente " + tipoExpedienteXmlFile
                    + " no está bajo el paquete raíz de los trámites " + getRootPackagePath()
                    + " (" + paqueteRaiz + "): todos los tipos de expediente deben estar dentro de él.");
        }

        Path tramiteInstance = findTramiteInstanceAncestro(tipoExpedienteXmlFile);
        if (tramiteInstance == null) {
            throw new RuntimeException("Tipo de expediente huérfano: no hay ningún "
                    + TramiteInstanceFile.TRAMITE_XML_NAME + " en ninguna carpeta por encima de "
                    + tipoExpedienteXmlFile + " (hasta " + getRootPackagePath()
                    + "): un tipo de expediente siempre pertenece a un trámite.");
        }

        return tramiteInstance;
    }

    /**
     * Todos los TramiteInstance.xml que hay bajo el paquete raíz, a cualquier
     * profundidad. Asume que la raíz existe: quien lo llame debe comprobar
     * antes {@link #existeRaiz()}.
     */
    public List<Path> findTramitesXmlFiles() {
        try (Stream<Path> walk = Files.walk(getRootPackagePath())) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(TramiteInstanceFile.TRAMITE_XML_NAME))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al buscar los trámites en:" + getRootPackagePath(), ex);
        }
    }

    /**
     * Un trámite no puede estar dentro de otro trámite. Asume que la raíz
     * existe: quien lo llame debe comprobar antes {@link #existeRaiz()}.
     */
    public void checkTramitesNoAnidados() {
        for (Path tramiteXmlFile : findTramitesXmlFiles()) {
            Path carpetaTramite = tramiteXmlFile.toAbsolutePath().normalize().getParent();
            Path tramitePadre = findTramiteInstanceAncestro(carpetaTramite);

            if (tramitePadre != null) {
                throw new RuntimeException("El trámite " + tramiteXmlFile + " está anidado dentro del trámite "
                        + tramitePadre + ": un trámite no puede estar dentro de otro.");
            }
        }
    }

}
