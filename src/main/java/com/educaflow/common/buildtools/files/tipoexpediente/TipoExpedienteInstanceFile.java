/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.tipoexpediente;

import com.educaflow.common.buildtools.files.tramite.TramiteInstanceFile;
import com.educaflow.common.buildtools.files.tramite.TramiteInstanceFileFinder;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@XmlRootElement(name = "TipoExpediente")
@XmlAccessorType(XmlAccessType.FIELD)
public class TipoExpedienteInstanceFile {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "code")
    private String code;

    @XmlElement(name = "tramite")
    private String tramite;

    @XmlElement(name = "ambitoCreador")
    private String ambitoCreador;

    @XmlElement(name = "ambitoResponsable")
    private String ambitoResponsable;  
    
    @XmlElement(name = "ambitoAuditor")
    private String ambitoAuditor;     
    
    @XmlTransient
    private Path path;

    @XmlTransient
    private TramitesLayout tramitesLayout;

    @XmlTransient
    private TramiteInstanceFile tramiteInstanceParent;

    @XmlTransient
    private List<String> events;

    @XmlTransient
    private List<String> profiles;

    @XmlElementWrapper(name = "fases")
    @XmlElement(name = "fase")
    private List<Fase> fases;

    /**
     * Solo para detectar el formato antiguo. Un {@code <states>} en la raíz ya no es válido: los
     * estados van dentro de su {@code <fase>}. Se deserializa para poder dar un error explícito en
     * vez de un críptico "no hay ningún estado".
     */
    @XmlElementWrapper(name = "states")
    @XmlElement(name = "state")
    private List<State> statesFormatoAntiguo;

    // No viene del XML: lo rellena el finder escaneando la carpeta documentospdf del tipo y la
    // compartida. Sin @XmlTransient, TipoDocumentoPdf entra en el JAXBContext y hay implementaciones
    // de JAXB (EclipseLink MOXy, que es la que gana en el classpath de test de secretaria-virtual)
    // que entonces exigen que tenga constructor sin argumentos y abortan el parseo.
    @XmlTransient
    private List<TipoDocumentoPdf> tipoDocumentosPdf;
        
    
    // getters y setters
    public String getName() {
        if ((name == null) || (name.isBlank())) {
            return getTramiteInstanceParent().getName() + " " + getVersion();
        } else {
            return name;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        if ((code == null) || (code.isBlank())) {
            return getTramiteInstanceParent().getCode() + getVersion();
        } else {
            return code;
        }
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * La versión del tipo de expediente derivada del nombre de su carpeta: "v1" → "V1"
     */
    private String getVersion() {
        // Locale.ROOT a propósito: la versión entra en el code del tipo y en los nombres de vista.
        return path.getParent().getFileName().toString().toUpperCase(Locale.ROOT);
    }

    /**
     * El trámite al que pertenece el tipo de expediente: el del
     * TramiteInstance.xml que hay en la carpeta del propio tipo o en cualquiera
     * de sus carpetas padre hasta el paquete raíz de los trámites.
     */
    private TramiteInstanceFile getTramiteInstanceParent() {
        if (tramiteInstanceParent == null) {
            Path tramiteInstanceXmlFile = tramitesLayout.getTramiteInstanceDelTipo(path);

            tramiteInstanceParent = new TramiteInstanceFileFinder(tramitesLayout).parse(tramiteInstanceXmlFile);
        }

        return tramiteInstanceParent;
    }

    public void setTramitesLayout(TramitesLayout tramitesLayout) {
        this.tramitesLayout = tramitesLayout;
    }

    /**
     * Las fases del tipo de expediente, en orden de declaración. Siempre hay al menos una: las
     * fases son obligatorias y todo estado pertenece a exactamente una.
     */
    public List<Fase> getFases() {
        return fases;
    }

    public void setFases(List<Fase> fases) {
        this.fases = fases;
    }

    /** La fase que se llama así, o null si no existe. */
    public Fase getFase(String name) {
        for (Fase fase : fases) {
            if (fase.getName().equals(name)) {
                return fase;
            }
        }

        return null;
    }

    /**
     * <b>Todos</b> los estados del tipo de expediente, de todas las fases y en orden de
     * declaración. Es lo que necesitan el enum {@code State} (que según el diseño de las fases es
     * idéntico en las clases de todas ellas), el data-init y el i18n.
     */
    public List<State> getStates() {
        List<State> todos = new ArrayList<>();

        for (Fase fase : fases) {
            todos.addAll(fase.getStates());
        }

        return todos;
    }

    /** Solo para el chequeo de formato antiguo del finder. */
    List<State> getStatesFormatoAntiguo() {
        return statesFormatoAntiguo;
    }

    /** El estado inicial del tipo de expediente. El finder garantiza que hay exactamente uno. */
    public State getInitialState() {
        for (State state : getStates()) {
            if (state.isInitial()) {
                return state;
            }
        }

        return null;
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(Path path) {
        this.path = path;
    }

    private List<String> getEventsFromStates(List<State> states) {
        List<String> events = new ArrayList<>();

        for (State state : states) {
            for (String event : state.getEvents()) {
                if (events.contains(event) == false) {
                    events.add(event);
                }
            }
        }

        return events;

    }

    private List<String> getProfilesFromStates(List<State> states) {
        List<String> profiles = new ArrayList<>();

        for (State state : states) {
            if ((state.getProfile() != null) && (state.getProfile().trim().isEmpty() == false)) {
                if (profiles.contains(state.getProfile()) == false) {
                    profiles.add(state.getProfile());
                }
            }
        }

        return profiles;

    }

    /**
     * Todos los eventos del tipo de expediente, de todas las fases. Es lo que lleva el enum
     * {@code Event}, que igual que {@code State} es idéntico en las clases de todas las fases.
     * Los eventos que atiende una fase en concreto son {@link Fase#getEvents()}.
     */
    public List<String> getEvents() {
        return getEventsFromStates(getStates());
    }

    /**
     * @param events the events to set
     */
    public void setEvents(List<String> events) {
        this.events = events;
    }

    /**
     * @return the profiles
     */
    public List<String> getProfiles() {
        return getProfilesFromStates(getStates());
    }

    /**
     * @param profiles the profiles to set
     */
    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    /**
     * @return the tramite
     */
    public String getTramite() {
        if ((tramite == null) || (tramite.isBlank())) {
            return getTramiteInstanceParent().getCode();
        } else {
            return tramite;
        }
    }

    /**
     * @param tramite the tramite to set
     */
    public void setTramite(String tramite) {
        this.tramite = tramite;
    }


    /**
     * @return the ambitoCreador
     */
    public String getAmbitoCreador() {
        return ambitoCreador;
    }

    /**
     * @param ambitoCreador the ambitoCreador to set
     */
    public void setAmbitoCreador(String ambitoCreador) {
        this.ambitoCreador = ambitoCreador;
    }

    /**
     * @return the ambitoResponsable
     */
    public String getAmbitoResponsable() {
        return ambitoResponsable;
    }

    /**
     * @param ambitoResponsable the ambitoResponsable to set
     */
    public void setAmbitoResponsable(String ambitoResponsable) {
        this.ambitoResponsable = ambitoResponsable;
    }
    
    /**
     * @return the ambitoAuditor
     */
    public String getAmbitoAuditor() {
        return ambitoAuditor;
    }

    /**
     * @param ambitoAuditor the ambitoAuditor to set
     */
    public void setAmbitoAuditor(String ambitoAuditor) {
        this.ambitoAuditor = ambitoAuditor;
    }    
   
    public void setTipoDocumentosPdf(List<TipoDocumentoPdf> tipoDocumentosPdf) {
        this.tipoDocumentosPdf=tipoDocumentosPdf;
    }    
    
    public List<TipoDocumentoPdf> getTipoDocumentosPdf() {
        return tipoDocumentosPdf;
    }
    
    /**
     * El paquete de la carpeta de versión del tipo de expediente, del que cuelgan los paquetes de
     * sus fases. Es el único dato que se guarda en la base de datos para poder localizar en runtime
     * el {@code PhaseEventManager} y el {@code StateEventValidator} de un estado: con la fase que sale
     * del propio {@code codeState} basta para componer el FQCN, y el data-init lo reescribe en cada
     * arranque, así que mover la carpeta del tipo se corrige solo.
     */
    public String getBasePackageName() {
        return getPackageName();
    }

    private String getPackageName() {
        Path filePath = path.getParent();
        String pathString = filePath.toString();

        pathString = pathString.replace("\\", "/");

        int javaIndex = pathString.indexOf("/java/");
        if (javaIndex == -1) {
            if (pathString.endsWith("/java")) {
                javaIndex = pathString.length() - "/java".length();
            } else {
                return "";
            }
        }

        String packagePath = pathString.substring(javaIndex + "/java/".length());

        int dotIndex = packagePath.lastIndexOf(".");
        if (dotIndex != -1) {
            packagePath = packagePath.substring(0, dotIndex);
        }

        return packagePath.replace("/", ".");
    }

    /**
     * FQCN de la entidad del tipo de expediente: la subclase de {@code Expediente} que su
     * {@code domains.xml} declara, siempre con el nombre del code del tipo y en el paquete del
     * módulo de expedientes. Lo necesitan tanto el código generado (que la importa) como el
     * data-init de permisos (que la nombra como objeto del permiso).
     */
    public String getFqcnExpediente() {
        return "com.educaflow.subsystem.expedientes.db." + getCode();
    }

    /**
     * El nombre de la clase del {@code PhaseEventManager}, igual en todas las fases: lo que las
     * distingue es el paquete, no el nombre.
     */
    public String getPhaseEventManagerClassName() {
        return "PhaseEventManagerImpl";
    }

    /** El nombre de la clase del {@code StateEventValidator}, igual en todas las fases. */
    public String getStateEventValidatorClassName() {
        return "StateEventValidatorImpl";
    }

    /**
     * El nombre de la clase del {@code InitialEventManager}, que a diferencia de las dos anteriores
     * es <b>una sola por tipo de expediente</b> y vive en la raíz de la versión: el evento inicial
     * se dispara cuando todavía no hay estado del que partir, así que no es de ninguna fase.
     */
    public String getInitialEventManagerClassName() {
        return "InitialEventManagerImpl";
    }

    /** FQCN del {@code InitialEventManager} del tipo: cuelga del paquete base, no del de una fase. */
    public String getFqcnInitialEventManager() {
        return getBasePackageName() + "." + getInitialEventManagerClassName();
    }

    public static String getSimpleClassName(String fqcn) {
        return fqcn.substring(fqcn.lastIndexOf('.') + 1);
    }
}
