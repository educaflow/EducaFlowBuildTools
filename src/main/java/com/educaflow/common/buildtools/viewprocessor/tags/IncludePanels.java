/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.viewprocessor.tags;

import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.viewprocessor.PanelFinder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author logongas
 */
public class IncludePanels {
    
    
    public static void doIncludePanels(Element includePanelsElement, PanelFinder panelFinder) {
        String content = includePanelsElement.getTextContent();
        String[] lines = content.split("\n");
        boolean includeHeader=XMLUtil.getBooleanAttribute(includePanelsElement, "header", true);
        Map<String,Boolean> panels=new LinkedHashMap<>();
        
        
        if (includeHeader) {
            panels.put("exp-Expediente-header",false);
        }
        
        for (String line : lines) {
            String trimmedLine = line.trim();

            String panelName;
            boolean readonly;
            if (!trimmedLine.isEmpty()) {
                if (trimmedLine.startsWith("-")) {
                    readonly = true; // Si al menos uno empieza con '-', readonly es true
                    panelName = trimmedLine.substring(1); // Quita el guion
                } else {
                    readonly = false;
                    panelName = trimmedLine;
                }
                
                panels.put(panelName,readonly);

            }
        }
        
        
        replaceWithPanels(includePanelsElement,panels,panelFinder);
        
    }
    
    
    public static void replaceWithPanels(Element originalElement, Map<String,Boolean> panels,PanelFinder panelFinder) {

        Node parent = originalElement.getParentNode();
        if (parent == null) {
            throw new IllegalArgumentException("El elemento original no tiene nodo padre.");
        }

        Document ownerDocument = originalElement.getOwnerDocument();

        // Insertar los nuevos elementos antes del original
        for (Entry<String,Boolean> entry : panels.entrySet()) {
            String panelName=entry.getKey();
            boolean readOnly=entry.getValue();
            
            Element panelElement=panelFinder.findAndImport(panelName,ownerDocument);
            
            
            if (readOnly == true) {
                List<Element> fields = XMLUtil.getElementsFromEvaluateXPath(".//field", panelElement);
                for (Element fieldElement: fields) {
                    fieldElement.setAttribute("readonly", "true");

                }
            }
            
            parent.insertBefore(panelElement, originalElement);
        }

        parent.removeChild(originalElement);
    }    
    
    
}
