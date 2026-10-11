/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.viewprocessor;

import com.educaflow.common.buildtools.common.XMLUtil;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Resuelve el nombre de un panel de un {@code <include-panels>} al panel del
 * que procede, de donde sale el que se clona al documento que se está
 * preprocesando.
 *
 * El nombre puede venir cualificado con la carpeta bajo {@code shared} en la
 * que vive su {@code common-template-views.xml}, con puntos como separadores y el
 * panel como último segmento:
 *
 * <ul>
 *   <li>{@code aaa} → el panel {@code aaa} del {@code common-template-views.xml} de
 *       la raíz de {@code shared}</li>
 *   <li>{@code bbb.eee} → el panel {@code eee} del
 *       {@code common-template-views.xml} de {@code <shared>/bbb}</li>
 *   <li>{@code bbb.ccc.ddd} → el panel {@code ddd} del
 *       {@code common-template-views.xml} de {@code <shared>/bbb/ccc}</li>
 * </ul>
 *
 * Un nombre sin puntos se busca antes en el form plantilla local de la
 * versión ({@code exp-<Code>-Templates}); si no está ahí, en el de la raíz de
 * {@code shared}. Un nombre punteado solo se busca en {@code shared}: el form
 * plantilla local no participa. Un nombre que empieza por
 * {@code subsysExpedientes} solo se busca en el form
 * {@code subsysExpedientes-system-templates-form} del
 * {@code shared/system-template-views.xml}.
 */
public class PanelFinder {
    
    /**
     * Prefijo de los paneles del subsistema de expedientes: los que viven en el
     * form {@code subsysExpedientes-system-templates-form} del
     * {@code shared/system-template-views.xml}.
     */
    private static final String PREFIJO_PANEL_SUBSISTEMA="subsysExpedientes";
    
    private final Element formLocalTemplate;
    private final Map<String, Element> formTemplatesPorCarpeta;
    private final Element formSystemTemplate;
    
    
    PanelFinder(Element formLocalTemplate, Map<String, Element> formTemplatesPorCarpeta, Element formSystemTemplate) {
        this.formLocalTemplate = formLocalTemplate;
        this.formTemplatesPorCarpeta = formTemplatesPorCarpeta;
        this.formSystemTemplate = formSystemTemplate;
    }
    
    
    public Element findAndImport(String panelName,Document targetDocument) {
        Element panelElement;
        
        if (panelName.startsWith(PREFIJO_PANEL_SUBSISTEMA)) {
            panelElement = findPanelEnSystem(panelName);
        } else if (panelName.contains(".")) {
            panelElement = findPanelEnShared(panelName);
        } else {
            panelElement = XMLUtil.getChildFilterByPrefixTagNameAndAttributeName(formLocalTemplate, panelName, "panel");
            
            if (panelElement == null) {
                panelElement = findPanelEnShared(panelName);
            }
        }

        return XMLUtil.importElement(panelElement,targetDocument);
        
    }

    
    /**
     * Busca el panel del subsistema de expedientes (nombre que empieza por
     * {@code subsysExpedientes}) en el form
     * {@code subsysExpedientes-system-templates-form} del
     * {@code shared/system-template-views.xml}.
     */
    private Element findPanelEnSystem(String panelName) {
        if (panelName.contains(".")) {
            throw new RuntimeException("El panel del subsistema:" + panelName + " no puede venir cualificado con carpeta: todos viven en el " + "system-template-views.xml" + " de la raiz de shared");
        }
        
        Element panelElement = XMLUtil.getChildFilterByPrefixTagNameAndAttributeName(formSystemTemplate, panelName, "panel");
        
        if (panelElement == null) {
            throw new RuntimeException("No existe el panel del subsistema con nombre:" + panelName);
        }
        
        return panelElement;
    }
    
    /**
     * Busca el panel de un nombre cualificado (o simple, si ya se ha visto que
     * no está en el form plantilla local) en su {@code common-template-views.xml} de
     * {@code shared}.
     *
     * Todo menos el último segmento es la carpeta bajo {@code shared}; el
     * último es el nombre del panel dentro del form
     * {@code subsysExpedientes-common-templates-form} de esa carpeta.
     */
    private Element findPanelEnShared(String panelName) {
        int puntoDelPanel = panelName.lastIndexOf('.');
        String directoryPath = (puntoDelPanel < 0) ? "" : panelName.substring(0, puntoDelPanel).replace('.', '/');
        String realPanelName = (puntoDelPanel < 0) ? panelName : panelName.substring(puntoDelPanel + 1);
        
        Element formTemplate = formTemplatesPorCarpeta.get(directoryPath);
        if (formTemplate == null) {
            throw new RuntimeException("No existe el common-template-views.xml de la carpeta de shared:" + directoryPath + " al que pertenece el panel:" + panelName);
        }
        
        Element panelElement = XMLUtil.getChildFilterByPrefixTagNameAndAttributeName(formTemplate, realPanelName, "panel");
        
        if (panelElement == null) {
            throw new RuntimeException("No existe el panel con nombre:" + panelName);
        }
        
        return panelElement;
    }

}