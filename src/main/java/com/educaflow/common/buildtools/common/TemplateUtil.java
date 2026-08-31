/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.common;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Extension;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.extension.escaper.EscapingStrategy;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author logongas
 */
public class TemplateUtil {

    public static String evaluateTemplate(String templateName, Map<String, Object> context) {

        try {
            // La identidad, que es lo que promete su nombre. Existe porque "none" NO es una estrategia
            // nativa de Pebble (las nativas son html/js/css/url_param/json): registrarla es lo que
            // permite poner defaultEscapingStrategy("none") sin que un `| escape` reviente con
            // "Unknown escaping strategy", y sin que el default vuelva a ser `html`.
            // MUST NOT sustituir texto aquí: EscapeFilter.apply la usa con independencia del flag
            // autoEscaping, así que cualquier `| escape`, `{% autoescape %}` o `{{ … }}` que la
            // atraviese vería el texto corrompido (esta hacía `\n` literal -> salto de línea real).
            EscapingStrategy escapingStrategy = (input) -> input;

            AbstractExtension customExtension = new AbstractExtension() {
                @Override
                public Map<String, Function> getFunctions() {
                    Map<String, Function> functions = new HashMap<>();
                    functions.put("asterisks", new AsteriskFunction());
                    functions.put("escapeXml", new EscapeXmlFunction());
                    functions.put("escapeJava", new EscapeJavaFunction());
                    return functions;
                }
            };

            ClasspathLoader loader = new ClasspathLoader();
            PebbleEngine engine = new PebbleEngine.Builder().autoEscaping(false).extension(customExtension).newLineTrimming(false).loader(loader).addEscapingStrategy("none", escapingStrategy).defaultEscapingStrategy("none").build();

            PebbleTemplate compiledTemplate = engine.getTemplate(templateName);

            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, context);

            return writer.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void createFileWithContent(Path path, String content) {

        try {
            if (Files.exists(path) == true) {
                throw new RuntimeException("El fichero no puede existir");
            }

            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public static class AsteriskFunction implements Function {

        @Override
        public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (args.containsKey("input")) {
                String input =  args.get("input")+"";
                if (input == null) {
                    return "";
                }
                return "*".repeat(input.length());
            }
            return "";
        }

        @Override
        public List<String> getArgumentNames() {
            return Collections.singletonList("input");
        }
    }

    /**
     * Escapa el texto para que pueda ir como valor de un atributo XML. Hace
     * falta porque el motor de plantillas va con el auto-escaping desactivado.
     */
    public static class EscapeXmlFunction implements Function {

        @Override
        public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (args.containsKey("input")) {
                Object input = args.get("input");
                if (input == null) {
                    return "";
                }
                return TextUtil.escapeXmlAttribute(input.toString());
            }
            return "";
        }

        @Override
        public List<String> getArgumentNames() {
            return Collections.singletonList("input");
        }
    }

    /**
     * Escapa el texto para que pueda ir dentro de un literal de cadena Java. Hace falta por lo
     * mismo que {@link EscapeXmlFunction}: el motor de plantillas va con el auto-escaping
     * desactivado.
     */
    public static class EscapeJavaFunction implements Function {

        @Override
        public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            if (args.containsKey("input")) {
                Object input = args.get("input");
                if (input == null) {
                    return "";
                }
                return TextUtil.escapeJavaString(input.toString());
            }
            return "";
        }

        @Override
        public List<String> getArgumentNames() {
            return Collections.singletonList("input");
        }
    }

}
