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

@XmlRootElement(name = "TipoExpediente")
@XmlAccessorType(XmlAccessType.FIELD)
public class TipoExpedienteInstanceFile {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "code")
    private String code;
    
    @XmlTransient
    private Path path;
    
    @XmlTransient
    private List<String> events;  
    
    @XmlTransient
    private List<String> profiles;  
    
    
    @XmlElementWrapper(name = "states")
    @XmlElement(name = "state")
    private List<State> states;

    // getters y setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public List<State> getStates() { return states; }
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
        List<String> events=new ArrayList<>();
        
        for(State state:states) {
            for(String event:state.getEvents()) {
                if (events.contains(event)==false) {
                    events.add(event);
                }
            }
        }
        
        
        return events;
        
    }
    
    
    private List<String> getProfilesFromStates(List<State> states) {
        List<String> profiles=new ArrayList<>();
        
        for(State state:states) {
            if ((state.getProfile()!=null) && (state.getProfile().trim().isEmpty()==false)) {
                if (profiles.contains(state.getProfile())==false) {
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
    
}
