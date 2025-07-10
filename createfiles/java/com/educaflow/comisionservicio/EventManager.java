package com.educaflow.comisionservicio;

import com.axelor.inject.Beans;
import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.*;
import com.educaflow.apps.expedientes.db.repo.ComisionServicioRepository;
import com.google.inject.Inject;



public class EventManager extends com.educaflow.apps.expedientes.common.EventManager<ComisionServicio, ComisionServicio.Estado, ComisionServicio.Evento,ComisionServicio.Profile> {

    private final ComisionServicioRepository repository;

    @Inject
    public EventManager(ComisionServicioRepository repository) {
        super(ComisionServicio.class, ComisionServicio.Estado.class, ComisionServicio.Evento.class,ComisionServicio.Profile.class);
        this.repository = repository;
    }

    @Override
    public Expediente triggerInitialEvent(TipoExpediente tipoExpediente, EventContext eventContext) {

        ComisionServicio comisionServicio = new ComisionServicio();
        comisionServicio.setTipoExpediente(tipoExpediente);
        comisionServicio.updateState(ComisionServicio.Estado.);


        return comisionServicio;
    }




    @WhenEvent
    public void triggerPresentar(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {
        comisionServicio.updateState(ComisionServicio.Estado.);
    }



    @WhenEvent
    public void triggerPresentarDocumentosFirmados(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {
        comisionServicio.updateState(ComisionServicio.Estado.);
    }



    @WhenEvent
    public void triggerResolver(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {
        comisionServicio.updateState(ComisionServicio.Estado.);
    }



    @WhenEvent
    public void triggerBack(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {
        comisionServicio.updateState(ComisionServicio.Estado.);
    }





    @WhenEvent
    public void triggerDelete(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {

    }

    @WhenEvent
    public void triggerExit(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {

    }



    @OnEnterState
    public void onEnterEntradaDatos(ComisionServicio comisionServicio, EventContext eventContext) {
        comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.CREADOR);
        comisionServicio.setAbierto(true);
    }

    @OnEnterState
    public void onEnterFirmaPorUsuario(ComisionServicio comisionServicio, EventContext eventContext) {
        comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.CREADOR);
        comisionServicio.setAbierto(true);
    }

    @OnEnterState
    public void onEnterRevisionYFirmaPorResponsable(ComisionServicio comisionServicio, EventContext eventContext) {
        comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.CREADOR);
        comisionServicio.setAbierto(true);
    }

    @OnEnterState
    public void onEnterAceptado(ComisionServicio comisionServicio, EventContext eventContext) {
        comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.CREADOR);
        comisionServicio.setAbierto(true);
    }

    @OnEnterState
    public void onEnterRechazado(ComisionServicio comisionServicio, EventContext eventContext) {
        comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.CREADOR);
        comisionServicio.setAbierto(true);
    }









}