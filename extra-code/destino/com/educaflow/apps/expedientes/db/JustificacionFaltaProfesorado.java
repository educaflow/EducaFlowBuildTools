package com.educaflow.apps.expedientes.db;

import com.axelor.db.EntityHelper;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaFile;
import java.time.LocalTime;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(
  name = "EXPEDIENTES_JUSTIFICACION_FALTA_PROFESORADO",
  indexes = {
    @Index(
      columnList = "justificante"
    ),
    @Index(
      columnList = "documento_completo_sin_firmar"
    ),
    @Index(
      columnList = "documento_completo_firmado_usuario"
    ),
    @Index(
      columnList = "documento_completo_firmado_usuario_director"
    )
  }
)
public class JustificacionFaltaProfesorado extends Expediente {

  private String apellidos;

  private String nombre;

  private String dni;

  private String dias;

  @Basic
  @Type(
    type = "com.axelor.db.hibernate.type.ValueEnumType"
  )
  private MesJustificacionFaltaProfesorado mes;

  private Integer anyo = 0;

  @Basic
  @Type(
    type = "com.axelor.db.hibernate.type.ValueEnumType"
  )
  private TipoJornadaFaltaJustificacionFaltaProfesorado tipoJornadaFalta;

  private LocalTime horaInicio;

  private LocalTime horaFin;

  @Basic
  @Type(
    type = "com.axelor.db.hibernate.type.ValueEnumType"
  )
  private MotivoFaltaJustificacionFaltaProfesorado motivoFalta;

  private String otroMotivo;

  private String disconformidad;

  private String resolucion;

  @Basic
  @Type(
    type = "com.axelor.db.hibernate.type.ValueEnumType"
  )
  private TipoResolucionJustificacionFaltaProfesorado tipoResolucion;

  @Widget(
    title = "Foto o PDF del justificante"
  )
  @OneToOne(
    fetch = FetchType.LAZY,
    cascade = {
      CascadeType.PERSIST,
      CascadeType.MERGE
    }
  )
  private MetaFile justificante;

  @Widget(
    title = "Documento a firmar por el usuario"
  )
  @OneToOne(
    fetch = FetchType.LAZY,
    cascade = {
      CascadeType.PERSIST,
      CascadeType.MERGE
    }
  )
  private MetaFile documentoCompletoSinFirmar;

  @Widget(
    title = "Documento "
  )
  @OneToOne(
    fetch = FetchType.LAZY,
    cascade = {
      CascadeType.PERSIST,
      CascadeType.MERGE
    }
  )
  private MetaFile documentoCompletoFirmadoUsuario;

  @Widget(
    title = "PDF file"
  )
  @OneToOne(
    fetch = FetchType.LAZY,
    cascade = {
      CascadeType.PERSIST,
      CascadeType.MERGE
    }
  )
  private MetaFile documentoCompletoFirmadoUsuarioDirector;

  @Widget(
    title = "Attributes"
  )
  @Basic(
    fetch = FetchType.LAZY
  )
  @Type(
    type = "json"
  )
  private String attrs;

  public JustificacionFaltaProfesorado() {
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDni() {
    return dni;
  }

  public void setDni(String dni) {
    this.dni = dni;
  }

  public String getDias() {
    return dias;
  }

  public void setDias(String dias) {
    this.dias = dias;
  }

  public MesJustificacionFaltaProfesorado getMes() {
    return mes;
  }

  public void setMes(MesJustificacionFaltaProfesorado mes) {
    this.mes = mes;
  }

  public Integer getAnyo() {
    return anyo == null ? 0 : anyo;
  }

  public void setAnyo(Integer anyo) {
    this.anyo = anyo;
  }

  public TipoJornadaFaltaJustificacionFaltaProfesorado getTipoJornadaFalta() {
    return tipoJornadaFalta;
  }

  public void setTipoJornadaFalta(TipoJornadaFaltaJustificacionFaltaProfesorado tipoJornadaFalta) {
    this.tipoJornadaFalta = tipoJornadaFalta;
  }

  public LocalTime getHoraInicio() {
    return horaInicio;
  }

  public void setHoraInicio(LocalTime horaInicio) {
    this.horaInicio = horaInicio;
  }

  public LocalTime getHoraFin() {
    return horaFin;
  }

  public void setHoraFin(LocalTime horaFin) {
    this.horaFin = horaFin;
  }

  public MotivoFaltaJustificacionFaltaProfesorado getMotivoFalta() {
    return motivoFalta;
  }

  public void setMotivoFalta(MotivoFaltaJustificacionFaltaProfesorado motivoFalta) {
    this.motivoFalta = motivoFalta;
  }

  public String getOtroMotivo() {
    return otroMotivo;
  }

  public void setOtroMotivo(String otroMotivo) {
    this.otroMotivo = otroMotivo;
  }

  public String getDisconformidad() {
    return disconformidad;
  }

  public void setDisconformidad(String disconformidad) {
    this.disconformidad = disconformidad;
  }

  public String getResolucion() {
    return resolucion;
  }

  public void setResolucion(String resolucion) {
    this.resolucion = resolucion;
  }

  public TipoResolucionJustificacionFaltaProfesorado getTipoResolucion() {
    return tipoResolucion;
  }

  public void setTipoResolucion(TipoResolucionJustificacionFaltaProfesorado tipoResolucion) {
    this.tipoResolucion = tipoResolucion;
  }

  public MetaFile getJustificante() {
    return justificante;
  }

  public void setJustificante(MetaFile justificante) {
    this.justificante = justificante;
  }

  public MetaFile getDocumentoCompletoSinFirmar() {
    return documentoCompletoSinFirmar;
  }

  public void setDocumentoCompletoSinFirmar(MetaFile documentoCompletoSinFirmar) {
    this.documentoCompletoSinFirmar = documentoCompletoSinFirmar;
  }

  public MetaFile getDocumentoCompletoFirmadoUsuario() {
    return documentoCompletoFirmadoUsuario;
  }

  public void setDocumentoCompletoFirmadoUsuario(MetaFile documentoCompletoFirmadoUsuario) {
    this.documentoCompletoFirmadoUsuario = documentoCompletoFirmadoUsuario;
  }

  public MetaFile getDocumentoCompletoFirmadoUsuarioDirector() {
    return documentoCompletoFirmadoUsuarioDirector;
  }

  public void setDocumentoCompletoFirmadoUsuarioDirector(MetaFile documentoCompletoFirmadoUsuarioDirector) {
    this.documentoCompletoFirmadoUsuarioDirector = documentoCompletoFirmadoUsuarioDirector;
  }

  public String getAttrs() {
    return attrs;
  }

  public void setAttrs(String attrs) {
    this.attrs = attrs;
  }

  @Override
  public boolean equals(Object obj) {
    return EntityHelper.equals(this, obj);
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public String toString() {
    return EntityHelper.toString(this);
  }



    //Estados del expediente
    public enum Estado {

		ENTRADA_DATOS(

Profile.CREADOR,true,false

)

,


		FIRMA_POR_USUARIO(

Profile.CREADOR,false,false

)

,


		REVISION_Y_FIRMA_POR_RESPONSABLE(

Profile.RESPONSABLE,false,false

)

,


		ACEPTADO(

Profile.RESPONSABLE,false,true

)

,


		RECHAZADO(

Profile.RESPONSABLE,false,true

)




    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los eventos
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Eventos del expediente
    public enum Evento {

    }

    //Perfiles del expediente
    public enum Profile {

    }


    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

    //Estados del expediente
    public enum Estado {

		ENTRADA_DATOS(Profile.CREADOR,true,false),


		FIRMA_POR_USUARIO(Profile.CREADOR,false,false),


		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false),


		ACEPTADO(Profile.RESPONSABLE,false,true),


		RECHAZADO(Profile.RESPONSABLE,false,true)


    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los eventos
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Eventos del expediente
    public enum Evento {

    }

    //Perfiles del expediente
    public enum Profile {

    }


    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

    //Estados del expediente
    public enum Estado {

		ENTRADA_DATOS(Profile.CREADOR,true,),


		FIRMA_POR_USUARIO(Profile.CREADOR,false,),


		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,),


		ACEPTADO(Profile.RESPONSABLE,false,),


		RECHAZADO(Profile.RESPONSABLE,false,)


    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los eventos
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Eventos del expediente
    public enum Evento {

    }

    //Perfiles del expediente
    public enum Profile {

    }


    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

    //Estados del expediente
    public enum Estado {

		ENTRADA_DATOS(Profile.CREADOR,true,),


		FIRMA_POR_USUARIO(Profile.CREADOR,false,),


		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,),


		ACEPTADO(Profile.RESPONSABLE,false,),


		RECHAZADO(Profile.RESPONSABLE,false,)


    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los eventos
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Eventos del expediente
    public enum Evento {

    }

    //Perfiles del expediente
    public enum Profile {

    }


    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

    //Estados del expediente
    public enum Estado {

		ENTRADA_DATOS(Profile.CREADOR,true,false,Event.DELETEEvent.PRESENTAR),


		FIRMA_POR_USUARIO(Profile.CREADOR,false,false,Event.BACKEvent.PRESENTAR_DOCUMENTOS_FIRMADOS),


		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false,Event.RESOLVER),


		ACEPTADO(Profile.RESPONSABLE,false,true,Event.EXIT),


		RECHAZADO(Profile.RESPONSABLE,false,true,Event.EXIT)


    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los eventos
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Eventos del expediente
    public enum Evento {

    }

    //Perfiles del expediente
    public enum Profile {

    }


    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

    //Estados del expediente
    public enum Estado {

		ENTRADA_DATOS(Profile.CREADOR,true,false,Event.DELETE,Event.PRESENTAR),


		FIRMA_POR_USUARIO(Profile.CREADOR,false,false,Event.BACK,Event.PRESENTAR_DOCUMENTOS_FIRMADOS),


		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false,Event.RESOLVER),


		ACEPTADO(Profile.RESPONSABLE,false,true,Event.EXIT),


		RECHAZADO(Profile.RESPONSABLE,false,true,Event.EXIT)


    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los eventos
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Eventos del expediente
    public enum Evento {

    }

    //Perfiles del expediente
    public enum Profile {

    }


    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

    //Estados del expediente
    public enum Estado {

		ENTRADA_DATOS(Profile.CREADOR,true,false,Event.DELETE,Event.PRESENTAR),


		FIRMA_POR_USUARIO(Profile.CREADOR,false,false,Event.BACK,Event.PRESENTAR_DOCUMENTOS_FIRMADOS),


		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false,Event.RESOLVER),


		ACEPTADO(Profile.RESPONSABLE,false,true,Event.EXIT),


		RECHAZADO(Profile.RESPONSABLE,false,true,Event.EXIT)


    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los eventos
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Eventos del expediente
    public enum Evento {

    }

    //Perfiles del expediente
    public enum Profile {

    }


    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

    //Estados del expediente
    public enum Estado {

		ENTRADA_DATOS(Profile.CREADOR,true,false,Event.DELETE,Event.PRESENTAR),


		FIRMA_POR_USUARIO(Profile.CREADOR,false,false,Event.BACK,Event.PRESENTAR_DOCUMENTOS_FIRMADOS),


		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false,Event.RESOLVER),


		ACEPTADO(Profile.RESPONSABLE,false,true,Event.EXIT),


		RECHAZADO(Profile.RESPONSABLE,false,true,Event.EXIT)


    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los eventos
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Eventos del expediente
    public enum Evento {

    }

    //Perfiles del expediente
    public enum Profile {

    }


    public void updateState(Estado estado) {
        super.updateState(estado);
        super.updateCurrentProfile(estado.getProfile());
    }

    //Estados del expediente
    public enum Estado {
		ENTRADA_DATOS(Profile.CREADOR,true,false,Event.DELETE,Event.PRESENTAR),
		FIRMA_POR_USUARIO(Profile.CREADOR,false,false,Event.BACK,Event.PRESENTAR_DOCUMENTOS_FIRMADOS),
		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false,Event.RESOLVER),
		ACEPTADO(Profile.RESPONSABLE,false,true,Event.EXIT),
		RECHAZADO(Profile.RESPONSABLE,false,true,Event.EXIT)

    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los events
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Events del expediente
    public enum Event {
		DELETE,
		PRESENTAR,
		BACK,
		PRESENTAR_DOCUMENTOS_FIRMADOS,
		RESOLVER,
		EXIT
    }

    //Perfiles del expediente
    public enum Profile {
		CREADOR,
		RESPONSABLE
    }


    public void updateState(State state) {
        super.updateState(state);
        super.updateCurrentProfile(state.getProfile());
    }

    //Estados del expediente
    public enum Estado {
		ENTRADA_DATOS(Profile.CREADOR,true,false,Event.DELETE,Event.PRESENTAR),
		FIRMA_POR_USUARIO(Profile.CREADOR,false,false,Event.BACK,Event.PRESENTAR_DOCUMENTOS_FIRMADOS),
		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false,Event.RESOLVER),
		ACEPTADO(Profile.RESPONSABLE,false,true,Event.EXIT),
		RECHAZADO(Profile.RESPONSABLE,false,true,Event.EXIT)

    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los events
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Events del expediente
    public enum Event {
DELETE,
PRESENTAR,
BACK,
PRESENTAR_DOCUMENTOS_FIRMADOS,
RESOLVER,
EXIT
    }

    //Perfiles del expediente
    public enum Profile {
CREADOR,
RESPONSABLE
    }


    public void updateState(State state) {
        super.updateState(state);
        super.updateCurrentProfile(state.getProfile());
    }

    //Estados del expediente
    public enum Estado {
		ENTRADA_DATOS(Profile.CREADOR,true,false,Event.DELETE,Event.PRESENTAR),
		FIRMA_POR_USUARIO(Profile.CREADOR,false,false,Event.BACK,Event.PRESENTAR_DOCUMENTOS_FIRMADOS),
		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false,Event.RESOLVER),
		ACEPTADO(Profile.RESPONSABLE,false,true,Event.EXIT),
		RECHAZADO(Profile.RESPONSABLE,false,true,Event.EXIT)

    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los events
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Events del expediente
    public enum Event {
	DELETE,
	PRESENTAR,
	BACK,
	PRESENTAR_DOCUMENTOS_FIRMADOS,
	RESOLVER,
	EXIT
    }

    //Perfiles del expediente
    public enum Profile {
	CREADOR,
	RESPONSABLE
    }


    public void updateState(State state) {
        super.updateState(state);
        super.updateCurrentProfile(state.getProfile());
    }

    //Estados del expediente
    public enum Estado {
		ENTRADA_DATOS(Profile.CREADOR,true,false,Event.DELETE,Event.PRESENTAR),
		FIRMA_POR_USUARIO(Profile.CREADOR,false,false,Event.BACK,Event.PRESENTAR_DOCUMENTOS_FIRMADOS),
		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false,Event.RESOLVER),
		ACEPTADO(Profile.RESPONSABLE,false,true,Event.EXIT),
		RECHAZADO(Profile.RESPONSABLE,false,true,Event.EXIT)

    private final Profile profile;
    private final List<Event> events;
    private final boolean initial;
    private final boolean close;

    // Constructor del enum Estado
    Estado(Profile profile, boolean initial, boolean close, Event... events) { // Usamos varargs para los events
        this.profile = profile;
        this.initial=initial;
        tihs.close=close;
        this.events = Arrays.asList(events); // Convertimos el array de varargs a una List
    }

    // Getter para la propiedad profile
    public Profile getProfile() {
        return profile;
    }

    // Getter para la propiedad events
    public List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClose() {
        return close;
    }


    }

    //Events del expediente
    public enum Event {
        DELETE,
         PRESENTAR,
         BACK,
         PRESENTAR_DOCUMENTOS_FIRMADOS,
         RESOLVER,
         EXIT
    }

    //Perfiles del expediente
    public enum Profile {
        CREADOR,
        RESPONSABLE
    }


    public void updateState(State state) {
        super.updateState(state);
        super.updateCurrentProfile(state.getProfile());
    }

    //Estados del expediente
    public enum State {
		ENTRADA_DATOS(Profile.CREADOR,true,false,Event.DELETE,Event.PRESENTAR),
		FIRMA_POR_USUARIO(Profile.CREADOR,false,false,Event.BACK,Event.PRESENTAR_DOCUMENTOS_FIRMADOS),
		REVISION_Y_FIRMA_POR_RESPONSABLE(Profile.RESPONSABLE,false,false,Event.RESOLVER),
		ACEPTADO(Profile.RESPONSABLE,false,true,Event.EXIT),
		RECHAZADO(Profile.RESPONSABLE,false,true,Event.EXIT);

    private final Profile profile;
    private final java.util.List<Event> events;
    private final boolean initial;
    private final boolean closed;

    // Constructor del enum Estado
    State(Profile profile, boolean initial, boolean closed, Event... events) { // Usamos varargs para los events
        this.profile = profile;
        this.initial=initial;
        this.closed=closed;
        this.events = java.util.Arrays.asList(events); // Convertimos el array de varargs a una List
    }


    public Profile getProfile() {
        return profile;
    }

    public java.util.List<Event> getEvents() {
        return events;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isClosed() {
        return closed;
    }


    }

    //Events del expediente
    public enum Event {
        DELETE,
        PRESENTAR,
        BACK,
        PRESENTAR_DOCUMENTOS_FIRMADOS,
        RESOLVER,
        EXIT
    }

    //Perfiles del expediente
    public enum Profile {
        CREADOR,
        RESPONSABLE
    }


    public void updateState(State state) {
        super.updateState(state);
        //super.updateCurrentProfile(state.getProfile());
    }
}