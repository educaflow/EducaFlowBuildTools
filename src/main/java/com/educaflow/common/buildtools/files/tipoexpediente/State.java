/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.tipoexpediente;

import com.educaflow.common.buildtools.common.TextUtil;
import com.google.common.base.CaseFormat;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "state")
@XmlAccessorType(XmlAccessType.FIELD)
public class State {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String title;    
    
    @XmlAttribute
    private String profile;

    @XmlAttribute
    private boolean initial;

    @XmlAttribute
    private boolean closed;

    @XmlAttribute
    @XmlJavaTypeAdapter(CommaSeparatedAdapter.class)
    private List<String> events;
    

    // getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }    

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public boolean isInitial() {
        return initial;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public List<String> getEvents() {
        return events;
    }
    
    public List<String> getEventsUpperCamelCase() {
        return TextUtil.getUpperCamelCase(events);
    }    

    public String getNameUpperCamelCase() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name + "");
    }
    
    public void setEvents(List<String> events) {
        this.events = events;
    }
    
    
    @Override
    public String toString() {
        return this.name;
    }
}
