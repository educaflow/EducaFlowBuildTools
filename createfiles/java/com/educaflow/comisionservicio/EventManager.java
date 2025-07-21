package com.educaflow.comisionservicio;

import com.axelor.inject.Beans;
import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.repo.JustificacionFaltaProfesoradoRepository;
import com.google.inject.Inject;



public class EventManager extends com.educaflow.apps.expedientes.common.EventManager<JustificacionFaltaProfesorado, JustificacionFaltaProfesorado.Estado, JustificacionFaltaProfesorado.Evento,JustificacionFaltaProfesorado.Profile> {

    private final JustificacionFaltaProfesoradoRepository repository;

    @Inject
    public EventManager(JustificacionFaltaProfesoradoRepository repository) {
        super(JustificacionFaltaProfesorado.class, JustificacionFaltaProfesorado.Estado.class, JustificacionFaltaProfesorado.Evento.class,JustificacionFaltaProfesorado.Profile.class);
        this.repository = repository;
    }

    @Override
    public void triggerInitialEvent(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {


    }




    @WhenEvent
    public void triggerDelete(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {
        //justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }




    @WhenEvent
    public void triggerPresentar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {
        //justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }




    @WhenEvent
    public void triggerBack(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {
        //justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }




    @WhenEvent
    public void triggerPresentarDocumentosFirmados(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {
        //justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }




    @WhenEvent
    public void triggerResolver(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {
        //justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }









    @OnEnterState
    public void onEnterEntradaDatos(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {

    }




    @OnEnterState
    public void onEnterFirmaPorUsuario(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {

    }




    @OnEnterState
    public void onEnterRevisionYFirmaPorResponsable(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {

    }




    @OnEnterState
    public void onEnterAceptado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {

    }




    @OnEnterState
    public void onEnterRechazado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext<JustificacionFaltaProfesorado.Profile> eventContext) {

    }










//hola mundo
}