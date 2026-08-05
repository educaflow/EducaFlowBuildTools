package com.educaflow.common.buildtools.createdatainittramite;

import com.educaflow.common.buildtools.common.FileUtil;
import com.educaflow.common.buildtools.files.tramite.TramiteInstanceFile;
import com.educaflow.common.buildtools.files.tramite.TramiteInstanceFileFinder;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Genera los data-init de cada trámite (la definición con priority="1" y el
 * tipo de expediente activo con priority="-1") a partir de su
 * TramiteInstance.xml.
 *
 * Uso: java Main &lt;ruta_origen&gt; &lt;ruta_destino&gt; [paqueteRaiz]
 *
 * @author logongas
 */
public class Main {

    public static void main(String[] args) {
        Path rootPathSourceFiles = Paths.get(args[0]);
        Path rootPathBuildResources = Paths.get(args[1]);
        String paqueteRaizTramites = TramitesLayout.paqueteRaizFromArgs(args, 2);

        System.out.println("Iniciando tarea de generar los data-init de los trámites....");
        System.out.println("rootPathSourceFiles="+rootPathSourceFiles);
        System.out.println("rootPathBuildResources="+rootPathBuildResources);

        //Se borra siempre: si la raíz de los trámites ha desaparecido, sus
        //data-init de un build anterior tampoco deben quedarse.
        Path rootPathTramites=rootPathBuildResources.resolve("tramites");
        FileUtil.borrarDirectorioRecursivo(rootPathTramites);

        TramitesLayout tramitesLayout=new TramitesLayout(rootPathSourceFiles, paqueteRaizTramites);

        if (tramitesLayout.existeRaiz()==false) {
            System.out.println("No existe "+tramitesLayout.getRootPackagePath()+"; no hay ningún trámite");
            System.out.println("Finalizada tarea de generar los data-init de los trámites");
            return;
        }

        tramitesLayout.checkTramitesNoAnidados();

        List<TramiteInstanceFile> tramites=new TramiteInstanceFileFinder(tramitesLayout).findTramitesFile();

        for (TramiteInstanceFile tramite : tramites) {
            tramite.check();

            System.out.println("Encontrado Tramite instancia:"+tramite.getName()+ " en " + tramite.getPath());

            InitDataTramiteFiles initDataTramiteFiles=new InitDataTramiteFiles(tramite, rootPathTramites);

            initDataTramiteFiles.generateInitData();
        }

        System.out.println("Finalizada tarea de generar los data-init de los trámites");
    }

}
