package com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.impl;

import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.TitleExtractor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author logongas
 */
public class TitleExtractorImplTramiteInstance implements TitleExtractor {

    static final public String TRAMITE_XML_NAME="TramiteInstance.xml";

    @Override
    public List<Path> findTitlesFilesInDirectory(Path directoryPath) {
        List<Path> xmlFiles=TitleExtractorUtil.findFilesByExtension(directoryPath,".xml");

        return xmlFiles.stream().filter( path -> isTramiteInstance(path)).collect(Collectors.toList());
    }

    private boolean isTramiteInstance(Path filePath) {
        return filePath.getFileName().toString().equals(TRAMITE_XML_NAME);
    }

    @Override
    public List<String> getTitlesFromFile(Path tramiteInstanceFilePath) {
        List<String> titles=new ArrayList<>();

        Document document=XMLUtil.getDocument(tramiteInstanceFilePath);
        Element nameElement=XMLUtil.getChildFilterByTagName(document.getDocumentElement(), "name");

        if ((nameElement!=null) && (!nameElement.getTextContent().isBlank())) {
            titles.add("value:"+nameElement.getTextContent().trim());
        }

        return titles;
    }

}
