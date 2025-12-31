package com.educaflow.common.buildtools.generatedocs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceStringReader;



/**
 *
 * @author logongas
 */
public class Main {

    public static final  String UML_FILE_EXTENSION1="plantuml";
    public static final  String UML_FILE_EXTENSION2="puml";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Uso: java Main <ruta_origen> ");
            return;
        }

        Path pathToSearch = Paths.get(args[0]);

        generateDiagramaUML(pathToSearch);

    }

    private static void generateDiagramaUML(Path pathToSearch) {
        System.out.println("Iniciando tarea de generar UML ....");
        List<Path> plantUMLFiles = findPlantUMLFiles(pathToSearch);

        for (Path plantUMLFile : plantUMLFiles) {
            try {

                generaraDiagramaUML(plantUMLFile);
                
            } catch (Exception ex) {
                throw new RuntimeException("Fallo al generar el fichero de PlantUML:" + plantUMLFile, ex);
            }
        }

        System.out.println("Finalizada tarea de generar UML");
    }

    public static List<Path> findPlantUMLFiles(Path basePath) {
        try (Stream<Path> paths = Files.walk(basePath)) {
            List<Path> plantUMLFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("."+UML_FILE_EXTENSION1) || path.toString().endsWith("."+UML_FILE_EXTENSION2))
                    .collect(Collectors.toList());

            return plantUMLFiles;

        } catch (Exception ex) {
            throw new RuntimeException("Error buscado ficheros de UML", ex);
        }

    }

    private static void generaraDiagramaUML(Path plantUMLFilePath) {
        try {
            String filename = plantUMLFilePath.getFileName().toString();
            String newFilename = filename.replaceFirst("\\." + UML_FILE_EXTENSION1 + "$", ".png").replaceFirst("\\." + UML_FILE_EXTENSION2 + "$", ".png");
            Path outputPath = plantUMLFilePath.getParent().resolve(newFilename);

            
            String source = new String(Files.readAllBytes(plantUMLFilePath));
            if (source.contains("@startuml")==false) {
                source = "@startuml\n" + source;
            }
             if (source.contains("@enduml")==false) {
                source = source +"\n\n@enduml";
            }           
            
            
            SourceStringReader reader = new SourceStringReader(source);
            
            System.out.println("Generando UML Diagram en:"+outputPath);
            try (OutputStream os = Files.newOutputStream(outputPath)) {
                reader.outputImage(os);
            }

        
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    

}
