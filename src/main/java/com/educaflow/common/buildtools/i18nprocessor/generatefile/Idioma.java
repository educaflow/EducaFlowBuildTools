/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.educaflow.common.buildtools.i18nprocessor.generatefile;

/**
 *
 * @author logongas
 */
public enum Idioma {
    Castellano("es"),
    Valenciano("ca");
    
    String code;
    
    Idioma(String code) {
        this.code=code;
    }
    
    public String getCode() {
        return code;
    }
}
