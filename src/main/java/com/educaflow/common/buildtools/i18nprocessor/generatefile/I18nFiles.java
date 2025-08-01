package com.educaflow.common.buildtools.i18nprocessor.generatefile;

import com.educaflow.common.buildtools.i18nprocessor.generatefile.titlefinder.EntryTitleFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author logongas
 */
public class I18nFiles {

    Path directoryPath;
    
    List<TextoTraducible> textosTraduciblesCastellano;
    List<TextoTraducible> textosTraduciblesValenciano;

    public I18nFiles(Path directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String createOrUpdateOrDeleteI18nFiles() {  
        Path filePathCastellano = getFilePath(this.directoryPath,Idioma.Castellano);
        Path filePathValenciano = getFilePath(this.directoryPath,Idioma.Valenciano);
        
        
        List<TextoTraducible> textosTraduciblesOriginalCastellano=getTextosTraduciblesFromI18NFilePath(filePathCastellano);
        List<TextoTraducible> textosTraduciblesOriginalValenciano=getTextosTraduciblesFromI18NFilePath(filePathValenciano);

        List<EntryTitle> entryTitles=EntryTitleFactory.getEntryTitles(directoryPath);
        String messagesFallosTraduccionCastellano=updateTextos(textosTraduciblesOriginalCastellano,entryTitles,Idioma.Castellano);
        String messagesFallosTraduccionValenciano=updateTextos(textosTraduciblesOriginalValenciano,entryTitles,Idioma.Valenciano);
        
        String messagesFallosTraduccionVacioCastellano=traducirVacios(textosTraduciblesOriginalCastellano,Idioma.Castellano);
        String messagesFallosTraduccionVacioValenciano=traducirVacios(textosTraduciblesOriginalValenciano,Idioma.Valenciano);
        
        
        if (textosTraduciblesCastellano.size()!=textosTraduciblesValenciano.size()) {
            throw new RuntimeException("El tamaño de las listas no coincide:"+ textosTraduciblesCastellano.size()+ " y " + textosTraduciblesValenciano.size());
        }
        
        System.out.println(textosTraduciblesCastellano.size() + " --> " + directoryPath);
        
                
        StringBuilder messages = new StringBuilder();
        if ((messagesFallosTraduccionCastellano!=null) || (messagesFallosTraduccionVacioCastellano!=null)) {
            messages.append("\n--------Fichero "+filePathCastellano.toAbsolutePath().toString()+"\nFallo al traducir estos campos (debe modificar o añadir el atributo title correspondiente):\n"+ messagesFallosTraduccionCastellano+"\n"+messagesFallosTraduccionVacioCastellano);
        }
        if ((messagesFallosTraduccionValenciano!=null) || (messagesFallosTraduccionVacioValenciano!=null)){
            messages.append("\n--------Fichero "+filePathValenciano.toAbsolutePath().toString()+"\nFallo al traducir estos campos (debe modificar o añadir el atributo title correspondiente):\n"+ messagesFallosTraduccionValenciano+"\n"+messagesFallosTraduccionVacioValenciano);
        }
     
        
        
	if (messages.length()>0) {
            return messages.toString()+"\n";
        } 


        if ((textosTraduciblesCastellano.size()==0) && (textosTraduciblesValenciano.size()==0)) {
            filePathCastellano.toFile().delete();
            filePathValenciano.toFile().delete();
        } else {
            TextoTraducibleRepository.writeToI18NFile(textosTraduciblesCastellano,filePathCastellano);
            TextoTraducibleRepository.writeToI18NFile(textosTraduciblesValenciano,filePathValenciano);
        }




        return null;
        
        
    }
   
    
    public String updateTextos(List<TextoTraducible> textosTraducibles,List<EntryTitle> entryTitles,Idioma idioma) {
        StringBuilder messages = new StringBuilder();        
        
        
        for (EntryTitle entryTitle:entryTitles) {
            TextoTraducible textoTraducible=getTextoTraducible(entryTitle.getTitle(), textosTraducibles);
            
            try { 
                if (textoTraducible==null) {
                    textoTraducible=createTextoTraducible(entryTitle.getTitle(),idioma);
                    textosTraducibles.add(textoTraducible);
                } else {
                    updateTextoTraducibleIfBlank(textoTraducible,idioma);
                }
            } catch (FalloTraduccionException ex) {
                messages.append(ex.getTextoCastellano()+":"+ex.getTraduccion()+" en " + entryTitle.getPath().toString() + "\n");
            }
        }        
        
        
	if (messages.length()>0) {
            return messages.toString();
        } else {
            if (idioma==Idioma.Castellano) {
                textosTraduciblesCastellano=textosTraducibles;
            } else if (idioma==Idioma.Valenciano) {
                textosTraduciblesValenciano=textosTraducibles;
            } else {
                throw new RuntimeException("Idioma desconocido:"+idioma);
            } 
            return null;
        }        
        
    }
    
    
    public String traducirVacios(List<TextoTraducible> textosTraducibles,Idioma idioma) {
        StringBuilder messages = new StringBuilder();        
        
        
        for (TextoTraducible textoTraducible:textosTraducibles) {
            try { 
                updateTextoTraducibleIfBlank(textoTraducible,idioma);
            } catch (FalloTraduccionException ex) {
                messages.append(ex.getTextoCastellano()+":"+ex.getTraduccion()+"\n");
            }
        }
        
        
	if (messages.length()>0) {
            return messages.toString();
        } else {
            if (idioma==Idioma.Castellano) {
                textosTraduciblesCastellano=textosTraducibles;
            } else if (idioma==Idioma.Valenciano) {
                textosTraduciblesValenciano=textosTraducibles;
            } else {
                throw new RuntimeException("Idioma desconocido:"+idioma);
            } 
            return null;
        }        
        
    }
    
    
    
   
    
    private TextoTraducible createTextoTraducible(String title,Idioma idioma) throws FalloTraduccionException {
        TextoTraducible textoTraducible=new TextoTraducible();
        textoTraducible.setKey(title);
        textoTraducible.setComment(null);
        textoTraducible.setContext(null);
        
        String message=getMessageTraducido(title,idioma);
        textoTraducible.setMessage(message);
        
        return textoTraducible;
    }
    
    private void updateTextoTraducibleIfBlank(TextoTraducible textoTraducible,Idioma idioma) throws FalloTraduccionException { 
        String title=textoTraducible.getKey();
        
        if ((textoTraducible.getMessage()==null) || (textoTraducible.getMessage().isBlank())) {
            String message=getMessageTraducido(title,idioma);

            textoTraducible.setMessage(message);
        }
    }    
 
    
    
    
    private String getMessageTraducido(String title,Idioma idioma) throws FalloTraduccionException {
        String message;
        
        if (idioma==Idioma.Castellano) {
            
            if ("Code".equals(title)) {
                message="Código";
            } else if ("Name".equals(title)) {
                message="Nombre";
            } else {
                message=title;
            }           

        } else if (idioma==Idioma.Valenciano) {
            if ("Code".equals(title)) {
                message="Codi";
            } else if ("Name".equals(title)) {
                message="Nom";
            } else {
                message=Traductor.traducirDesdeCastellanoAValenciano(title);
            } 
        } else {
            throw new RuntimeException("Idioma desconocido:"+idioma);
        } 
        
        return message;
    }
    
    
    private TextoTraducible getTextoTraducible(String key,List<TextoTraducible> textosTraducibles) {
        return textosTraducibles.stream()
               .filter(textoTraducible -> key.equals(textoTraducible.getKey()))
               .findFirst()
               .orElse(null);
    }
    
    
    public Path getFilePath(Path directory,Idioma idioma) {
        return directory.resolve("i18n_" + idioma.getCode() + ".csv");
    }
    
    
    
    private List<TextoTraducible> getTextosTraduciblesFromI18NFilePath(Path file) {
        List<TextoTraducible> textosTraducibles;
        if (Files.exists(file) == true) {
            textosTraducibles = TextoTraducibleRepository.readFromI18NFile(file);
        } else {
            textosTraducibles = new ArrayList<>();
        } 
        
        return textosTraducibles;
    }
    
       
    

    

    
}
