/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.i18nprocessor.generatefile;

/**
 *
 * @author logongas
 */
public class FalloTraduccionException extends Exception {
    
    private String textoCastellano=null;
    private String traduccion=null;
    
    public FalloTraduccionException() {
        super();
    }
    
    
    public FalloTraduccionException(String textoCastellano,String traduccion) {
        super();
        this.textoCastellano=textoCastellano;
        this.traduccion=traduccion;
    }

    /**
     * @return the textoCastellano
     */
    public String getTextoCastellano() {
        return textoCastellano;
    }

    /**
     * @return the traduccion
     */
    public String getTraduccion() {
        return traduccion;
    }
    
    
    
}
