package com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.impl;

import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.i18nprocessor.generatefile.AxelorInflector;
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
public class TitleExtractorImplDomainModel implements TitleExtractor {
    
    
    @Override
    public List<Path> findTitlesFilesInDirectory(Path directoryPath) {
        List<Path> xmlFiles=TitleExtractorUtil.findFilesByExtension(directoryPath,".xml");
        
        return xmlFiles.stream().filter( file -> isDomainModelFile(file)).collect(Collectors.toList());
    }
    
    private boolean isDomainModelFile(Path filePath) {
        Document document=XMLUtil.getDocument(filePath);
        
        if ("domain-models".equals(document.getDocumentElement().getTagName())) {
            return true;
        } else {
            return false;
        }
        
    }    
    
    @Override
    public List<String> getTitlesFromFile(Path domainFile) {
        List<String> titles=new ArrayList<>();
        
        
        Document document=XMLUtil.getDocument(domainFile);
        
        
        //Campos del dominio
        List<Element> fields=XMLUtil.getElementsFromEvaluateXPath("//entity/*[@name]", document.getDocumentElement(),true);
        for(Element fieldElement:fields) {
            String name=fieldElement.getAttribute("name");
            String title=XMLUtil.getStringAttribute(fieldElement, "title", null);
            
            if (title==null) {
                title=AxelorInflector.humanize(name);
            }
            titles.add(title);
        }
        
        //Enumerados
        List<Element> items=XMLUtil.getElementsFromEvaluateXPath("//enum/item", document.getDocumentElement(),true);
        for(Element itemElement:items) {
            String name=itemElement.getAttribute("name");
            String title=XMLUtil.getStringAttribute(itemElement, "title", null);
            
            if (title==null) {
                title=AxelorInflector.humanize(name);
            }
            titles.add(title);
        }  
        
        
        
        return titles;
        
    }     
    
    
    
    
    

}
