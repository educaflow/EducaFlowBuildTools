package com.educaflow.common.buildtools.common;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 *
 * @author logongas
 */
public class FileUtil {
    
    
    public static void ls(Path path) {
        if (path != null && Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                System.out.println("Ficheros en la carpeta: " + path);
                for (Path entry : stream) {
                    System.out.println(entry.getFileName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No existe la carpeta o no es un directorio");
        }
    }

    /**
     * Borra la carpeta y todo su contenido. Si no existe no hace nada.
     */
    public static void borrarDirectorioRecursivo(Path path) {
        if (Files.exists(path) == false) {
            return;
        }

        try (Stream<Path> walk = Files.walk(path)) {
            for (Path entry : walk.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) {
                Files.delete(entry);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Fallo al borrar la carpeta:" + path, ex);
        }
    }

}
