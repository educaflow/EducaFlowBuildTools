package com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author logongas
 */
public class TitleExtractorUtil {

    public static List<Path> findFilesByExtension(Path directoryPath,String extension) {
        List<Path> xmlPaths = new ArrayList<>();

        File directory = directoryPath.toFile();

        File[] xmlFilesArr = directory.listFiles((dir, name) -> name.endsWith(extension));

        for (File file : xmlFilesArr) {
            xmlPaths.add(file.toPath());
        }

        return xmlPaths;
    }
}
