package com.educaflow.common.buildtools.documentopdf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 *
 * @author logongas
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java Main <ruta_origen> <ruta_destino>");
            return;
        }

        Path pathToSearch = Paths.get(args[0]);
        Path targetBaseDir = Paths.get(args[1]); 

        

        copyPdfFiles(pathToSearch,targetBaseDir);
    }
    
    private static void copyPdfFiles(Path pathToSearch,Path targetBaseDir) {
        try {
            System.out.println("Iniciando tarea de copiar ficheros PDF ....");

            DocumentoPdfFinder finder = new DocumentoPdfFinder();
            List<Path> files=finder.findFiles(pathToSearch);

            Files.createDirectories(targetBaseDir);

            for (Path filePath : files) {

                try {
                    Path relativePath = pathToSearch.relativize(filePath);
                    Path newFilePathInTarget = targetBaseDir.resolve(relativePath);



                    Files.createDirectories(newFilePathInTarget.getParent()); 
                    Files.copy(filePath, newFilePathInTarget, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("copiado fichero a "+newFilePathInTarget);


                } catch (Exception ex) {
                    throw new RuntimeException("Fallo al prerocesar el fichero: " + filePath,ex);
                }
            }

            System.out.println("Finalizada tarea de copiar ficheros PDF"); 
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }    
    
}
