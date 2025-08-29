/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.tipoexpediente;

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
    private List<String> events;

    @XmlTransient
    private List<String> profiles;

    @XmlElementWrapper(name = "states")
    @XmlElement(name = "state")
    private List<State> states;

    private List<TipoDocumentoPdf> tipoDocumentosPdf;
        
    
    // getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
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
     * @return the events
     */
    public List<String> getEvents() {
        return getEventsFromStates(states);
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
        return getProfilesFromStates(states);
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
        return tramite;
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
    
    public String getPackageName() {
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

}
