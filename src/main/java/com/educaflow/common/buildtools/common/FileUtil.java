package com.educaflow.common.buildtools.common;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
    
}
