package com.educaflow.common.buildtools.i18nprocessor.generatefile;

import com.educaflow.common.buildtools.common.XMLUtil;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author logongas
 */
public class TitleFinder {
    
    public static List<EntryTitle> getTitles(Path directoryPath) { 
        List<EntryTitle> titles=new ArrayList<>();

        List<Path> domainModelFiles = findDomainModelinFilesInDirectory(directoryPath);
        titles.addAll(processFiles(domainModelFiles, path -> getTitlesFromDomain(path)));

        List<Path> viewFiles = findViewFilesInDirectory(directoryPath);
        titles.addAll(processFiles(viewFiles, path -> getTitlesFromViews(path)));

        return titles;
    }   
    

    
    /*********************************************************************************/
    /****************************** Ficheros de Modelos ******************************/
    /*********************************************************************************/
    
    private static List<Path> findViewFilesInDirectory(Path directoryPath) {        
        List<Path> xmlFiles=findXMLFiles(directoryPath);
        
        return xmlFiles.stream().filter( file -> isViewFile(file)).collect(Collectors.toList());
    }
    
    private static boolean isDomainModelFile(Path filePath) {
        Document document=XMLUtil.getDocument(filePath);
        
        if ("domain-models".equals(document.getDocumentElement().getTagName())) {
            return true;
        } else {
            return false;
        }
        
    }    
    
    private static List<String> getTitlesFromDomain(Path domainFile) {
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
    
    /********************************************************************************/
    /****************************** Ficheros de Vistas ******************************/
    /********************************************************************************/
    
    
    private static List<Path> findDomainModelinFilesInDirectory(Path directoryPath) {
        List<Path> xmlFiles=findXMLFiles(directoryPath);
        
        return xmlFiles.stream().filter( file -> isDomainModelFile(file)).collect(Collectors.toList());
    }    

    
    private static boolean isViewFile(Path filePath) {
        Document document=XMLUtil.getDocument(filePath);
        
        if ("object-views".equals(document.getDocumentElement().getTagName())) {
            return true;
        } else {
            return false;
        }
    }   
    
    
    private static List<String> getTitlesFromViews(Path viewFile) {
        List<String> titles=new ArrayList<>();
        
        
        Document document=XMLUtil.getDocument(viewFile);
        
        
        //Campos del dominio
        List<Element> elementsWithTitle=XMLUtil.getElementsFromEvaluateXPath("//*[@title]", document.getDocumentElement(),true);
        for(Element elementWithTitle:elementsWithTitle) {
            String title=XMLUtil.getStringAttribute(elementWithTitle, "title", null);
            if (title!=null) {
                titles.add(title);
            }
        }
        
        
        return titles;
        
    }    
         
    
    
    
    
    /*******************************************************************************/
    /****************************** Funciones Comunes ******************************/
    /*******************************************************************************/    
    
    

    private static List<EntryTitle> processFiles(List<Path> files, Function<Path, List<String>> titleExtractor) {
        List<EntryTitle> titles = new ArrayList<>();
        for (Path file : files) {
            List<String> fileTitles = titleExtractor.apply(file);
            for (String title : fileTitles) {
                EntryTitle entryTitle=new EntryTitle(title,file);
                titles.add(entryTitle);
            }
        }

        return titles;
    }     
    
    private static List<Path> findXMLFiles(Path directoryPath) {
        List<Path> xmlPaths=new ArrayList<>();
       
        File directory = directoryPath.toFile();

        File[] xmlFilesArr = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));


        for(File file:xmlFilesArr) {
            xmlPaths.add(file.toPath());
        }
        
        return xmlPaths;
    }    
    
            
}
