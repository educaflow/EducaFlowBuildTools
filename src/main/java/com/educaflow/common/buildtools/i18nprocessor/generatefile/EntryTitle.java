package com.educaflow.common.buildtools.i18nprocessor.generatefile;

import java.nio.file.Path;

/**
 *
 * @author logongas
 */
public class EntryTitle {

    private final String title;
    private final Path path;

    public EntryTitle(String title, Path path) {
        this.title = title;
        this.path = path;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }

}
