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




/**
     Estados del expediente
    **/

    public enum Estado {
    ENTRADA_DATOS,    FIRMA_POR_USUARIO,    REVISION_Y_FIRMA_POR_RESPONSABLE,    ACEPTADO,    RECHAZADO
    }

    


    /**
     Eventos del expediente
    **/

    public enum Evento {
    PRESENTAR,    PRESENTAR_DOCUMENTOS_FIRMADOS,    RESOLVER,    BACK
    }

    

    /**
     Profiles que usa el expediente
    **/

    public enum Profile {
    CREADOR,    RESPONSABLE,    AUDITOR
    }

    




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/

    public enum Estado {
    -,    
    }

    


    /**
     Eventos del expediente
    **/

    public enum Evento {
    -,    
    }

    

    /**
     Profiles que usa el expediente
    **/

    public enum Profile {
    -,    
    }

    




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/

    public enum Estado {
    


ENTRADA_DATOS-,    

FIRMA_POR_USUARIO-,    

REVISION_Y_FIRMA_POR_RESPONSABLE-,    

ACEPTADO-,    

RECHAZADO-


    
    }

    


    /**
     Eventos del expediente
    **/

    public enum Evento {
    


PRESENTAR-,    

PRESENTAR_DOCUMENTOS_FIRMADOS-,    

RESOLVER-,    

BACK-


    
    }

    

    /**
     Profiles que usa el expediente
    **/

    public enum Profile {
    


CREADOR-,    

RESPONSABLE-,    

AUDITOR-


    
    }

    




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/

    public enum Estado {
        
        
        ENTRADA_DATOS,    
        
        FIRMA_POR_USUARIO,    
        
        REVISION_Y_FIRMA_POR_RESPONSABLE,    
        
        ACEPTADO,    
        
        RECHAZADO
        
        
    }

    


    /**
     Eventos del expediente
    **/

    public enum Evento {
        
        
        PRESENTAR,    
        
        PRESENTAR_DOCUMENTOS_FIRMADOS,    
        
        RESOLVER,    
        
        BACK
        
        
    }

    

    /**
     Profiles que usa el expediente
    **/

    public enum Profile {
        
        
        CREADOR,    
        
        RESPONSABLE,    
        
        AUDITOR
        
        
    }

    




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/

    public enum Estado {
        
        ENTRADA_DATOS,FIRMA_POR_USUARIO,REVISION_Y_FIRMA_POR_RESPONSABLE,ACEPTADO,RECHAZADO
        
    }

    


    /**
     Eventos del expediente
    **/

    public enum Evento {
        
        PRESENTAR,PRESENTAR_DOCUMENTOS_FIRMADOS,RESOLVER,BACK
        
    }

    

    /**
     Profiles que usa el expediente
    **/

    public enum Profile {
        
        CREADOR,RESPONSABLE,AUDITOR
        
    }

    




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/

    public enum Estado {
        
        ENTRADA_DATOS,FIRMA_POR_USUARIO,REVISION_Y_FIRMA_POR_RESPONSABLE,ACEPTADO,RECHAZADO
        
    }

    


    /**
     Eventos del expediente
    **/

    public enum Evento {
        
        PRESENTAR,PRESENTAR_DOCUMENTOS_FIRMADOS,RESOLVER,BACK
        
    }

    

    /**
     Profiles que usa el expediente
    **/

    public enum Profile {
        
        CREADOR,RESPONSABLE,AUDITOR
        
    }

    




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/

    public enum Estado {
        
        ENTRADA_DATOS,FIRMA_POR_USUARIO,REVISION_Y_FIRMA_POR_RESPONSABLE,ACEPTADO,RECHAZADO
        
    }

    


    /**
     Eventos del expediente
    **/

    public enum Evento {
        
        PRESENTAR,PRESENTAR_DOCUMENTOS_FIRMADOS,RESOLVER,BACK
        
    }

    

    /**
     Profiles que usa el expediente
    **/

    public enum Profile {
        
        CREADOR,RESPONSABLE,AUDITOR
        
    }

    




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/

    public enum Estado {
        
        ENTRADA_DATOS,,,,,FIRMA_POR_USUARIO,,,,,REVISION_Y_FIRMA_POR_RESPONSABLE,,,,,ACEPTADO,,,,,RECHAZADO
        
    }

    


    /**
     Eventos del expediente
    **/

    public enum Evento {
        
        PRESENTAR,,,,,PRESENTAR_DOCUMENTOS_FIRMADOS,,,,,RESOLVER,,,,,BACK
        
    }

    

    /**
     Profiles que usa el expediente
    **/

    public enum Profile {
        
        CREADOR,,,,,RESPONSABLE,,,,,AUDITOR
        
    }

    




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/

    public enum Estado {
        
        ENTRADA_DATOS,,,,,FIRMA_POR_USUARIO,,,,,REVISION_Y_FIRMA_POR_RESPONSABLE,,,,,ACEPTADO,,,,,RECHAZADO
        
    }

    


    /**
     Eventos del expediente
    **/

    public enum Evento {
        
        PRESENTAR,,,,,PRESENTAR_DOCUMENTOS_FIRMADOS,,,,,RESOLVER,,,,,BACK
        
    }

    

    /**
     Profiles que usa el expediente
    **/

    public enum Profile {
        
        CREADOR,,,,,RESPONSABLE,,,,,AUDITOR
        
    }

    




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/


    public enum Estado {
        
        ENTRADA_DATOS,
FIRMA_POR_USUARIO,
REVISION_Y_FIRMA_POR_RESPONSABLE,
ACEPTADO,
RECHAZADO
        
    }

    



    /**
     Eventos del expediente
    **/


    public enum Evento {
        
        PRESENTAR,
PRESENTAR_DOCUMENTOS_FIRMADOS,
RESOLVER,
BACK
        
    }

    


    /**
     Profiles que usa el expediente
    **/


    public enum Profile {
        
        CREADOR,
RESPONSABLE,
AUDITOR
        
    }

    





    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/


    public enum Estado {

ENTRADA_DATOS,
FIRMA_POR_USUARIO,
REVISION_Y_FIRMA_POR_RESPONSABLE,
ACEPTADO,
RECHAZADO

    }

    



    /**
     Eventos del expediente
    **/


    public enum Evento {

PRESENTAR,
PRESENTAR_DOCUMENTOS_FIRMADOS,
RESOLVER,
BACK

    }

    


    /**
     Profiles que usa el expediente
    **/


    public enum Profile {

CREADOR,
RESPONSABLE,
AUDITOR

    }

    





    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

/**
     Estados del expediente
    **/public enum Estado {

ENTRADA_DATOS,
FIRMA_POR_USUARIO,
REVISION_Y_FIRMA_POR_RESPONSABLE,
ACEPTADO,
RECHAZADO

    }


    /**
     Eventos del expediente
    **/public enum Evento {

PRESENTAR,
PRESENTAR_DOCUMENTOS_FIRMADOS,
RESOLVER,
BACK

    }

    /**
     Profiles que usa el expediente
    **/public enum Profile {

CREADOR,
RESPONSABLE,
AUDITOR

    }




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }


public enum Estado {

ENTRADA_DATOS,
FIRMA_POR_USUARIO,
REVISION_Y_FIRMA_POR_RESPONSABLE,
ACEPTADO,
RECHAZADO

    }


    
public enum Evento {

PRESENTAR,
PRESENTAR_DOCUMENTOS_FIRMADOS,
RESOLVER,
BACK

    }

    
public enum Profile {

CREADOR,
RESPONSABLE,
AUDITOR

    }




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }


	public enum Estado {

		ENTRADA_DATOS,
		FIRMA_POR_USUARIO,
		REVISION_Y_FIRMA_POR_RESPONSABLE,
		ACEPTADO,
		RECHAZADO

    	}


    
	public enum Evento {

		PRESENTAR,
		PRESENTAR_DOCUMENTOS_FIRMADOS,
		RESOLVER,
		BACK

    	}

    
	public enum Profile {

		CREADOR,
		RESPONSABLE,
		AUDITOR

    	}




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }


	public enum Estado {

		ENTRADA_DATOS,
		FIRMA_POR_USUARIO,
		REVISION_Y_FIRMA_POR_RESPONSABLE,
		ACEPTADO,
		RECHAZADO

    }


    
	public enum Evento {

		PRESENTAR,
		PRESENTAR_DOCUMENTOS_FIRMADOS,
		RESOLVER,
		BACK

    }

    
	public enum Profile {

		CREADOR,
		RESPONSABLE,
		AUDITOR

    }




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }


	public enum Estado {
		ENTRADA_DATOS,
		FIRMA_POR_USUARIO,
		REVISION_Y_FIRMA_POR_RESPONSABLE,
		ACEPTADO,
		RECHAZADO
    }


    
	public enum Evento {
		PRESENTAR,
		PRESENTAR_DOCUMENTOS_FIRMADOS,
		RESOLVER,
		BACK
    }

    
	public enum Profile {
		CREADOR,
		RESPONSABLE,
		AUDITOR
    }




    public void updateState(Estado estado) {
        super.updateState(estado);
    }



    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }


	public enum Estado {
		ENTRADA_DATOS,
		FIRMA_POR_USUARIO,
		REVISION_Y_FIRMA_POR_RESPONSABLE,
		ACEPTADO,
		RECHAZADO
    }


    
	public enum Evento {
		PRESENTAR,
		PRESENTAR_DOCUMENTOS_FIRMADOS,
		RESOLVER,
		BACK
    }

    
	public enum Profile {
		CREADOR,
		RESPONSABLE,
		AUDITOR
    }

    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

	public enum Estado {
		ENTRADA_DATOS,
		FIRMA_POR_USUARIO,
		REVISION_Y_FIRMA_POR_RESPONSABLE,
		ACEPTADO,
		RECHAZADO
    }	public enum Evento {
		PRESENTAR,
		PRESENTAR_DOCUMENTOS_FIRMADOS,
		RESOLVER,
		BACK
    }	public enum Profile {
		CREADOR,
		RESPONSABLE,
		AUDITOR
    }

    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

//Estados del expediente
	public enum Estado {
		ENTRADA_DATOS,
		FIRMA_POR_USUARIO,
		REVISION_Y_FIRMA_POR_RESPONSABLE,
		ACEPTADO,
		RECHAZADO
    }


    //Eventos del expediente
	public enum Evento {
		PRESENTAR,
		PRESENTAR_DOCUMENTOS_FIRMADOS,
		RESOLVER,
		BACK
    }

    //Perfiles del expediente
	public enum Profile {
		CREADOR,
		RESPONSABLE,
		AUDITOR
    }

    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }

	//Estados del expediente
	public enum Estado {
		ENTRADA_DATOS,
		FIRMA_POR_USUARIO,
		REVISION_Y_FIRMA_POR_RESPONSABLE,
		ACEPTADO,
		RECHAZADO
    }


    //Eventos del expediente
	public enum Evento {
		PRESENTAR,
		PRESENTAR_DOCUMENTOS_FIRMADOS,
		RESOLVER,
		BACK
    }

    //Perfiles del expediente
	public enum Profile {
		CREADOR,
		RESPONSABLE,
		AUDITOR
    }

    public void updateState(Estado estado) {
        super.updateState(estado);
    }

    public void setCurrentActionProfiles(Profile...profiles) {
        super.setCurrentActionProfiles(profiles);
    }
}