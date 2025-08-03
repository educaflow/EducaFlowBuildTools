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
public class TitleExtractorImplTramites implements TitleExtractor {
    
    
    @Override
    public List<Path> findTitlesFilesInDirectory(Path directoryPath) {
        List<Path> xmlFiles=TitleExtractorUtil.findFilesByExtension(directoryPath,".xml");
        
        return xmlFiles.stream().filter( path -> isTramites(path)).collect(Collectors.toList());
    }
    
    private boolean isTramites(Path filePath) {
        Path inputPath=filePath.getParent();
        
        if ((inputPath!=null) && (inputPath.getFileName().toString().equals("input"))) {
            Path dataInit=inputPath.getParent();
            if ((dataInit!=null) && (dataInit.getFileName().toString().equals("data-init"))) {
                
                Document document=XMLUtil.getDocument(filePath);

                if ("datos".equals(document.getDocumentElement().getTagName())) {
                    
                    Element tramitesElement=XMLUtil.getChildFilterByTagName(document.getDocumentElement(), "tramites");
                    if (tramitesElement!=null) {
                        return true;
                    }
                }
                
            }
            
        }
        
        return false;
    }
    
    @Override
    public List<String> getTitlesFromFile(Path tramitesFilePath) {
        List<String> titles=new ArrayList<>();
        
        Document document=XMLUtil.getDocument(tramitesFilePath);
        Element datos=document.getDocumentElement();
        Element tramitesElement=XMLUtil.getChildFilterByTagName(document.getDocumentElement(), "tramites");
        List<Element> tramites=XMLUtil.getChildsFilterByTagName(tramitesElement,"tramite");
        
        
        for(Element tramite:tramites) {
            titles.add("value:"+tramite.getAttribute("name"));
        }
        
        
        return titles;
        
    }     
    
    
}
