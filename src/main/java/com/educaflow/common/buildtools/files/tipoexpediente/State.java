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
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "state")
@XmlAccessorType(XmlAccessType.FIELD)
public class State {

    /**
     * El nombre del estado, el que se escribe en el XML. Solo tiene que ser único <b>dentro de su
     * fase</b>: la identidad de un estado es la pareja (fase, estado), y por eso lo que se persiste
     * son las dos columnas {@code codePhase} y {@code codeState}.
     */
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

    /** La fase a la que pertenece el estado. La enlaza el finder tras deserializar. */
    @XmlTransient
    private Fase fase;


    // getters y setters
    public String getName() {
        return name;
    }

    public Fase getFase() {
        return fase;
    }

    public void setFase(Fase fase) {
        this.fase = fase;
    }

    /**
     * El código del estado: el que se persiste en {@code Expediente.codeState}, el que nombra la
     * constante del enum de su fase en la clase {@code States} generada y el que entra como
     * segmento en el nombre de las vistas. Es el nombre tal cual está en el XML; lo que lo hace
     * único en todo el tipo de expediente es ir siempre acompañado del código de la fase.
     */
    public String getCodeState() {
        return name;
    }

    /**
     * El texto que ve el usuario: el {@code title} del XML si lo hay, y si no el {@code name}
     * humanizado. Es lo que acaba en el {@code nameState} del expediente.
     */
    public String getTitleOrHumanizedName() {
        if ((title == null) || (title.isBlank())) {
            return TextUtil.humanize(name);
        }

        return title;
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

    /**
     * Los eventos disparables desde el estado, o una lista <b>vacía</b> si el atributo {@code events}
     * no está en el XML.
     *
     * <p>Un estado sin eventos es legítimo —un estado final lo es—, así que omitir el atributo
     * equivale a {@code events=""} y no es un error de fail-fast. Se normaliza <b>aquí, en el origen</b>,
     * y no con guardias en cada consumidor: {@link #getEventsUpperCamelCase()} se invoca sin guardia
     * desde los tests de secretaria-virtual, y ahí un {@code null} rompe el build; en
     * {@code TipoExpedienteInstanceFile}, en cambio, el NPE queda capturado por la tarea y solo hace
     * que el {@code <extra-code-model>} no se inyecte, en silencio.
     *
     * <p>Si alguna vez se quisiera prohibirlo, el sitio sería {@code checkEventos} del finder, cuyos
     * mensajes nombran el fichero.
     */
    public List<String> getEvents() {
        return (events != null) ? events : Collections.emptyList();
    }

    public List<String> getEventsUpperCamelCase() {
        return TextUtil.getUpperCamelCase(getEvents());
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
