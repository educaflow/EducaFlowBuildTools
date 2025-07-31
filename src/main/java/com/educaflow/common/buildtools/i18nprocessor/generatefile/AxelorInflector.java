/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.i18nprocessor.generatefile;

/**
 *
 * @author logongas
 */
public class AxelorInflector {

    
    /**
     * Esta es la función que usa Axelor para obtener el "title" cuando no lo hay.
     * Y es justamente el texto que habrá que traducir.
     * Este se usa con el "name" de una columna de un modelo.
     * Por eso usamos exactamente la misma función que usa Axelor.
     * @param word
     * @return 
     */
    public static String humanize(String word) {
        String result = underscore(word).replaceAll("_id$", "").replaceAll("\\A_+", "").replaceAll("[_\\s]+", " ");
        return capitalize(result);
    }

    private static String capitalize(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    private static String underscore(String camelCase) {
        return camelCase
                .trim()
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z\\d])([A-Z])", "$1_$2")
                .replaceAll("[-\\s]+", "_")
                .toLowerCase();
    }

}
