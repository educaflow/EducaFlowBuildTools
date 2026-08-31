package com.educaflow.common.buildtools.files.views;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.Fase;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Generador del {@code views.xml} de <b>una fase</b>, que vive en
 * {@code <vN>/<fase en minúsculas>/views.xml} y contiene los {@code <form state="...">} de los
 * estados de esa fase.
 *
 * <p>En el atributo {@code state} va el nombre <b>corto</b> del estado, igual que en el
 * {@code TipoExpedienteInstance.xml}: la fase la deduce el {@code viewprocessor} de la carpeta en
 * la que está el fichero, y es él quien compone el nombre real para el {@code name} de la vista
 * generada.
 *
 * <p>Este fichero <b>no</b> lleva form de plantilla: los paneles salen del {@code views.xml} de la
 * raíz de la versión (ver {@link ViewsFile}).
 *
 * @author logongas
 */
public class ViewsFaseFile {

    private final Fase fase;
    private final Path path;

    public ViewsFaseFile(Path path, Fase fase) {
        this.path = path;
        this.fase = fase;
    }

    /**
     * Crea el fichero solo si no existe (nunca pisa fuentes editadas a mano).
     *
     * @return true si lo ha creado, false si ya existía.
     */
    public boolean createViewsFaseFileIfNotExists() {
        if (Files.exists(path) == false) {
            createViewsFaseFile(path);
            return true;
        }
        return false;
    }

    public Path getPath() {
        return path;
    }

    public Fase getFase() {
        return fase;
    }

    private void createViewsFaseFile(Path path) {
        Map<String, Object> context = new HashMap<>();
        context.put("states", fase.getStates());
        context.put("fase", fase);

        String content = TemplateUtil.evaluateTemplate("views-fase.template", context);

        TemplateUtil.createFileWithContent(path, content);
    }
}
