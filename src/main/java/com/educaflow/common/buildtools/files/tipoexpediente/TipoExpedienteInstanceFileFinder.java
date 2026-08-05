/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.tipoexpediente;

import com.educaflow.common.buildtools.common.TextUtil;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 * @author logongas
 */
public class TipoExpedienteInstanceFileFinder {

    static final public String TIPO_EXPEDIENTE_XML_NAME="TipoExpedienteInstance.xml";

    private final TramitesLayout tramitesLayout;

    public TipoExpedienteInstanceFileFinder(TramitesLayout tramitesLayout) {
        this.tramitesLayout = tramitesLayout;
    }

    public List<TipoExpedienteInstanceFile> findTiposExpedienteFile() {
        List<TipoExpedienteInstanceFile> tipoExpedienteInstanceFiles=new ArrayList<>();

        Path origen=tramitesLayout.getOrigen();
        if (!Files.exists(origen) || !Files.isDirectory(origen)) {
            throw new RuntimeException("El directorio no existe o no es un directorio:"+origen);
        }

        if (tramitesLayout.existeRaiz()==false) {
            System.out.println("No existe "+tramitesLayout.getRootPackagePath()+"; no hay ningún trámite ni tipo de expediente");
            return tipoExpedienteInstanceFiles;
        }

        tramitesLayout.checkTramitesNoAnidados();

        List<Path> expedienteXmlFiles = findTiposExpedienteXmlFiles();
        for (Path expedienteXmlFile : expedienteXmlFiles) {
            try {
                TipoExpedienteInstanceFile tipoExpedienteInstanceFile=parseTipoExpedienteXml(expedienteXmlFile);
                tipoExpedienteInstanceFiles.add(tipoExpedienteInstanceFile);
            } catch (Exception ex) {
                throw new RuntimeException("Fallo al obtener el tipo de expediente:"+expedienteXmlFile,ex);
            }

        }

        checkCodesNoDuplicados(tipoExpedienteInstanceFiles);

        return tipoExpedienteInstanceFiles;
    }



    public TipoExpedienteInstanceFile parseTipoExpedienteXml(Path expedienteXmlFile) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TipoExpedienteInstanceFile.class, State.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            File xmlFile = expedienteXmlFile.toFile();
            TipoExpedienteInstanceFile tipoExpediente=(TipoExpedienteInstanceFile) unmarshaller.unmarshal(xmlFile);
            tipoExpediente.setPath(expedienteXmlFile);
            tipoExpediente.setTramitesLayout(tramitesLayout);

            //Se valida ya aquí (aunque el XML declare todos sus campos y no
            //necesite heredar nada del trámite) para que un tipo de expediente
            //huérfano o fuera del paquete raíz aborte cuanto antes.
            tramitesLayout.getTramiteInstanceDelTipo(expedienteXmlFile);

            checkOnlyOneInitialState(tipoExpediente);


            List<TipoDocumentoPdf> tipoDocumentosPdfExpecificos=getDocumentosPdf(expedienteXmlFile.getParent());

            List<TipoDocumentoPdf> tipoDocumentosPdfShared=getDocumentosPdf(tramitesLayout.getSharedPath());

            List<TipoDocumentoPdf> tipoDocumentosPdf = new ArrayList<>(tipoDocumentosPdfExpecificos);
            tipoDocumentosPdf.addAll(tipoDocumentosPdfShared);

            tipoExpediente.setTipoDocumentosPdf(tipoDocumentosPdf);

            return tipoExpediente;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Path> findTiposExpedienteXmlFiles() {
        Path rootPath=tramitesLayout.getRootPackagePath();

        try {

            if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
                throw new RuntimeException("El directorio no existe o no es un directorio:"+rootPath);
            }

            try (Stream<Path> walk = Files.walk(rootPath)) {
                return walk
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(TIPO_EXPEDIENTE_XML_NAME))
                        .collect(Collectors.toList());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Al admitir tipos de expediente a cualquier profundidad, dos carpetas con
     * el mismo nombre bajo el mismo trámite (grupoA/v1 y grupoB/v1) derivarían
     * el mismo code y el mismo name. Como el code identifica al tipo de
     * expediente, eso es siempre un error.
     */
    private static void checkCodesNoDuplicados(List<TipoExpedienteInstanceFile> tiposExpedientes) {
        Map<String,List<Path>> pathsPorCode=new LinkedHashMap<>();

        for(TipoExpedienteInstanceFile tipoExpediente:tiposExpedientes) {
            pathsPorCode.computeIfAbsent(tipoExpediente.getCode(), code -> new ArrayList<>()).add(tipoExpediente.getPath());
        }

        StringBuilder messages=new StringBuilder();
        for(Map.Entry<String,List<Path>> entry:pathsPorCode.entrySet()) {
            if (entry.getValue().size()>1) {
                messages.append("El code '"+entry.getKey()+"' lo tienen a la vez estos tipos de expediente:"
                        +entry.getValue().stream().map(Path::toString).collect(Collectors.joining(", "))+"\n");
            }
        }

        if (messages.length()>0) {
            throw new RuntimeException("Hay tipos de expediente con el mismo code:\n"+messages.toString());
        }
    }

    private static void checkOnlyOneInitialState(TipoExpedienteInstanceFile tipoExpediente) {
        List<String> initialStates=new ArrayList<>();
        
        for(State state:tipoExpediente.getStates()) {
            if (state.isInitial()==true) {
                initialStates.add(state.getName());
            }
        }
        
        
        if (initialStates.isEmpty()) {
            throw new RuntimeException("No existe ningun estado inicial");
        } else if (initialStates.size()>1) {
            throw new RuntimeException("Existe más de un estado inicial:"+String.join(",", initialStates));
        }
        
    }

    private static List<TipoDocumentoPdf> getDocumentosPdf(Path carpetaBuscar) {
        try {
           
            Path directorioDocumentosPdf=carpetaBuscar.resolve("documentospdf");
            
            List<TipoDocumentoPdf> lista = new ArrayList<>();



            if (!Files.isDirectory(directorioDocumentosPdf)) {
                System.out.println("No existen documentos pdf");
                return lista; 
            }

            Set<String> nombresBase = new HashSet<>();

            // 3. Buscar solo ficheros .pdf en esa carpeta
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directorioDocumentosPdf, "*.pdf")) {
                for (Path entry : stream) {
                    if (Files.isRegularFile(entry)) {
                        String nombre = entry.getFileName().toString();
                        String enumValue = nombre.substring(0, nombre.length() - 4);


                        String packageDocumentosPdf=TextUtil.getSubstringBetween(directorioDocumentosPdf.toString(),"java","documentospdf");

                        String filePathName="" + packageDocumentosPdf + "documentospdf/" + nombre;

                        TipoDocumentoPdf tipoDocumentoPdf=new TipoDocumentoPdf(toUpperSnakeCase(enumValue),filePathName);

                        lista.add(tipoDocumentoPdf);
                        nombresBase.add(enumValue);
                        System.out.println("Documento PDF:"+tipoDocumentoPdf.getFileName()+"-->"+tipoDocumentoPdf.getEnumValue());
                    }
                }
            }

            // 4. Buscar los .xml de definición de documentos: su .pdf se genera en
            // tiempo de compilación (tarea generatePdfDocuments) y puede no estar
            // versionado, pero el TipoDocumentoPdf debe existir igualmente.
            // Cuentan los que tienen raíz <documento> y no empiezan por "_"
            // (los _*.xml son fragmentos incluidos desde otros documentos).
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directorioDocumentosPdf, "*.xml")) {
                for (Path entry : stream) {
                    if (!Files.isRegularFile(entry)) {
                        continue;
                    }
                    String nombre = entry.getFileName().toString();
                    if (nombre.startsWith("_")) {
                        continue;
                    }
                    String nombreBase = nombre.substring(0, nombre.length() - 4);
                    if (!isDocumento(entry)) {
                        continue;
                    }
                    if (nombresBase.contains(nombreBase)) {
                        throw new RuntimeException("Existen a la vez " + nombreBase + ".pdf y "
                                + nombre + " (con raíz <documento>) en " + directorioDocumentosPdf
                                + ": no se sabría si usar el " + nombreBase + ".pdf existente o el que"
                                + " generaría el " + nombre + ". Borra uno de los dos.");
                    }

                    String packageDocumentosPdf=TextUtil.getSubstringBetween(directorioDocumentosPdf.toString(),"java","documentospdf");

                    String filePathName="" + packageDocumentosPdf + "documentospdf/" + nombreBase + ".pdf";

                    TipoDocumentoPdf tipoDocumentoPdf=new TipoDocumentoPdf(toUpperSnakeCase(nombreBase),filePathName);

                    lista.add(tipoDocumentoPdf);
                    nombresBase.add(nombreBase);
                    System.out.println("Documento PDF (desde XML):"+tipoDocumentoPdf.getFileName()+"-->"+tipoDocumentoPdf.getEnumValue());
                }
            }

            return lista;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static boolean isDocumento(Path xml) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(xml.toFile()).getDocumentElement().getTagName().equals("documento");
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al parsear el XML: " + xml, ex);
        }
    }

    public static String toUpperSnakeCase(String s) {
        String withUnderscores = s.replaceAll("(?<!^)(?=[A-Z])", "_");

        return withUnderscores.toUpperCase();
    }    
      
}
