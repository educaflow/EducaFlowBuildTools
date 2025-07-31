package com.educaflow.common.buildtools.i18nprocessor;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.w3c.dom.Document;

public class I18NFileFinder {

    
    private final DocumentBuilderFactory documentBuilderFactory;

    public I18NFileFinder() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
    }


    public List<Path>  findI18NFiles(Path directoryPath) {
        final List<Path> result = new ArrayList<>();

        if (!Files.isDirectory(directoryPath)) {
            throw new RuntimeException("La ruta proporcionada no es un directorio válido: " + directoryPath);
        }

        try (Stream<Path> walk = Files.walk(directoryPath)) {
            walk.filter(Files::isRegularFile) 
                .filter(
                        path -> path.getFileName().toString().equals(Main.SOURCE_FILE_NAME_ES) || path.getFileName().toString().equals(Main.SOURCE_FILE_NAME_CA)
                ).forEach(path -> {
                    result.add(path);
                });;
        } catch (Exception ex) {
            throw new RuntimeException("Error al recorrer el directorio " + directoryPath + ": " + ex.getMessage(), ex);
        }

        return result;
    }
    



}
