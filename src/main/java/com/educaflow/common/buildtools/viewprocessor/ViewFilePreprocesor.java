package com.educaflow.common.buildtools.viewprocessor;

import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.Fase;
import com.educaflow.common.buildtools.files.tramite.TramitesLayout;
import com.educaflow.common.buildtools.viewprocessor.tags.Footer;
import com.educaflow.common.buildtools.viewprocessor.tags.Form;
import com.educaflow.common.buildtools.viewprocessor.tags.IncludePanels;
import java.nio.file.Path;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author logongas
 */
public class ViewFilePreprocesor {

    /**
     * @param filePath ruta del fichero de vistas que se está procesando. Hace falta porque el
     *        {@code views.xml} de una fase no es autosuficiente: su fase y sus paneles se resuelven
     *        a partir de dónde está (ver {@link TipoExpedienteViewsContext}).
     */
    public static Document process(Document document, Path filePath, List<Element> templateForms, TramitesLayout tramitesLayout) {
        Document newDocument = XMLUtil.cloneDocument(document);

        List<Element> formElements = TipoExpedienteViewsContext.getFormElementsWithStateAttribute(newDocument.getDocumentElement());
        Element formTipoExpedienteTemplate = TipoExpedienteViewsContext.findTemplateFormEnDocumento(newDocument, filePath);

        if (formElements.isEmpty() && (formTipoExpedienteTemplate == null)) {
            //No tiene nada de un tipo de expediente: es la inmensa mayoría de las vistas del
            //proyecto, que se copian tal cual.
            return newDocument;
        }

        Fase fase = null;
        if (formElements.isEmpty() == false) {
            //Es el views.xml de una fase: su fase sale de la carpeta y, si no trae plantilla propia
            //(que es lo normal), sus paneles salen del views.xml de la raíz de la versión.
            TipoExpedienteViewsContext contexto = TipoExpedienteViewsContext.of(filePath, tramitesLayout);
            fase = contexto.getFase();

            if (formTipoExpedienteTemplate == null) {
                formTipoExpedienteTemplate = contexto.getTemplateForm();
            }
        }

        PanelFinder panelFinder = new PanelFinder(formTipoExpedienteTemplate, templateForms);

        List<Element> includePanelsList = XMLUtil.getElementsFromEvaluateXPath(".//include-panels", newDocument.getDocumentElement());
        for (Element element : includePanelsList) {
            IncludePanels.doIncludePanels(element, panelFinder);
        }

        List<Element> footerList = XMLUtil.getElementsFromEvaluateXPath(".//footer", newDocument.getDocumentElement());
        for (Element element : footerList) {
            Footer.doFooter(element, panelFinder);
        }

        for (Element formElement : formElements) {
            Form.doForm(formElement, formTipoExpedienteTemplate, fase);
        }

        return newDocument;
    }

}
