/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.viewprocessor.tags;

import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.viewprocessor.PanelFinder;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author logongas
 */
public class Footer {

    private static final int BUTTONS_PANEL_COLS = 6;
    private static final int BUTTONS_PANEL_ITEM_SPAN = 3;
    
    public static void doFooter(Element footer, PanelFinder panelFinder) {
        Document ownerDocument=footer.getOwnerDocument();
        
        Element buttonsLeftElement=XMLUtil.getChildFilterByTagName(footer, "buttons-left");
        Element buttonsRightElements=XMLUtil.getChildFilterByTagName(footer, "buttons-right");
        
        
        List<Element> childButtonsLeftElements=new ArrayList<>();
        List<Element> childButtonsRightElements=new ArrayList<>();
        
        if (buttonsLeftElement!=null) {
            childButtonsLeftElements =XMLUtil.importElements(XMLUtil.getChilds(buttonsLeftElement),ownerDocument);
        }
        if (buttonsRightElements!=null) {
            childButtonsRightElements = XMLUtil.importElements(XMLUtil.getChilds(buttonsRightElements),ownerDocument);
        }
        
        Element footerPanel = panelFinder.findAndImport("subsysExpedientes-template-footer-panel",ownerDocument);

        
        Element panelLeft = createButtonsPanel(footerPanel);
        Element panelRight = createButtonsPanel(footerPanel);

        for (Element button : childButtonsLeftElements) {
            panelLeft.appendChild(button);
        }
        for (Element button : childButtonsRightElements) {
            int colSpan = XMLUtil.getIntegerAttribute(button, "colSpan", BUTTONS_PANEL_ITEM_SPAN);
            button.setAttribute("colOffset", (BUTTONS_PANEL_COLS - colSpan) + "");
            panelRight.appendChild(button);
        }

        footerPanel.appendChild(panelLeft);
        footerPanel.appendChild(panelRight);


        Node parent = footer.getParentNode();
        parent.insertBefore(footerPanel,footer);
        if (isFooterDeFormDeEstado(footer)) {
            Element notasPanel = panelFinder.findAndImport("subsysExpedientes-template-notas-panel",ownerDocument);
            parent.insertBefore(notasPanel,footer);
        }
        parent.removeChild(footer);

    }

    /**
     * Cada lado del footer va en su propio panel sin marco de media anchura, así que lo de un lado no desplaza
     * al otro y no hace falta saber qué botones se ven (eso depende de los {@code showIf}, que solo se sabe en
     * runtime). Tiene {@value #BUTTONS_PANEL_COLS} columnas para que el {@code colSpan} de un botón mida lo mismo
     * que si estuviera directamente en el form. Como en cada lado solo se ve un botón a la vez, a los de la
     * derecha les basta un {@code colOffset} fijo para quedar pegados al margen derecho; un botón oculto no
     * ocupa sitio porque Axelor oculta su celda vacía y el siguiente sube a esa fila.
     */
    private static Element createButtonsPanel(Element footerPanel) {
        Document document = footerPanel.getOwnerDocument();
        String namespace = footerPanel.getNamespaceURI();
        Element panel = (namespace == null) ? document.createElement("panel") : document.createElementNS(namespace, "panel");
        panel.setAttribute("colSpan", BUTTONS_PANEL_COLS + "");
        panel.setAttribute("cols", BUTTONS_PANEL_COLS + "");
        panel.setAttribute("itemSpan", BUTTONS_PANEL_ITEM_SPAN + "");
        panel.setAttribute("showFrame", "false");

        return panel;
    }

    /**
     * El panel de notas es del expediente, así que solo va tras el footer de un {@code <form state=...>}.
     * Un {@code <footer>} de cualquier otro form (el de una entidad hija) no lo lleva: su modelo no es
     * un expediente.
     */
    private static boolean isFooterDeFormDeEstado(Element footer) {
        Node parent = footer.getParentNode();

        return (parent instanceof Element) && ((Element) parent).hasAttribute("state");
    }
    
    
  

}
