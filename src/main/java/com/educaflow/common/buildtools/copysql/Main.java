/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.copysql;

import com.educaflow.common.buildtools.copydomain.*;
import com.educaflow.common.buildtools.copyinitdata.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Main {
    
    public static void main(String[] args) {
        try {
        
        
            String origen = args[0];
            String destino = args[1];


            System.out.println("Iniciando tarea de copiar ficheros SQL....");
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
                List<Path> origenSQLFilesPath=findSQLFiles(origenPath);

                for (Path origenSQLFilePath : origenSQLFilesPath) {

                    Path parteRelativa = origenPath.relativize(origenSQLFilePath);
                    Path destinoSQLFile = destinoPath.resolve(parteRelativa);

                    try {
                        System.out.println("Copiando: " + origenSQLFilePath.toAbsolutePath()+ " a " + destinoSQLFile.toAbsolutePath());
                        Files.createDirectories(destinoSQLFile.getParent());
                        Files.copy(origenSQLFilePath, destinoSQLFile, StandardCopyOption.REPLACE_EXISTING);
                        
                    } catch (IOException ex) {
                        throw new RuntimeException("Error al copiar la carpeta " + origenSQLFilePath.toAbsolutePath()+ " a " + destinoSQLFile.toAbsolutePath(),ex);
                    }

                }

            } else {
                throw new RuntimeException("El directorio de origen '" + origenPath + "' no existe o no es un directorio.");
            }

            System.out.println("Finalizada tarea de copiar ficheros SQL");
        
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
    }


    public static List<Path> findSQLFiles(Path basePath) throws IOException {
        List<Path> ficherosValidos = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(basePath)) {
            List<Path> sqlFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .collect(Collectors.toList());

            for (Path sqlFile : sqlFiles) {
                ficherosValidos.add(sqlFile);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error configurando el parser XML", ex);
        }

        return ficherosValidos;
    }   
    
    

}