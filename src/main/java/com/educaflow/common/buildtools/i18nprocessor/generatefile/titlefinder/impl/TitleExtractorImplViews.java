/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.impl;

import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.TitleExtractor;
import java.io.File;
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
public class TitleExtractorImplViews implements TitleExtractor {

    @Override
    public List<Path> findTitlesFilesInDirectory(Path directoryPath) {
        List<Path> xmlFiles = TitleExtractorUtil.findFilesByExtension(directoryPath,".xml");

        return xmlFiles.stream().filter(file -> isViewFile(file)).collect(Collectors.toList());
    }

    
    @Override
    public List<String> getTitlesFromFile(Path viewFile) {
        List<String> titles = new ArrayList<>();

        Document document = XMLUtil.getDocument(viewFile);

        //Campos del dominio
        List<Element> elementsWithTitle = XMLUtil.getElementsFromEvaluateXPath("//*[@title]", document.getDocumentElement(), true);
        for (Element elementWithTitle : elementsWithTitle) {
            String title = XMLUtil.getStringAttribute(elementWithTitle, "title", null);
            if (title != null) {
                titles.add(title);
            }
        }

        return titles;

    }    

    
    private boolean isViewFile(Path filePath) {
        Document document = XMLUtil.getDocument(filePath);

        if ("object-views".equals(document.getDocumentElement().getTagName())) {
            return true;
        } else {
            return false;
        }
    }


}
