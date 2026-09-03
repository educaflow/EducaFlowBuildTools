package com.educaflow.common.buildtools.common;

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

    public final static String SUFIJO_NO_TRADUCIR="__!!";
    private final String procesoTraductor;
    
    public Traductor(String procesoTraductor) {
        this.procesoTraductor=procesoTraductor;
    }
    
    public String traducirDesdeCastellanoAValenciano(String textoCastellano) throws FalloTraduccionException {
        try {

            // La salida de errores NO se mezcla con la traducción: si se mezcla,
            // un fallo del traductor se acaba colando como si fuera valenciano.
            Process process = new ProcessBuilder(this.procesoTraductor, "spa-cat_valencia")
                    .start();

            // En un hilo aparte porque leer los dos flujos uno detrás de otro se
            // bloquea si el proceso llena el buffer del que todavía no se lee.
            StringBuilder errores = new StringBuilder();
            Thread lectorErrores = new Thread(() -> leerFlujo(process.getErrorStream(), errores));
            lectorErrores.start();

            // Enviar el texto a Apertium
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(textoCastellano);
            }

            StringBuilder sb = new StringBuilder();
            leerFlujo(process.getInputStream(), sb);

            int codigoSalida = process.waitFor();
            lectorErrores.join();

            String traduccion = sb.toString();

            comprobarQueElTraductorHaFuncionado(textoCastellano, traduccion, errores.toString(), codigoSalida);

            if (isTraduccionErronea(traduccion)) {
                throw new FalloTraduccionException(textoCastellano, traduccion);
            }

            traduccion = traduccion.replaceAll("\\*", "");
            traduccion=traduccion.replace(SUFIJO_NO_TRADUCIR,"");

            return traduccion;

        } catch (FalloTraduccionException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Comprueba que el proceso traductor ha traducido de verdad.
     *
     * No basta con mirar el código de salida: si a apertium le falta alguna de
     * las piezas de la tubería de su modo (cg-proc, de cg3, o lrx-proc, de
     * apertium-lex-tools) NO traduce nada y aun así termina con código 0,
     * dejando el error solo en la salida de errores y la traducción vacía. Como
     * ese texto no lleva ninguna palabra marcada con '*', isTraduccionErronea lo
     * da por bueno, así que si no se comprueba aquí se cuela como valenciano.
     */
    private void comprobarQueElTraductorHaFuncionado(String textoCastellano, String traduccion,
            String errores, int codigoSalida) {
        if (codigoSalida == 0 && errores.isEmpty()
                && !(traduccion.isBlank() && !textoCastellano.isBlank())) {
            return;
        }

        throw new RuntimeException("ERROR: el proceso traductor '" + procesoTraductor
                + " spa-cat_valencia' no tradujo el texto \"" + textoCastellano
                + "\": terminó con código de salida " + codigoSalida + ", devolvió \""
                + traduccion + "\" y escribió en la salida de errores \"" + errores + "\"."
                + " Comprueba que están instalados apertium, el par de idiomas"
                + " (apertium-spa-cat) y las herramientas que usa la tubería de su modo:"
                + " cg3 (aporta cg-proc) y apertium-lex-tools (aporta lrx-proc).");
    }

    /** Vuelca un flujo del proceso en {@code destino}, línea a línea. */
    private static void leerFlujo(java.io.InputStream flujo, StringBuilder destino) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(flujo))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (destino.length() == 0) {
                    destino.append(line);
                } else {
                    destino.append("\n").append(line);
                }
            }
        } catch (java.io.IOException ex) {
            // El proceso murió mientras se leía: lo diagnostica quien comprueba
            // el código de salida.
        }
    }

    private boolean isTraduccionErronea(String traduccion) {
        Matcher matcher = Pattern.compile("\\*([^\\s]+)").matcher(traduccion);

        while (matcher.find()) {
            String palabra = matcher.group(1);

            // Si la palabra termina en punto, no es errónea
            if (terminaEnPunto(traduccion, matcher.start(1), matcher.end(1))) {
                continue;
            }
            //contains y no endsWith porque la palabra puede llevar pegada la
            //puntuación que la sigue: "(RATs__!!)"
            if (palabra.contains(SUFIJO_NO_TRADUCIR)) {
                continue;
            }
            
            if (palabra.equals(palabra.toUpperCase())) {
                continue;
            }
            

            return true;
        }

        
        
        return false;
    }

    // Función auxiliar que comprueba si justo después de la palabra hay un '.'
    private boolean terminaEnPunto(String texto, int start, int end) {
        // Comprobar que no nos pasamos del límite
        if (end < texto.length() && texto.charAt(end) == '.') {
            return true;
        }
        return false;
    }
}
