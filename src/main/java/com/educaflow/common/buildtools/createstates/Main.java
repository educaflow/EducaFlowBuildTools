package com.educaflow.common.buildtools.createstates;

import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Genera la clase {@code States} de cada tipo de expediente a partir de su
 * {@code TipoExpedienteInstance.xml}.
 *
 * <p>{@code States} NO es un esqueleto: es una proyección literal del XML y se reemite entera en
 * cada build, así que no vive en {@code src/main/java} ni se versiona. Por eso este generador es
 * independiente de {@code createfiles}, que es idempotente y nunca pisa lo escrito.
 *
 * <pre>
 *   Main &lt;origen&gt; &lt;destino&gt; [paqueteRaiz] &lt;domainsTipoExpedienteXml&gt;
 * </pre>
 *
 * donde {@code origen} es la raíz del árbol de fuentes ({@code ./src/main/java}), {@code destino}
 * la raíz de salida ({@code ./build/src-gen-states/main/java}) y {@code domainsTipoExpedienteXml} el
 * dominio del que sale el enum global {@code Profile} contra el que se validan los perfiles
 * (regla 11).
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Uso: Main <origen> <destino> <paqueteRaiz> <domainsTipoExpedienteXml>");
            System.exit(1);
        }

        Path rootPathSourceFiles = Paths.get(args[0]);
        Path rootPathDestino = Paths.get(args[1]);
        String paqueteRaizTramites = TramitesLayout.paqueteRaizFromArgs(args, 2);
        Path domainsTipoExpedienteXml = Paths.get(args[3]);

        System.out.println("Iniciando tarea de generar las clases States de los tipos de expediente....");
        System.out.println("rootPathSourceFiles=" + rootPathSourceFiles);
        System.out.println("rootPathDestino=" + rootPathDestino);

        ProfilesDelDominio profilesDelDominio = ProfilesDelDominio.leer(domainsTipoExpedienteXml);

        TramitesLayout tramitesLayout = new TramitesLayout(rootPathSourceFiles, paqueteRaizTramites);
        List<TipoExpedienteInstanceFile> tiposExpedientes =
                new TipoExpedienteInstanceFileFinder(tramitesLayout).findTiposExpedienteFile();

        for (TipoExpedienteInstanceFile tipoExpediente : tiposExpedientes) {
            System.out.println("Generando States de " + tipoExpediente.getCode()
                    + " en el paquete " + tipoExpediente.getBasePackageName());

            new StatesFile(tipoExpediente, rootPathDestino, profilesDelDominio).generateStates();
        }

        System.out.println("Finalizada tarea de generar las clases States de los tipos de expediente");
    }
}
