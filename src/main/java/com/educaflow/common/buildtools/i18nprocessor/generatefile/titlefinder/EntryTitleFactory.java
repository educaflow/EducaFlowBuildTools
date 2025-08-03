package com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder;

import com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.impl.TitleExtractorImplViews;
import com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.impl.TitleExtractorImplDomainModel;
import com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.impl.TitleExtractorImplTipoExpedienteInstance;
import com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.impl.TitleExtractorImplTramites;
import com.educaflow.common.buildtools.i18nprocessor.generatefile.EntryTitle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author logongas
 */
public abstract class EntryTitleFactory {
    
    public static List<EntryTitle> getEntryTitles(Path directoryPath) { 
        List<EntryTitle> titles=new ArrayList<>();

        
        List<TitleExtractor> titleExtractors = List.of(
                new TitleExtractorImplDomainModel(),
                new TitleExtractorImplViews(),
                new TitleExtractorImplTipoExpedienteInstance(),
                new TitleExtractorImplTramites()
        );

        
        for(TitleExtractor titleExtractor:titleExtractors) {
            List<Path> domainModelFiles = titleExtractor.findTitlesFilesInDirectory(directoryPath);
            List<EntryTitle> fileEntryTitles=getEntryTitlesFromFile(domainModelFiles, path -> titleExtractor.getTitlesFromFile(path));
            titles.addAll(fileEntryTitles); 
        }


        return titles;
    }   
    
    
    private static List<EntryTitle> getEntryTitlesFromFile(List<Path> files, Function<Path, List<String>> titleExtractor) {
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


            
}
