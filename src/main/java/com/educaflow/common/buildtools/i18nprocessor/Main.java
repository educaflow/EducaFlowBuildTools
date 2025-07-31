/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.educaflow.common.buildtools.i18nprocessor;

import com.educaflow.common.buildtools.i18nprocessor.generatefile.I18nFiles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author logongas
 */
public class Main {

    public static String SOURCE_FILE_NAME_ES="i18n_es.csv";
    public static String SOURCE_FILE_NAME_CA="i18n_ca.csv";
    
    public static String TARGET_FILE_NAME_ES="custom_es.csv";
    public static String TARGET_FILE_NAME_CA="custom_ca.csv";    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Uso: java Main <ruta_origen> <ruta_destino>");
            return;
        }

        Path pathToSearch = Paths.get(args[0]);
        Path targetBaseDir = Paths.get(args[1]); 

        
        generateI18nFiles(pathToSearch);

        copyI18nFiles(pathToSearch,targetBaseDir);

    }
    
    private static void generateI18nFiles(Path pathToSearch) {
        System.out.println("Iniciando tarea de generar ficheros i18n ....");
        
        
        List<Path> allDirectoriesPath=getDirectoriesIncludingRoot(pathToSearch);
        StringBuilder messages=new StringBuilder();
        
        for(Path directory:allDirectoriesPath) {
            I18nFiles i18nFiles=new I18nFiles(directory);
            String message=i18nFiles.createOrUpdateOrDeleteI18nFiles();
            if (message!=null) {
                messages.append(message);
            } 
        }
        
        if (messages.length()>0) {
            throw new RuntimeException("No fue posible generar lo ficheros de i18n:\n"+messages.toString());
        }
        
        System.out.println("Finalizada tarea de generar fichero i18n"); 
    }
    
    
    private static void copyI18nFiles(Path pathToSearch,Path targetBaseDir) {
        try {
            System.out.println("Iniciando tarea de copiar fichero i18n ....");

            I18NFileFinder finder = new I18NFileFinder();
            List<Path> files=finder.findI18NFiles(pathToSearch);

            Files.createDirectories(targetBaseDir);

            for (Path filePath : files) {

                try {
                    Path relativePath = pathToSearch.relativize(filePath);
                    Path newFilePathInTarget = targetBaseDir.resolve(relativePath);

                    if (newFilePathInTarget.getFileName().toString().equals(SOURCE_FILE_NAME_ES)) {
                        newFilePathInTarget=newFilePathInTarget.resolveSibling(TARGET_FILE_NAME_ES);
                    } else if (newFilePathInTarget.getFileName().toString().equals(SOURCE_FILE_NAME_CA)) {
                        newFilePathInTarget=newFilePathInTarget.resolveSibling(TARGET_FILE_NAME_CA);
                    } else {
                        throw new RuntimeException("El nombre del fichero no es válido:"+newFilePathInTarget);
                    }


                    Files.createDirectories(newFilePathInTarget.getParent()); 
                    Files.copy(filePath, newFilePathInTarget, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("copiado fichero a "+newFilePathInTarget);


                } catch (Exception ex) {
                    throw new RuntimeException("Fallo al prerocesar el fichero: " + filePath,ex);
                }
            }

            System.out.println("Finalizada tarea de copiar fichero i18n"); 
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    
    public static List<Path> getDirectoriesIncludingRoot(Path rootDir) {
        
        try {
            try (Stream<Path> stream = Files.walk(rootDir)) {
                return stream
                        .filter(Files::isDirectory)
                        .collect(Collectors.toList());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }    


    
}
