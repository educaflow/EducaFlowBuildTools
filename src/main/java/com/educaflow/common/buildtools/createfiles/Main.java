/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.createfiles;

import com.educaflow.common.buildtools.files.domainmodel.DomainModelFile;
import com.educaflow.common.buildtools.files.phaseeventmanagerfile.PhaseEventManagerFile;
import com.educaflow.common.buildtools.files.initialeventmanagerfile.InitialEventManagerFile;
import com.educaflow.common.buildtools.files.stateeventvalidator.StateEventValidatorFile;
import com.educaflow.common.buildtools.files.tipoexpediente.Fase;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;
import com.educaflow.common.buildtools.files.views.ViewsFaseFile;
import com.educaflow.common.buildtools.files.views.ViewsFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera los esqueletos que le falten a cada tipo de expediente, repartidos entre la carpeta de
 * versión y una subcarpeta por fase:
 *
 * <pre>
 *   &lt;vN&gt;/TipoExpedienteInstance.xml
 *   &lt;vN&gt;/domains.xml              &lt;- el modelo, de todo el tipo
 *   &lt;vN&gt;/views.xml                &lt;- solo el form exp-&lt;Code&gt;-Templates con los paneles
 *   &lt;vN&gt;/InitialEventManagerImpl.java  &lt;- el evento inicial, uno por tipo
 *   &lt;vN&gt;/&lt;fase en minúsculas&gt;/PhaseEventManagerImpl.java
 *   &lt;vN&gt;/&lt;fase en minúsculas&gt;/StateEventValidatorImpl.kt
 *   &lt;vN&gt;/&lt;fase en minúsculas&gt;/views.xml
 * </pre>
 *
 * <p>Es <b>solo un generador</b> e <b>idempotente</b>: crea únicamente los ficheros que no existen,
 * nunca pisa fuentes editadas a mano. La comprobación de que el código escrito a mano concuerda con
 * la máquina de estados del XML <b>ya no vive aquí</b>: está en los tests de {@code secretaria-virtual}
 * ({@code src/test/java/com/educaflow/tiposexpedientes}), que leen bytecode y por eso alcanzan también
 * al {@code StateEventValidator} escrito en Kotlin.
 *
 * <p>Uso:
 * <pre>
 *   Main &lt;origen&gt; [paqueteRaiz] [--tipo=&lt;ruta&gt;] [--fase=&lt;FASE&gt;]
 * </pre>
 * donde {@code origen} es la raíz del árbol de fuentes (p.ej. {@code ./src/main/java}),
 * {@code paqueteRaiz} es el paquete raíz de los trámites (por omisión
 * {@link TramitesLayout#PAQUETE_RAIZ_POR_DEFECTO}) y {@code --tipo=} acota la generación a un único
 * tipo de expediente, indicando la ruta de su {@code TipoExpedienteInstance.xml} o la de la carpeta
 * que lo contiene. Sin {@code --tipo=} se procesan todos los trámites, que es lo que hace el build.
 *
 * <p>{@code --fase=} acota además la generación a una sola fase. Con {@code --fase=} no se generan
 * los ficheros de la raíz de la versión, porque no son de ninguna fase en concreto. Solo tiene
 * sentido junto con {@code --tipo=}: sin él se aplica a todos los tipos de expediente y aborta a
 * medias en el primero que no tenga esa fase. <b>No</b> hace falta para añadir una fase nueva sin
 * tocar las que ya están, porque al ser idempotente sin {@code --fase=} se crean igualmente solo
 * los ficheros que faltan.
 *
 * <p>Códigos de salida: {@code 0} todo bien (se hayan creado ficheros o no), {@code 1} uso incorrecto
 * (argumentos inválidos, o {@code --tipo=}/{@code --fase=} sin coincidencias) y {@code 2} error
 * procesando los tipos de expediente (XML inválido, error de E/S…).
 *
 * @author logongas
 */
public class Main {

    private static final String OPCION_TIPO = "--tipo=";
    private static final String OPCION_FASE = "--fase=";

    private static final int SALIDA_OK = 0;
    private static final int SALIDA_USO_INCORRECTO = 1;
    private static final int SALIDA_ERROR_PROCESANDO = 2;

    public static void main(String[] args) {
        List<String> posicionales = new ArrayList<>();
        String rutaTipo = null;
        String nombreFase = null;

        for (String arg : args) {
            if (arg.startsWith(OPCION_TIPO)) {
                rutaTipo = arg.substring(OPCION_TIPO.length()).trim();
                if (rutaTipo.isEmpty()) {
                    salirConError(SALIDA_USO_INCORRECTO, "La opción " + OPCION_TIPO + " necesita una ruta");
                }
            } else if (arg.startsWith(OPCION_FASE)) {
                nombreFase = arg.substring(OPCION_FASE.length()).trim();
                if (nombreFase.isEmpty()) {
                    salirConError(SALIDA_USO_INCORRECTO, "La opción " + OPCION_FASE + " necesita el nombre de una fase");
                }
            } else {
                posicionales.add(arg);
            }
        }

        if (posicionales.isEmpty()) {
            salirConError(SALIDA_USO_INCORRECTO,
                    "Uso: Main <origen> [paqueteRaiz] [" + OPCION_TIPO + "<ruta>] [" + OPCION_FASE + "<FASE>]");
        }

        Path rootPathSourceFiles = Paths.get(posicionales.get(0));
        String paqueteRaizTramites = TramitesLayout.paqueteRaizFromArgs(posicionales.toArray(new String[0]), 1);

        try {
            generar(rootPathSourceFiles, paqueteRaizTramites, rutaTipo, nombreFase);
        } catch (SalidaException ex) {
            salirConError(ex.getCodigoSalida(), ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            salirConError(SALIDA_ERROR_PROCESANDO, ex.getMessage());
        }

        System.exit(SALIDA_OK);
    }

    private static void generar(Path rootPathSourceFiles, String paqueteRaizTramites, String rutaTipo, String nombreFase) {
        TramitesLayout tramitesLayout = new TramitesLayout(rootPathSourceFiles, paqueteRaizTramites);
        List<TipoExpedienteInstanceFile> tipoExpedienteFiles = new TipoExpedienteInstanceFileFinder(tramitesLayout).findTiposExpedienteFile();

        if (rutaTipo != null) {
            tipoExpedienteFiles = filtrarPorTipo(tipoExpedienteFiles, rutaTipo);
        }

        Contador contador = new Contador();
        int fasesGeneradas = 0;

        for (TipoExpedienteInstanceFile tipoExpedienteFile : tipoExpedienteFiles) {

            //Los ficheros de la raíz de la versión son de todo el tipo, no de una fase: solo se
            //generan cuando no se está acotando a una fase concreta.
            if (nombreFase == null) {
                Path pathDomainModelFile = getPathDomainModelFile(tipoExpedienteFile);
                contador.cuenta(pathDomainModelFile,
                        new DomainModelFile(pathDomainModelFile, tipoExpedienteFile).createDomainModelIfNotExists());

                Path pathViewsFile = getPathViewsFile(tipoExpedienteFile);
                contador.cuenta(pathViewsFile,
                        new ViewsFile(pathViewsFile, tipoExpedienteFile).createViewsFileIfNotExists());

                //El evento inicial es del tipo de expediente, no de una fase: se dispara cuando
                //todavía no hay estado del que partir, así que hay exactamente uno por tipo y vive
                //en la raíz de la versión junto al TipoExpedienteInstance.xml.
                Path pathInitialEventManagerFile = getPathInitialEventManagerFile(tipoExpedienteFile);
                contador.cuenta(pathInitialEventManagerFile,
                        new InitialEventManagerFile(pathInitialEventManagerFile, tipoExpedienteFile)
                                .createInitialEventManagerFileIfNotExists());
            }

            for (Fase fase : filtrarPorFase(tipoExpedienteFile, nombreFase)) {
                Path carpetaFase = getCarpetaFase(tipoExpedienteFile, fase);
                crearCarpeta(carpetaFase);
                fasesGeneradas++;

                Path pathPhaseEventManagerFile = carpetaFase.resolve(tipoExpedienteFile.getPhaseEventManagerClassName() + ".java");
                contador.cuenta(pathPhaseEventManagerFile,
                        new PhaseEventManagerFile(pathPhaseEventManagerFile, fase).createPhaseEventManagerFileIfNotExists());

                Path pathStateEventValidatorFile = carpetaFase.resolve(tipoExpedienteFile.getStateEventValidatorClassName() + ".kt");
                contador.cuenta(pathStateEventValidatorFile,
                        new StateEventValidatorFile(pathStateEventValidatorFile, fase).createStateEventValidatorFileIfNotExists());

                Path pathViewsFaseFile = carpetaFase.resolve("views.xml");
                contador.cuenta(pathViewsFaseFile,
                        new ViewsFaseFile(pathViewsFaseFile, fase).createViewsFaseFileIfNotExists());
            }
        }

        System.out.println("Resumen: " + contador.creados + " fichero(s) creado(s), " + contador.existentes
                + " ya existente(s) en " + fasesGeneradas + " fase(s) de "
                + tipoExpedienteFiles.size() + " tipo(s) de expediente.");
    }

    /** Lleva la cuenta e imprime una línea CREADO por cada fichero nuevo. */
    private static class Contador {

        private int creados = 0;
        private int existentes = 0;

        void cuenta(Path path, boolean creado) {
            if (creado) {
                creados++;
                System.out.println("CREADO " + rutaLegible(path));
            } else {
                existentes++;
            }
        }
    }

    /**
     * Deja solo la fase indicada. Sin {@code --fase=} se generan todas, que es lo normal: como el
     * generador es idempotente, generarlas todas no toca las que ya están.
     */
    private static List<Fase> filtrarPorFase(TipoExpedienteInstanceFile tipoExpedienteFile, String nombreFase) {
        if (nombreFase == null) {
            return tipoExpedienteFile.getFases();
        }

        Fase fase = tipoExpedienteFile.getFase(nombreFase);
        if (fase == null) {
            throw new SalidaException(SALIDA_USO_INCORRECTO,
                    "El tipo de expediente " + tipoExpedienteFile.getCode() + " no tiene ninguna fase '"
                    + nombreFase + "'. Sus fases son: " + tipoExpedienteFile.getFases() + ".");
        }

        return List.of(fase);
    }

    private static void crearCarpeta(Path carpeta) {
        try {
            Files.createDirectories(carpeta);
        } catch (Exception ex) {
            throw new RuntimeException("No se ha podido crear la carpeta de la fase: " + carpeta, ex);
        }
    }

    /**
     * Deja solo el tipo de expediente cuya carpeta es la indicada. La ruta puede ser la del propio
     * {@code TipoExpedienteInstance.xml} o la de la carpeta que lo contiene.
     */
    private static List<TipoExpedienteInstanceFile> filtrarPorTipo(List<TipoExpedienteInstanceFile> tipoExpedienteFiles, String rutaTipo) {
        Path buscada = normalizar(Paths.get(rutaTipo));
        Path carpetaBuscada = buscada.getFileName().toString().equals(TipoExpedienteInstanceFileFinder.TIPO_EXPEDIENTE_XML_NAME)
                ? buscada.getParent()
                : buscada;

        List<TipoExpedienteInstanceFile> filtrados = new ArrayList<>();
        for (TipoExpedienteInstanceFile tipoExpedienteFile : tipoExpedienteFiles) {
            if (normalizar(tipoExpedienteFile.getPath()).getParent().equals(carpetaBuscada)) {
                filtrados.add(tipoExpedienteFile);
            }
        }

        if (filtrados.isEmpty()) {
            throw new SalidaException(SALIDA_USO_INCORRECTO,
                    "No hay ningún tipo de expediente en: " + rutaTipo
                    + "\nSe esperaba la ruta de un " + TipoExpedienteInstanceFileFinder.TIPO_EXPEDIENTE_XML_NAME
                    + " o la de la carpeta que lo contiene.");
        }

        return filtrados;
    }

    private static Path normalizar(Path path) {
        return path.toAbsolutePath().normalize();
    }

    /** La ruta relativa al directorio de trabajo si se puede, y si no la absoluta. */
    private static String rutaLegible(Path path) {
        Path absoluta = normalizar(path);
        Path directorioTrabajo = normalizar(Paths.get(""));
        if (absoluta.startsWith(directorioTrabajo)) {
            return directorioTrabajo.relativize(absoluta).toString();
        }
        return absoluta.toString();
    }

    private static void salirConError(int codigoSalida, String mensaje) {
        System.err.println("ERROR: " + mensaje);
        System.exit(codigoSalida);
    }

    /** Error que ya sabe con qué código de salida debe terminar el proceso. */
    private static class SalidaException extends RuntimeException {

        private final int codigoSalida;

        SalidaException(int codigoSalida, String mensaje) {
            super(mensaje);
            this.codigoSalida = codigoSalida;
        }

        int getCodigoSalida() {
            return codigoSalida;
        }
    }

    private static Path getPathDomainModelFile(TipoExpedienteInstanceFile tipoExpedienteFile) {
        return replaceFileName(tipoExpedienteFile.getPath(),"domains.xml");
    }

    private static Path getPathViewsFile(TipoExpedienteInstanceFile tipoExpedienteFile) {
        return replaceFileName(tipoExpedienteFile.getPath(),"views.xml");
    }

    private static Path getPathInitialEventManagerFile(TipoExpedienteInstanceFile tipoExpedienteFile) {
        return replaceFileName(tipoExpedienteFile.getPath(),
                tipoExpedienteFile.getInitialEventManagerClassName() + ".java");
    }

    /** La subcarpeta de la fase dentro de la carpeta de versión: el name de la fase en minúsculas. */
    private static Path getCarpetaFase(TipoExpedienteInstanceFile tipoExpedienteFile, Fase fase) {
        return tipoExpedienteFile.getPath().getParent().resolve(fase.getPackageSimpleName());
    }


    private static Path replaceFileName(Path path,String newFileName) {
        Path parentDirectory = path.getParent();
        Path newPath = parentDirectory.resolve(newFileName);

        return newPath;
    }

}
