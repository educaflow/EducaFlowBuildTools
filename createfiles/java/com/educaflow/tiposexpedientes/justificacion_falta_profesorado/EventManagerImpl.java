package com.educaflow.apps.expedientes.tiposexpedientes.justificacion_falta_profesorado;

import com.axelor.inject.Beans;
import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.TipoResolucionJustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.repo.JustificacionFaltaProfesoradoRepository;
import com.educaflow.common.validation.messages.BusinessException;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;


public class EventManagerImpl extends com.educaflow.apps.expedientes.common.EventManager<JustificacionFaltaProfesorado, JustificacionFaltaProfesorado.State, JustificacionFaltaProfesorado.Event,JustificacionFaltaProfesorado.Profile> {

    private final JustificacionFaltaProfesoradoRepository repository;

    @Inject
    public EventManagerImpl(JustificacionFaltaProfesoradoRepository repository) {
        super(JustificacionFaltaProfesorado.class, JustificacionFaltaProfesorado.State.class, JustificacionFaltaProfesorado.Event.class,JustificacionFaltaProfesorado.Profile.class);
        this.repository = repository;
    }

    @Override
    public void triggerInitialEvent(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) throws BusinessException {


        justificacionFaltaProfesorado.setAnyo(LocalDate.now().getYear());
        justificacionFaltaProfesorado.setNombre("Lorenzo");
        justificacionFaltaProfesorado.setApellidos("Acción García");
        justificacionFaltaProfesorado.setDni("12345678Z");

    }

    @WhenEvent
    public void triggerPresentar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) throws BusinessException {
        appendDocuments(justificacionFaltaProfesorado);
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.FIRMA_POR_USUARIO);


    }
    @WhenEvent
    public void triggerPresentarDocumentosFirmados(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) throws BusinessException {
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.REVISION_Y_FIRMA_POR_RESPONSABLE);
        justificacionFaltaProfesorado.setTipoResolucion(TipoResolucionJustificacionFaltaProfesorado.ACEPTAR);
        justificacionFaltaProfesorado.setDisconformidad(null);
        justificacionFaltaProfesorado.setResolucion(null);
    }

    @WhenEvent
    public void triggerResolver(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext)  throws BusinessException {
        TipoResolucionJustificacionFaltaProfesorado tipoResolucion = justificacionFaltaProfesorado.getTipoResolucion();

        switch (tipoResolucion) {
            case ACEPTAR:
                justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.ACEPTADO);
                break;
            case RECHAZAR:
                justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.RECHAZADO);
                break;
            case SUBSANAR_DATOS:
                justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.ENTRADA_DATOS);
                break;
            default:
                throw new IllegalArgumentException("Tipo de resolución no reconocido: " + tipoResolucion);
        }
    }


    @WhenEvent
    public void triggerBack(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext)  throws BusinessException {
            JustificacionFaltaProfesorado.State state=JustificacionFaltaProfesorado.State.valueOf(justificacionFaltaProfesorado.getCodeState());

            switch (state) {
                case ENTRADA_DATOS:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.ENTRADA_DATOS);
                    break;
                case FIRMA_POR_USUARIO:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.ENTRADA_DATOS);
                    break;
                case REVISION_Y_FIRMA_POR_RESPONSABLE:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.ENTRADA_DATOS);
                    break;
                case ACEPTADO:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.REVISION_Y_FIRMA_POR_RESPONSABLE);
                    break;
                case RECHAZADO:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.REVISION_Y_FIRMA_POR_RESPONSABLE);
                    break;
                default:
                    throw new IllegalArgumentException("State no reconocido: " + state);
            }

    }


    @WhenEvent
    public void triggerDelete(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) throws BusinessException {
        //justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }


    @OnEnterState
    public void onEnterEntradaDatos(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }


    @OnEnterState
    public void onEnterFirmaPorUsuario(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }

    @OnEnterState
    public void onEnterRevisionYFirmaPorResponsable(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }


    @OnEnterState
    public void onEnterAceptado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }

    @OnEnterState
    public void onEnterRechazado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }



    public void appendDocuments(JustificacionFaltaProfesorado justificacionFaltaProfesorado) {
        String fileName = "justificacion_falta_profesor.pdf";
        String resourcePath = "pdf-templates/" + fileName;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            justificacionFaltaProfesorado.setDocumentoCompletoSinFirmar(Beans.get(com.axelor.meta.MetaFiles.class).upload(inputStream, fileName));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}