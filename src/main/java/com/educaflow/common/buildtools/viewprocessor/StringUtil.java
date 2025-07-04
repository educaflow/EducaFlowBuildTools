package com.educaflow.common.buildtools.viewprocessor;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author logongas
 */
public class StringUtil {
    
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
    
}
