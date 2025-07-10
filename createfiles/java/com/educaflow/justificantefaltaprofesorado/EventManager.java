package com.educaflow.justificantefaltaprofesorado;

import com.axelor.inject.Beans;
import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.*;
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
    public Expediente triggerInitialEvent(TipoExpediente tipoExpediente, EventContext eventContext) {

        JustificacionFaltaProfesorado justificacionFaltaProfesorado = new JustificacionFaltaProfesorado();
        justificacionFaltaProfesorado.setTipoExpediente(tipoExpediente);
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);


        return justificacionFaltaProfesorado;
    }




    @WhenEvent
    public void triggerPresentar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }



    @WhenEvent
    public void triggerPresentarDocumentosFirmados(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }



    @WhenEvent
    public void triggerResolver(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }



    @WhenEvent
    public void triggerBack(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }





    @WhenEvent
    public void triggerDelete(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {

    }

    @WhenEvent
    public void triggerExit(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {

    }



    @OnEnterState
    public void onEnterEntradaDatos(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(JustificacionFaltaProfesorado.Profile.CREADOR);
        justificacionFaltaProfesorado.setAbierto(true);
    }

    @OnEnterState
    public void onEnterFirmaPorUsuario(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(JustificacionFaltaProfesorado.Profile.CREADOR);
        justificacionFaltaProfesorado.setAbierto(true);
    }

    @OnEnterState
    public void onEnterRevisionYFirmaPorResponsable(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(JustificacionFaltaProfesorado.Profile.CREADOR);
        justificacionFaltaProfesorado.setAbierto(true);
    }

    @OnEnterState
    public void onEnterAceptado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(JustificacionFaltaProfesorado.Profile.CREADOR);
        justificacionFaltaProfesorado.setAbierto(true);
    }

    @OnEnterState
    public void onEnterRechazado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(JustificacionFaltaProfesorado.Profile.CREADOR);
        justificacionFaltaProfesorado.setAbierto(true);
    }









}