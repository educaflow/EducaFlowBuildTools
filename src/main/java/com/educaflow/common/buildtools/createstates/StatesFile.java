package com.educaflow.common.buildtools.createstates;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFileFinder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Emite el {@code States.java} de un tipo de expediente en la raíz de salida. */
public class StatesFile {

    private static final String TEMPLATE = "states.template";
    /** El nombre simple de la clase generada. Package-private para que lo comparta {@link IdentificadoresGenerados}. */
    static final String CLASS_NAME = "States";

    /** {@code import a.b.C;} → {@code C}. De aquí sale la reserva de nombres de tipo anidado. */
    private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.]+)\\s*;");

    private final TipoExpedienteInstanceFile tipoExpediente;
    private final Path rootPathDestino;
    private final ProfilesDelDominio profiles;

    public StatesFile(TipoExpedienteInstanceFile tipoExpediente, Path rootPathDestino, ProfilesDelDominio profiles) {
        this.tipoExpediente = tipoExpediente;
        this.rootPathDestino = rootPathDestino;
        this.profiles = profiles;
    }

    public void generateStates() {
        try {
            // Todo lo que puede fallar, falla ANTES de escribir nada.
            TipoExpedienteInstanceFileFinder.checkProfiles(tipoExpediente, profiles);
            IdentificadoresGenerados.check(tipoExpediente, nombresImportadosPorLaPlantilla());

            Map<String, Object> context = new HashMap<>();
            context.put("tipoExpediente", tipoExpediente);
            context.put("packageName", tipoExpediente.getBasePackageName());
            context.put("className", CLASS_NAME);
            context.put("newLine", "\n");

            String content = TemplateUtil.evaluateTemplate(TEMPLATE, context);

            Path destino = rootPathDestino
                    .resolve(tipoExpediente.getBasePackageName().replace('.', '/'))
                    .resolve(CLASS_NAME + ".java");

            Files.createDirectories(destino.getParent());
            // No se usa TemplateUtil.createFileWithContent: ese exige que el fichero NO exista, que
            // es la semántica de un esqueleto. States se reemite entero en cada build.
            Files.writeString(destino, content, StandardCharsets.UTF_8);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Fallo al generar el States de " + tipoExpediente.getCode(), ex);
        }
    }

    /**
     * Los nombres simples que importa la plantilla. Se derivan de la propia plantilla y no se
     * mantienen en una lista aparte: si states.template cambia sus imports, la reserva cambia sola
     * (regla 8).
     */
    private Set<String> nombresImportadosPorLaPlantilla() throws Exception {
        try (InputStream in = StatesFile.class.getClassLoader().getResourceAsStream(TEMPLATE)) {
            if (in == null) {
                throw new RuntimeException("No se encuentra la plantilla " + TEMPLATE + " en el classpath.");
            }

            String plantilla = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            Set<String> nombres = new LinkedHashSet<>();
            Matcher matcher = IMPORT.matcher(plantilla);
            while (matcher.find()) {
                String fqcn = matcher.group(1);
                nombres.add(fqcn.substring(fqcn.lastIndexOf('.') + 1));
            }

            return nombres;
        }
    }
}
