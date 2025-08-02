package com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.impl;

import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.State;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder;
import static com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder.TIPO_EXPEDIENTE_XML_NAME;
import com.educaflow.common.buildtools.i18nprocessor.generatefile.AxelorInflector;
import com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.TitleExtractor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author logongas
 */
public class TitleExtractorImplTipoExpedienteInstance implements TitleExtractor {
    
    
    @Override
    public List<Path> findTitlesFilesInDirectory(Path directoryPath) {
        List<Path> xmlFiles=TitleExtractorUtil.findFilesByExtension(directoryPath,".xml");
        
        return xmlFiles.stream().filter( path -> path.getFileName().toString().equals(TIPO_EXPEDIENTE_XML_NAME)).collect(Collectors.toList());
    }
    
    @Override
    public List<String> getTitlesFromFile(Path tipoExpedienteInstanceFilePath) {
        List<String> titles=new ArrayList<>();
        
        TipoExpedienteInstanceFile tipoExpedienteInstanceFile=TipoExpedienteInstanceFileFinder.parseTipoExpedienteXml(tipoExpedienteInstanceFilePath);

        
        titles.add("value:"+tipoExpedienteInstanceFile.getName());
        
        

        for(State state:tipoExpedienteInstanceFile.getStates()) {
            String title=state.getTitle();
            
            if ((title==null) || (title.isBlank())){
                title=AxelorInflector.humanize(state.getName());
            }
            
            
            titles.add("value:"+title);
        }
        
        
        
        return titles;
        
    }     
    
    
}
