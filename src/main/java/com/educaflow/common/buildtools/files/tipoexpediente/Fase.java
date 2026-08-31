package com.educaflow.common.buildtools.files.tipoexpediente;

import com.educaflow.common.buildtools.common.TextUtil;
import com.google.common.base.CaseFormat;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.Locale;

/**
 * Una fase de un tipo de expediente: el grupo de estados cuyo {@code PhaseEventManagerImpl},
 * {@code StateEventValidatorImpl} y {@code views.xml} viven juntos en la subcarpeta
 * {@code <vN>/<fase en minúsculas>/}.
 *
 * <p>La fase <b>no es una entidad del dominio</b>: no existe ninguna tabla {@code Fase}. Es una
 * forma de agrupar ficheros para que no queden clases y vistas gigantes, y para poder copiar una
 * fase entera de un tipo de expediente a otro. De la fase sobreviven en ejecución su código, que
 * viaja en la columna {@code codePhase} del expediente, y el nombre del paquete de sus clases.
 *
 * <p>Las transiciones pueden cruzar fases con toda normalidad.
 *
 * @author logongas
 */
@XmlRootElement(name = "fase")
@XmlAccessorType(XmlAccessType.FIELD)
public class Fase {

    @XmlAttribute
    private String name;

    /** El texto que ve el usuario: acaba en el {@code namePhase} del expediente vía {@link #getTitleOrHumanizedName()}. */
    @XmlAttribute
    private String title;

    @XmlElement(name = "state")
    private List<State> states;

    @XmlTransient
    private TipoExpedienteInstanceFile tipoExpediente;

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

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    public TipoExpedienteInstanceFile getTipoExpediente() {
        return tipoExpediente;
    }

    public void setTipoExpediente(TipoExpedienteInstanceFile tipoExpediente) {
        this.tipoExpediente = tipoExpediente;
    }

    /**
     * El nombre de la subcarpeta y del paquete Java de la fase: el {@code name} en minúsculas
     * ({@code RECEPCION} → {@code recepcion}), para respetar el convenio de paquetes de Java.
     */
    public String getPackageSimpleName() {
        // Locale.ROOT a propósito: es un nombre de carpeta y de paquete Java. Bajo JVM turca una
        // fase con I daría 'ı' y el paquete generado no casaría con el que compone ExpedienteLocator.
        return name.toLowerCase(Locale.ROOT);
    }

    /** El paquete completo de las clases de la fase. */
    public String getPackageName() {
        return tipoExpediente.getBasePackageName() + "." + getPackageSimpleName();
    }

    public String getFqcnPhaseEventManager() {
        return getPackageName() + "." + tipoExpediente.getPhaseEventManagerClassName();
    }

    public String getFqcnStateEventValidator() {
        return getPackageName() + "." + tipoExpediente.getStateEventValidatorClassName();
    }

    public String getNameUpperCamelCase() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name + "");
    }

    /**
     * El texto que ve el usuario: el {@code title} del XML si lo hay, y si no el {@code name}
     * humanizado.
     */
    public String getTitleOrHumanizedName() {
        if ((title == null) || (title.isBlank())) {
            return TextUtil.humanize(name);
        }

        return title;
    }

    /**
     * Los eventos que atiende la fase: la unión, en orden de declaración y sin repetir, de los
     * eventos de sus estados. Es lo que determina qué métodos {@code trigger<Evento>} lleva el
     * {@code PhaseEventManagerImpl} de la fase, porque un evento siempre se dispara desde un estado.
     */
    public List<String> getEvents() {
        List<String> events = new java.util.ArrayList<>();

        for (State state : states) {
            for (String event : state.getEvents()) {
                if (events.contains(event) == false) {
                    events.add(event);
                }
            }
        }

        return events;
    }

    /** Los eventos de la fase en UpperCamelCase, que es como entran en los nombres de método. */
    public List<String> getEventsUpperCamelCase() {
        return TextUtil.getUpperCamelCase(getEvents());
    }

    @Override
    public String toString() {
        return this.name;
    }

}
