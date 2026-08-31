/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.tipoexpediente;

import com.educaflow.common.buildtools.common.TextUtil;
import com.educaflow.common.buildtools.createstates.ProfilesDelDominio;
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
import java.util.Locale;

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
            JAXBContext jaxbContext = JAXBContext.newInstance(TipoExpedienteInstanceFile.class, Fase.class, State.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            File xmlFile = expedienteXmlFile.toFile();
            TipoExpedienteInstanceFile tipoExpediente=(TipoExpedienteInstanceFile) unmarshaller.unmarshal(xmlFile);
            tipoExpediente.setPath(expedienteXmlFile);
            tipoExpediente.setTramitesLayout(tramitesLayout);

            //Se valida ya aquí (aunque el XML declare todos sus campos y no
            //necesite heredar nada del trámite) para que un tipo de expediente
            //huérfano o fuera del paquete raíz aborte cuanto antes.
            tramitesLayout.getTramiteInstanceDelTipo(expedienteXmlFile);

            checkFormatoConFases(tipoExpediente, expedienteXmlFile);
            enlazarFases(tipoExpediente);
            checkFases(tipoExpediente);
            checkEventos(tipoExpediente);
            checkFormatoProfiles(tipoExpediente);
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
                //El código de un estado solo es único dentro de su fase, así que para nombrarlo en
                //un mensaje hace falta la pareja.
                initialStates.add(state.getFase().getName()+"/"+state.getName());
            }
        }


        if (initialStates.isEmpty()) {
            throw new RuntimeException("No existe ningun estado inicial");
        } else if (initialStates.size()>1) {
            throw new RuntimeException("Existe más de un estado inicial:"+String.join(",", initialStates));
        }

    }

    /**
     * Las fases son obligatorias: todo tipo de expediente tiene al menos una y todo estado está
     * dentro de una. El {@code <states>} suelto en la raíz es el formato antiguo y ya no vale, así
     * que se detecta para poder decir qué hay que hacer en vez de fallar más adelante con un
     * "no hay ningún estado".
     */
    private static void checkFormatoConFases(TipoExpedienteInstanceFile tipoExpediente, Path expedienteXmlFile) {
        if ((tipoExpediente.getStatesFormatoAntiguo()!=null) && (tipoExpediente.getStatesFormatoAntiguo().isEmpty()==false)) {
            throw new RuntimeException("El " + TIPO_EXPEDIENTE_XML_NAME + " de " + expedienteXmlFile
                    + " tiene un <states> en la raíz, que es el formato antiguo. Los estados van"
                    + " agrupados en <fases>: <fases><fase name=\"...\" title=\"...\"><state .../></fase></fases>."
                    + " Además cada fase necesita su subcarpeta con PhaseEventManagerImpl, StateEventValidatorImpl y views.xml.");
        }

        if ((tipoExpediente.getFases()==null) || (tipoExpediente.getFases().isEmpty())) {
            throw new RuntimeException("El " + TIPO_EXPEDIENTE_XML_NAME + " de " + expedienteXmlFile
                    + " no declara ninguna <fase>: las fases son obligatorias y todo tipo de"
                    + " expediente tiene al menos una.");
        }
    }

    /** Enlaza cada fase con su tipo de expediente y cada estado con su fase. */
    private static void enlazarFases(TipoExpedienteInstanceFile tipoExpediente) {
        for (Fase fase : tipoExpediente.getFases()) {
            fase.setTipoExpediente(tipoExpediente);

            if ((fase.getStates()==null) || (fase.getStates().isEmpty())) {
                throw new RuntimeException("La fase '" + fase.getName() + "' no tiene ningún estado.");
            }

            for (State state : fase.getStates()) {
                state.setFase(fase);
            }
        }
    }

    /**
     * El nombre de una fase debe ser único en el tipo de expediente, y el de un estado solo tiene
     * que serlo <b>dentro de su fase</b>: lo que identifica a un estado es la pareja (fase, estado),
     * así que dos fases pueden tener un estado que se llame igual.
     */
    private static void checkFases(TipoExpedienteInstanceFile tipoExpediente) {
        Set<String> nombresFases=new HashSet<>();

        for (Fase fase : tipoExpediente.getFases()) {
            checkNombreFase(fase.getName());

            if (nombresFases.add(fase.getName())==false) {
                throw new RuntimeException("Hay más de una fase llamada '" + fase.getName() + "'.");
            }

            Set<String> nombresEstados=new HashSet<>();
            for (State state : fase.getStates()) {
                checkNombreEstado(state.getName());

                if (nombresEstados.add(state.getName())==false) {
                    throw new RuntimeException("Hay más de un estado llamado '" + state.getName()
                            + "' en la fase '" + fase.getName() + "'. El nombre de un estado solo"
                            + " tiene que ser único dentro de su fase, pero dentro de ella no puede repetirse.");
                }
            }
        }
    }

    /**
     * Reglas 9 y 10, sobre los eventos de cada estado.
     *
     * <p>9: un evento da nombre a un método {@code trigger<Evento>} de los esqueletos, así que tiene
     * que ser un identificador en UPPER_SNAKE_CASE. Hasta ahora no se validaba y un evento con un
     * guion o un espacio rompía la generación con un error críptico.
     *
     * <p>10: un evento repetido en el mismo estado lo deduplicaría en silencio el LinkedHashSet de
     * la clase generada, y el build es el único sitio donde el mensaje puede señalar el fichero.
     */
    private static void checkEventos(TipoExpedienteInstanceFile tipoExpediente) {
        for (Fase fase : tipoExpediente.getFases()) {
            for (State state : fase.getStates()) {
                Set<String> vistos = new HashSet<>();
                for (String evento : state.getEvents()) {
                    checkNombreIdentificador(evento, "evento");

                    if (vistos.add(evento) == false) {
                        throw new RuntimeException("El evento '" + evento + "' está repetido en el"
                                + " atributo events del estado '" + state.getName() + "' de la fase '"
                                + fase.getName() + "'.");
                    }
                }
            }
        }
    }

    /**
     * El perfil de un estado tiene que ser un identificador: empieza por letra y solo lleva letras
     * sin acentos, dígitos y guiones bajos. Es la misma norma que aplica
     * {@code ActionRequestHelper.getProfileName} de secretaria-virtual al {@code _profile} que manda
     * el cliente; la regex está duplicada porque este JAR no comparte código con la aplicación.
     *
     * <p>Es una comprobación de <b>formato</b>, distinta de {@link #checkProfiles}, que mira que el
     * perfil exista en el enum del dominio. Van separadas porque un perfil con un espacio o un
     * acento no es un typo de ninguna constante, y decir «no existe en el enum, los válidos son…»
     * despista más que ayuda.
     */
    private static void checkFormatoProfiles(TipoExpedienteInstanceFile tipoExpediente) {
        for (Fase fase : tipoExpediente.getFases()) {
            for (State state : fase.getStates()) {
                String profile = state.getProfile();
                if ((profile == null) || (profile.isBlank())) {
                    continue;
                }
                if (!profile.matches("[A-Za-z][A-Za-z0-9_]*")) {
                    throw new RuntimeException("El perfil '" + profile + "' del estado '"
                            + state.getName() + "' de la fase '" + fase.getName() + "' no es válido:"
                            + " debe empezar por letra y llevar solo letras sin acentos, dígitos y"
                            + " guiones bajos, porque de él sale la referencia Profile." + profile
                            + " de la clase States generada.");
                }
            }
        }
    }

    /**
     * Regla 11: el perfil de un estado tiene que ser uno del enum global {@code Profile} del
     * dominio, porque la clase {@code States} generada emite una referencia {@code Profile.<PERFIL>}.
     *
     * <p>Vive aquí, con las demás validaciones del XML, pero la llama {@code StatesFile}: es la
     * única validación que necesita un dato de fuera del propio {@code TipoExpedienteInstance.xml}.
     */
    public static void checkProfiles(TipoExpedienteInstanceFile tipoExpediente, ProfilesDelDominio profiles) {
        for (Fase fase : tipoExpediente.getFases()) {
            for (State state : fase.getStates()) {
                String profile = state.getProfile();
                if ((profile == null) || (profile.isBlank())) {
                    continue;
                }
                if (profiles.contiene(profile) == false) {
                    // El tipo de expediente va en el propio mensaje porque checkProfiles se invoca
                    // desde StatesFile, fuera del wrapper del finder que añade la ruta a las demás
                    // validaciones. Sin él, un estado como ENTRADA_DATOS de la fase RECEPCION —que
                    // existe literalmente en todos los tipos— no diría cuál hay que tocar.
                    throw new RuntimeException("El tipo de expediente " + tipoExpediente.getCode()
                            + " (" + tipoExpediente.getPath() + ") declara el perfil '" + profile
                            + "' en el estado '" + state.getName() + "' de la fase '" + fase.getName()
                            + "', que no existe en el enum Profile de " + profiles.getFichero()
                            + ". Los perfiles válidos son: " + profiles.getNombres() + ".");
                }
            }
        }
    }

    /** El nombre de una fase, que solo tiene que ser un identificador en UPPER_SNAKE_CASE. */
    public static void checkNombreFase(String fase) {
        checkNombreIdentificador(fase, "fase");
    }

    /** El nombre de un estado, que solo tiene que ser un identificador en UPPER_SNAKE_CASE. */
    public static void checkNombreEstado(String estado) {
        checkNombreIdentificador(estado, "estado");
    }

    /**
     * Todo nombre del {@code TipoExpedienteInstance.xml} que acabe siendo un identificador Java (el
     * de una fase, el de un estado y el de un evento) tiene que estar en UPPER_SNAKE_CASE: son los
     * nombres de las constantes de la clase {@code States} generada y los trozos de los nombres de
     * método de los esqueletos.
     */
    private static void checkNombreIdentificador(String nombre, String queEs) {
        if ((nombre == null) || (nombre.isBlank())) {
            throw new RuntimeException("El nombre de " + queEs + " no puede estar vacío.");
        }
        if (!nombre.matches("[A-Z][A-Z0-9]*(_[A-Z0-9]+)*")) {
            throw new RuntimeException("El nombre de " + queEs + " '" + nombre + "' no es válido:"
                    + " debe estar en UPPER_SNAKE_CASE (empezar por letra mayúscula y llevar solo"
                    + " mayúsculas, dígitos y guiones bajos que separen segmentos no vacíos).");
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

        // Locale.ROOT a propósito: de aquí salen las CONSTANTES del enum TipoDocumentoPdf, así que
        // sin él el proyecto no compilaría bajo una JVM en locale turco.
        return withUnderscores.toUpperCase(Locale.ROOT);
    }    
      
}
