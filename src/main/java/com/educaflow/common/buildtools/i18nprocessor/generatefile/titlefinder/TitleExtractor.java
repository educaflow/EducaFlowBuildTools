package com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder;

import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author logongas
 */
public interface TitleExtractor {

    List<Path> findTitlesFilesInDirectory(Path directoryPath);
    List<String> getTitlesFromFile(Path titleFile);


}
