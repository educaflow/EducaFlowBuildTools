package com.educaflow.common.buildtools.i18nprocessor.generatefile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author logongas
 */
public class Traductor {

    private static final String SUFIJO_NO_TRADUCIR="__!!";
    
    public static String traducirDesdeCastellanoAValenciano(String textoCastellano) throws FalloTraduccionException {
        try {

            Process process = new ProcessBuilder("apertium", "spa-cat_valencia")
                    .redirectErrorStream(true)
                    .start();

            // Enviar el texto a Apertium
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(textoCastellano);
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {

                    if (sb.length() == 0) {
                        sb.append(line);
                    } else {
                        sb.append("\n").append(line);
                    }

                }
            }

            String traduccion = sb.toString();

            if (isTraduccionErronea(traduccion)) {
                throw new FalloTraduccionException(textoCastellano, traduccion);
            }

            traduccion = traduccion.replaceAll("\\*", "");
            traduccion=traduccion.replace(SUFIJO_NO_TRADUCIR,"");

            return traduccion;

        } catch (FalloTraduccionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean isTraduccionErronea(String traduccion) {
        Matcher matcher = Pattern.compile("\\*([^\\s]+)").matcher(traduccion);

        while (matcher.find()) {
            String palabra = matcher.group(1);

            // Si la palabra termina en punto, no es errónea
            if (terminaEnPunto(traduccion, matcher.start(1), matcher.end(1))) {
                continue;
            }
            if (palabra.endsWith(SUFIJO_NO_TRADUCIR)) {
                continue;
            }
            
            if (palabra.equals(palabra.toUpperCase())) {
                continue;
            }
            

            return false;
        }

        
        
        return false;
    }

    // Función auxiliar que comprueba si justo después de la palabra hay un '.'
    private static boolean terminaEnPunto(String texto, int start, int end) {
        // Comprobar que no nos pasamos del límite
        if (end < texto.length() && texto.charAt(end) == '.') {
            return true;
        }
        return false;
    }
}
