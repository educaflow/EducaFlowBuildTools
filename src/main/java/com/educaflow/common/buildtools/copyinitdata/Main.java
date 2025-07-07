/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.copyinitdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Main {

    public static void main(String[] args) {
        String origenPath = args[0];
        String destinoPath = args[1];
        String keepSubdirOrigin= args[2];
        String keepSubdirDest= args[3];

        File origenDir = new File(origenPath);
        File destinoDir = new File(destinoPath);

        if (!destinoDir.exists()) {
            destinoDir.mkdirs();
        }

        if (origenDir.exists() && origenDir.isDirectory()) {
            File[] subDirs = origenDir.listFiles(File::isDirectory); 
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    File bFolder = new File(subDir, keepSubdirOrigin);

                    if (bFolder.exists() && bFolder.isDirectory()) {
                        File newADirInDestino = new File(destinoDir, subDir.getName());
                        if (!newADirInDestino.exists()) {
                            newADirInDestino.mkdirs();
                        }

                        File destinoBFolder = new File(newADirInDestino, keepSubdirDest);

                        try {
                            copyFolder(bFolder.toPath(), destinoBFolder.toPath());
                            System.out.println("Copiado: " + bFolder.getAbsolutePath() + " a " + destinoBFolder.getAbsolutePath());
                        } catch (IOException e) {
                            throw new RuntimeException("Error al copiar la carpeta " + bFolder.getAbsolutePath() ,e);
                        }
                    } else {
                        System.out.println("La carpeta " + keepSubdirOrigin + " no se encontró en: " + subDir.getAbsolutePath());
                    }
                }
            }
        } else {
            throw new RuntimeException("El directorio de origen '" + origenPath + "' no existe o no es un directorio.");
        }
    }

    private static void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> {
            Path destination = dest.resolve(src.relativize(source));
            try {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Error al copiar " + source + " a " + destination ,e);
            }
        });
    }
}