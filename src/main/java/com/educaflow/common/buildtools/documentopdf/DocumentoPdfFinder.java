/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.documentopdf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author logongas
 */
public class DocumentoPdfFinder {
    public List<Path>  findFiles(Path directoryPath) {
        final List<Path> result = new ArrayList<>();

        if (!Files.isDirectory(directoryPath)) {
            throw new RuntimeException("La ruta proporcionada no es un directorio válido: " + directoryPath);
        }

        try (Stream<Path> walk = Files.walk(directoryPath)) {
            walk.filter(Files::isRegularFile) 
                .filter(
                        path -> path.getFileName().toString().endsWith(".pdf")
                ).forEach(path -> {
                    result.add(path);
                });;
        } catch (Exception ex) {
            throw new RuntimeException("Error al recorrer el directorio " + directoryPath + ": " + ex.getMessage(), ex);
        }

        return result;
    }
}
