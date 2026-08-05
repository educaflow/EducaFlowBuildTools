/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.createfiles;

import com.educaflow.common.buildtools.files.domainmodel.DomainModelFile;
import com.educaflow.common.buildtools.files.eventmanagerfile.EventManagerFile;
import com.educaflow.common.buildtools.files.stateeventvalidator.StateEventValidatorFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;
import com.educaflow.common.buildtools.files.views.ViewsFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera los esqueletos que le falten a cada tipo de expediente: {@code domains.xml},
 * {@code views.xml}, el {@code .java} del {@code EventManager} y el {@code .kt} del
 * {@code StateEventValidator}, todos hermanos de su {@code TipoExpedienteInstance.xml}.
 *
 * <p>Es <b>solo un generador</b> e <b>idempotente</b>: crea únicamente los ficheros que no existen,
 * nunca pisa fuentes editadas a mano. La comprobación de que el código escrito a mano concuerda con
 * la máquina de estados del XML <b>ya no vive aquí</b>: está en los tests de {@code secretaria-virtual}
 * ({@code src/test/java/com/educaflow/tiposexpedientes}), que leen bytecode y por eso alcanzan también
 * al {@code StateEventValidator} escrito en Kotlin.
 *
 * <p>Uso:
 * <pre>
 *   Main &lt;origen&gt; [paqueteRaiz] [--tipo=&lt;ruta&gt;]
 * </pre>
 * donde {@code origen} es la raíz del árbol de fuentes (p.ej. {@code ./src/main/java}),
 * {@code paqueteRaiz} es el paquete raíz de los trámites (por omisión
 * {@link TramitesLayout#PAQUETE_RAIZ_POR_DEFECTO}) y {@code --tipo=} acota la generación a un único
 * tipo de expediente, indicando la ruta de su {@code TipoExpedienteInstance.xml} o la de la carpeta
 * que lo contiene. Sin {@code --tipo=} se procesan todos los trámites, que es lo que hace el build.
 *
 * <p>Códigos de salida: {@code 0} todo bien (se hayan creado ficheros o no), {@code 1} uso incorrecto
 * (argumentos inválidos o {@code --tipo=} sin coincidencias) y {@code 2} error procesando los tipos
 * de expediente (XML inválido, error de E/S…).
 *
 * @author logongas
 */
public class Main {

    private static final String OPCION_TIPO = "--tipo=";

    private static final int SALIDA_OK = 0;
    private static final int SALIDA_USO_INCORRECTO = 1;
    private static final int SALIDA_ERROR_PROCESANDO = 2;

    public static void main(String[] args) {
        List<String> posicionales = new ArrayList<>();
        String rutaTipo = null;

        for (String arg : args) {
            if (arg.startsWith(OPCION_TIPO)) {
                rutaTipo = arg.substring(OPCION_TIPO.length()).trim();
                if (rutaTipo.isEmpty()) {
                    salirConError(SALIDA_USO_INCORRECTO, "La opción " + OPCION_TIPO + " necesita una ruta");
                }
            } else {
                posicionales.add(arg);
            }
        }

        if (posicionales.isEmpty()) {
            salirConError(SALIDA_USO_INCORRECTO,
                    "Uso: Main <origen> [paqueteRaiz] [" + OPCION_TIPO + "<ruta>]");
        }

        Path rootPathSourceFiles = Paths.get(posicionales.get(0));
        String paqueteRaizTramites = TramitesLayout.paqueteRaizFromArgs(posicionales.toArray(new String[0]), 1);

        try {
            generar(rootPathSourceFiles, paqueteRaizTramites, rutaTipo);
        } catch (SalidaException ex) {
            salirConError(ex.getCodigoSalida(), ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            salirConError(SALIDA_ERROR_PROCESANDO, ex.getMessage());
        }

        System.exit(SALIDA_OK);
    }

    private static void generar(Path rootPathSourceFiles, String paqueteRaizTramites, String rutaTipo) {
        TramitesLayout tramitesLayout = new TramitesLayout(rootPathSourceFiles, paqueteRaizTramites);
        List<TipoExpedienteInstanceFile> tipoExpedienteFiles = new TipoExpedienteInstanceFileFinder(tramitesLayout).findTiposExpedienteFile();

        if (rutaTipo != null) {
            tipoExpedienteFiles = filtrarPorTipo(tipoExpedienteFiles, rutaTipo);
        }

        int creados = 0;
        int existentes = 0;

        for (TipoExpedienteInstanceFile tipoExpedienteFile : tipoExpedienteFiles) {

            Path pathDomainModelFile = getPathDomainModelFile(tipoExpedienteFile);
            DomainModelFile domainModelFile = new DomainModelFile(pathDomainModelFile, tipoExpedienteFile);
            if (domainModelFile.createDomainModelIfNotExists()) {
                creados++;
                System.out.println("CREADO " + rutaLegible(pathDomainModelFile));
            } else {
                existentes++;
            }

            Path pathViewsFile = getPathViewsFile(tipoExpedienteFile);
            ViewsFile viewsFile = new ViewsFile(pathViewsFile, tipoExpedienteFile);
            if (viewsFile.createViewsFileIfNotExists()) {
                creados++;
                System.out.println("CREADO " + rutaLegible(pathViewsFile));
            } else {
                existentes++;
            }

            Path pathEventManagerFile = getPathEventManagerFile(tipoExpedienteFile);
            EventManagerFile eventManagerFile = new EventManagerFile(pathEventManagerFile, tipoExpedienteFile);
            if (eventManagerFile.createEventManagerFileIfNotExists()) {
                creados++;
                System.out.println("CREADO " + rutaLegible(pathEventManagerFile));
            } else {
                existentes++;
            }

            Path pathStateEventValidatorFile = getPathStateEventValidatorFile(tipoExpedienteFile);
            StateEventValidatorFile stateEventValidatorFile = new StateEventValidatorFile(pathStateEventValidatorFile, tipoExpedienteFile);
            if (stateEventValidatorFile.createStateEventValidatorFileIfNotExists()) {
                creados++;
                System.out.println("CREADO " + rutaLegible(pathStateEventValidatorFile));
            } else {
                existentes++;
            }

        }

        System.out.println("Resumen: " + creados + " fichero(s) creado(s), " + existentes
                + " ya existente(s) en " + tipoExpedienteFiles.size() + " tipo(s) de expediente.");
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

    private static Path getPathEventManagerFile(TipoExpedienteInstanceFile tipoExpedienteFile) {
        return replaceFileName(tipoExpedienteFile.getPath(),tipoExpedienteFile.getEventManagerClassName()+".java");
    }

    private static Path getPathStateEventValidatorFile(TipoExpedienteInstanceFile tipoExpedienteFile) {
        return replaceFileName(tipoExpedienteFile.getPath(),tipoExpedienteFile.getStateEventValidatorClassName()+".kt");
    }


    private static Path replaceFileName(Path path,String newFileName) {
        Path parentDirectory = path.getParent();
        Path newPath = parentDirectory.resolve(newFileName);

        return newPath;
    }

}
