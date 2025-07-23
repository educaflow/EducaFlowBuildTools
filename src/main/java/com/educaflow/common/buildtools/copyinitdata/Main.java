/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.copyinitdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String DATA_INIT_DIR="data-init";
    private static final String INPUT_CONFIG_FILE="input-config.xml";
    private static final String INPUT_DIR="input";
    
    
    public static void main(String[] args) {
        try {
        
        
            String origen = args[0];
            String destino = args[1];


            System.out.println("Iniciando tarea de copiar carpetas data-init....");
            System.out.println("origen="+origen);
            System.out.println("destino="+destino);


            File origenDir = new File(origen);
            File destinoDir = new File(destino);
            Path origenPath = Paths.get(origen);
            Path destinoPath = Paths.get(destino);
            
            if (!destinoDir.exists()) {
                destinoDir.mkdirs();
            }

            if (origenDir.exists() && origenDir.isDirectory()) {
                List<Path> origenDataInitPaths=findDataInitWithInputConfig(origenPath);

                for (Path origenDataInitPath : origenDataInitPaths) {

                    Path parteRelativa = origenPath.relativize(origenDataInitPath);
                    Path destinoDataInitPath = destinoPath.resolve(parteRelativa);
                    Files.createDirectories(destinoDataInitPath.resolve(INPUT_DIR));
                    
                    try {
                        System.out.println("Copiando: " + parteRelativa);
                        Files.copy(origenDataInitPath.resolve(INPUT_CONFIG_FILE), destinoDataInitPath.resolve(INPUT_CONFIG_FILE), StandardCopyOption.REPLACE_EXISTING);
                        copyFolder(origenDataInitPath.resolve(INPUT_DIR), destinoDataInitPath.resolve(INPUT_DIR));
                        
                    } catch (IOException ex) {
                        throw new RuntimeException("Error al copiar la carpeta " + origenDataInitPath.toAbsolutePath()+ " a " + destinoDataInitPath.toAbsolutePath(),ex);
                    }

                }

            } else {
                throw new RuntimeException("El directorio de origen '" + origenPath + "' no existe o no es un directorio.");
            }

            System.out.println("Finalizada tarea de copiar data-init");
        
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
    }

    public static List<Path> findDataInitWithInputConfig(Path basePath) throws IOException {
        List<Path> carpetasEncontradas = new ArrayList<>();

        Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.getFileName().toString().equals(DATA_INIT_DIR)) {
                    Path inputConfig = dir.resolve(INPUT_CONFIG_FILE);
                    if (Files.isRegularFile(inputConfig)) {
                        carpetasEncontradas.add(dir);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return carpetasEncontradas;
    }    
    
    
    public static void copyFolder(Path sourceDir, Path targetDir) throws IOException {
        // Validaciones iniciales
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("La ruta de origen no existe o no es un directorio: " + sourceDir);
        }
        if (Files.exists(targetDir) && !Files.isDirectory(targetDir)) {
            throw new IllegalArgumentException("La ruta de destino existe pero no es un directorio: " + targetDir);
        }

        // Crear el directorio de destino si no existe
        Files.createDirectories(targetDir);

        // Usamos Files.walk para recorrer el árbol de directorios de forma recursiva
        // y copiamos cada Path al destino.
        Files.walk(sourceDir)
             .forEach(sourcePath -> {
                 try {
                     Path relativePath = sourceDir.relativize(sourcePath);
                     Path targetPath = targetDir.resolve(relativePath);

                     if (Files.isDirectory(sourcePath)) {
                         // Si es un directorio, crearlo en el destino
                         Files.createDirectories(targetPath);
                     } else {
                         // Si es un archivo, copiarlo (sobrescribir si existe)
                         Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                     }
                 } catch (IOException e) {
                     // Aquí podrías manejar el error de forma más sofisticada,
                     // por ejemplo, loguearlo o relanzar una excepción específica.
                     System.err.println("Error al copiar " + sourcePath + " a " + targetDir + ": " + e.getMessage());
                     throw new RuntimeException("Fallo en la copia", e); // Re-lanzamos como RuntimeException para detener el forEach
                 }
             });
    }
}