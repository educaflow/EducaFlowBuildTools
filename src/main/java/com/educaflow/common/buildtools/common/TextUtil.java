/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.common;

import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author logongas
 */
public class TextUtil {
    public static String caseLowerFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static List<String>  getUpperCamelCase(List<? extends Object> list) {
        List<String> upperCamelCaseList = new ArrayList<>();

        for (Object obj : list) {
            upperCamelCaseList.add(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, obj + ""));
        }

        return upperCamelCaseList;

    }
    
        public static String getHumanNameFromExpedienteName(String expedienteName) {
        if (expedienteName == null || expedienteName.isEmpty()) {
            return "";
        }


        StringBuilder spacedWordBuilder = new StringBuilder();
        spacedWordBuilder.append(expedienteName.charAt(0)); 

        for (int i = 1; i < expedienteName.length(); i++) {
            char currentChar = expedienteName.charAt(i);
            char prevChar = expedienteName.charAt(i - 1);

            if (Character.isLowerCase(prevChar) && Character.isUpperCase(currentChar)) {
                spacedWordBuilder.append(" ");
            }

            else if (Character.isUpperCase(prevChar) && Character.isUpperCase(currentChar) &&
                     (i + 1 < expedienteName.length() && Character.isLowerCase(expedienteName.charAt(i + 1)))) {
                spacedWordBuilder.append(" ");
            }
            
            spacedWordBuilder.append(currentChar);
        }

        String[] parts = spacedWordBuilder.toString().split(" ");
        StringBuilder finalResult = new StringBuilder();

        for (int j = 0; j < parts.length; j++) {
            String part = parts[j];
            if (part.isEmpty()) {
                continue;
            }

            if (j > 0) {
                finalResult.append(" ");
            }


            if (part.matches("[A-Z]+")) { 
                finalResult.append(part); 
            } else {
                if (j == 0) {
                    finalResult.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
                } else {
                    finalResult.append(part.toLowerCase());
                }
            }
        }
        
        return finalResult.toString().trim(); 
    }    
        
    public static String getSubstringBetween(String text, String start, String end) {
        int i = text.indexOf(start);
        if (i == -1) return null;  // no encontrada la palabra start
        i += start.length();
        
        int j = text.indexOf(end, i);
        if (j == -1) return null;  // no encontrada la palabra end

        return text.substring(i, j);
    }

    /**
     * Escapa el texto para que pueda ir como valor de un atributo XML.
     */
    public static String escapeXmlAttribute(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * Escapa el texto para que pueda ir dentro de un literal de cadena Java.
     * Hace falta porque el motor de plantillas va con el auto-escaping desactivado y hay literales
     * (el {@code name} del tipo de expediente, los {@code title} de fases y estados) que son texto
     * libre escrito a mano en un XML.
     */
    public static String escapeJavaString(String text) {
        if (text == null) {
            return "";
        }

        StringBuilder escapado = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\' -> escapado.append("\\\\");
                case '"' -> escapado.append("\\\"");
                case '\n' -> escapado.append("\\n");
                case '\r' -> escapado.append("\\r");
                case '\t' -> escapado.append("\\t");
                case '\b' -> escapado.append("\\b");
                case '\f' -> escapado.append("\\f");
                default -> escapado.append(c);
            }
        }

        return escapado.toString();
    }

    /**
     * El texto que ve el usuario a partir de un identificador en UPPER_SNAKE_CASE:
     * {@code ENTRADA_DATOS} → {@code "Entrada datos"}.
     *
     * <p>Es el nombre por omisión de una fase o de un estado que no declara {@code title}, así que
     * <b>debe dar exactamente lo mismo</b> que el {@code humanize} del {@code Inflector} de Axelor,
     * que es lo que hasta ahora calculaba el runtime para el {@code nameState}.
     *
     * <p><b>Delega en {@link AxelorInflector}</b>, que es la copia fiel del {@code Inflector} que ya
     * vive en este repo, y no reimplementa el algoritmo: el <b>mismo</b> nombre de estado lo
     * humanizan dos consumidores del build —el título por omisión que acaba en la clase {@code States}
     * y la clave que el extractor de i18n mete en el CSV—, así que dos implementaciones distintas
     * producen entradas de traducción que nunca casan. Con un nombre legal como {@code ANEXO_2A} la
     * versión anterior daba {@code "Anexo 2a"} aquí y {@code "Anexo 2 a"} en el CSV.
     *
     * <p><b>Sin locale a propósito</b>, pese a ser el patrón habitual de {@code toLowerCase()}: el
     * contrato es ser bug-compatible con el {@code Inflector} de Axelor, que también es locale-less.
     * Ponerle {@code Locale.ROOT} solo aquí <b>crearía</b> la divergencia con las claves de i18n que
     * esta delegación viene a eliminar.
     */
    public static String humanize(String upperSnakeCase) {
        if ((upperSnakeCase == null) || (upperSnakeCase.isEmpty())) {
            return "";
        }

        return AxelorInflector.humanize(upperSnakeCase);
    }

}
