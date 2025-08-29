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
        
    
}
