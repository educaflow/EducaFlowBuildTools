package com.educaflow.common.buildtools.xml2pdf;

import com.educaflow.common.buildtools.common.FalloTraduccionException;
import com.educaflow.common.buildtools.common.Traductor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;

/**
 * Completa los &lt;valenciano&gt; que faltan en el XML de un documento
 * traduciendo su &lt;castellano&gt; con el mismo proceso traductor externo
 * (apertium) que usa i18nprocessor.
 *
 * Solo se traduce cuando el elemento &lt;valenciano&gt; NO está: un
 * &lt;valenciano/&gt; vacío es la forma de decir "este texto no lleva
 * valenciano", y se respeta.
 *
 * Los campos inline ${expresion;n} y las URL no se traducen (apertium
 * traduciría las palabras de la URL): antes de traducir se sustituyen por un
 * marcador que el traductor deja intacto, y después se restauran. Para que no
 * se traduzca ninguna otra cosa (siglas, nombres propios…) hay que marcarla en
 * el &lt;castellano&gt; con el sufijo de Traductor, que no se dibuja.
 */
class TraductorValenciano {

    /** Lo que se traduce tal cual: campos inline ${expresion;n} y URL. */
    static final Pattern NO_TRADUCIBLE = Pattern.compile(
            Xml2Pdf.INLINE.pattern() + "|[a-zA-Z][a-zA-Z0-9+.-]*://[^\\s]+");

    /** Prefijo del marcador con el que se protege lo no traducible; se le añade
     * el sufijo de Traductor para que el traductor no lo dé por erróneo aunque
     * no conozca la palabra (y luego se lo quite). */
    static final String MARCADOR = "Xnotraduir";

    /** Compartida por todos los documentos de una misma ejecución: los
     * fragmentos _*.xml repiten los mismos textos en muchos documentos y cada
     * traducción cuesta un proceso externo. */
    private static final Map<String, String> traducciones = new HashMap<>();

    private final String procesoTraductor;
    private final Traductor traductor;
    private final String descripcion;

    TraductorValenciano(String procesoTraductor, String descripcion) {
        this.procesoTraductor = procesoTraductor;
        this.traductor = new Traductor(procesoTraductor);
        this.descripcion = descripcion;
    }

    /** Recorre el documento ya expandido e inserta el &lt;valenciano&gt;
     * traducido en todo elemento que tenga &lt;castellano&gt; con texto y no
     * tenga &lt;valenciano&gt;. */
    void completarValenciano(Element elemento) {
        Element castellano = null;
        boolean hayValenciano = false;
        for (Element hijo : Xml2Pdf.children(elemento)) {
            if (hijo.getTagName().equals("castellano")) {
                castellano = hijo;
            } else if (hijo.getTagName().equals("valenciano")) {
                hayValenciano = true;
            } else {
                completarValenciano(hijo);
            }
        }

        if (castellano == null || hayValenciano) {
            return;
        }
        String texto = castellano.getTextContent().trim();
        if (texto.isEmpty()) {
            return;
        }
        Element valenciano = elemento.getOwnerDocument().createElement("valenciano");
        valenciano.setTextContent(traducir(texto, castellano));
        // el esquema exige <valenciano> antes de <castellano>
        elemento.insertBefore(valenciano, castellano);
    }

    /** El &lt;castellano&gt; del que sale el texto se pasa solo para poder
     * decir en qué fichero y línea está el texto que no se supo traducir: con
     * los fragmentos _*.xml compartidos, el fichero del documento que se está
     * generando no es el fichero que hay que arreglar. */
    private String traducir(String castellano, Element origen) {
        String donde = Xml2Pdf.ubicacionO(origen, descripcion);
        String cacheado = traducciones.get(castellano);
        if (cacheado != null) {
            return cacheado;
        }

        // proteger los campos inline y las URL
        List<String> literales = new ArrayList<>();
        StringBuffer protegido = new StringBuffer();
        Matcher m = NO_TRADUCIBLE.matcher(castellano);
        while (m.find()) {
            m.appendReplacement(protegido, Matcher.quoteReplacement(
                    marcador(literales.size()) + Traductor.SUFIJO_NO_TRADUCIR));
            literales.add(m.group());
        }
        m.appendTail(protegido);

        String valenciano;
        try {
            valenciano = traductor.traducirDesdeCastellanoAValenciano(protegido.toString());
        } catch (FalloTraduccionException ex) {
            throw new RuntimeException("ERROR: no se pudo traducir al valenciano el texto \""
                    + castellano + "\" de " + donde + " (el traductor devolvió \""
                    + ex.getTraduccion() + "\", las palabras que no supo traducir van con '*'):"
                    + " añade el elemento <valenciano> con la traducción, o marca en el"
                    + " <castellano> las palabras que no se deben traducir (siglas, nombres"
                    + " propios…) con el sufijo " + Traductor.SUFIJO_NO_TRADUCIR
                    + " (no se dibuja en el PDF).", ex);
        } catch (RuntimeException ex) {
            throw new RuntimeException("ERROR: fallo al ejecutar el proceso traductor '"
                    + procesoTraductor + "' para traducir al valenciano el texto \"" + castellano
                    + "\" de " + donde + ": o lo instalas, o añades el elemento <valenciano>"
                    + " con la traducción.", ex);
        }

        // restaurar; en orden descendente porque el marcador 1 es prefijo del 10
        for (int i = literales.size() - 1; i >= 0; i--) {
            if (!valenciano.contains(marcador(i))) {
                throw new RuntimeException("ERROR: el traductor perdió \"" + literales.get(i)
                        + "\" al traducir al valenciano el texto \"" + castellano
                        + "\" de " + donde + ": añade el elemento <valenciano> con la traducción.");
            }
            valenciano = valenciano.replace(marcador(i), literales.get(i));
        }

        traducciones.put(castellano, valenciano);
        return valenciano;
    }

    private static String marcador(int indice) {
        return MARCADOR + indice;
    }
}