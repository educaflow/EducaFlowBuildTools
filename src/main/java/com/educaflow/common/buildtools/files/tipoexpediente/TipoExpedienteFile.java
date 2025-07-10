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
import java.util.List;

@XmlRootElement(name = "tipoExpediente")
@XmlAccessorType(XmlAccessType.FIELD)
public class TipoExpedienteFile {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "code")
    private String code;

    @XmlElementWrapper(name = "states")
    @XmlElement(name = "state")
    private List<State> states;

    @XmlElementWrapper(name = "events")
    @XmlElement(name = "event")
    private List<Event> events;

    @XmlElementWrapper(name = "profiles")
    @XmlElement(name = "profile")
    private List<Profile> profiles;
    
    @XmlTransient
    private Path path;

    // Getters y Setters
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

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }
    
    
    

    @Override
    public String toString() {
        return "TipoExpediente{" +
               "name='" + name + '\'' +
               ", code='" + code + '\'' +
               ", states=" + states +
               ", events=" + events +
               ", profiles=" + profiles +
               '}';
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
}
