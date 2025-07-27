/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.viewprocessor;

import com.educaflow.common.buildtools.common.XMLUtil;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author logongas
 */
public class PanelFinder {
    
    private final Element formLocalTemplate;
    private final List<Element> formGeneralTemplates;
    
    
    PanelFinder(Element formLocalTemplate, List<Element> formGeneralTemplates) {
        this.formLocalTemplate=formLocalTemplate;
        this.formGeneralTemplates=formGeneralTemplates;
        
    }
    
    
    public Element findAndImport(String panelName,Document targetDocument) {
        Element panelElement=XMLUtil.getChildFilterByPrefixTagNameAndAttributeName(formLocalTemplate,panelName,"panel");

        if (panelElement==null) {
            for(Element formGeneralTemplate:formGeneralTemplates) {
                Element rawPanelElement=XMLUtil.getChildFilterByPrefixTagNameAndAttributeName(formGeneralTemplate,panelName,"panel");
                if (rawPanelElement!=null) {
                    if (panelElement==null) {
                        panelElement=rawPanelElement;
                    } else {
                        throw new RuntimeException("Existe más de un panel llamado '' en los ficheros de 'template-views.xml':"+panelName);
                    }
                    
                }
            }
        }
        
        if (panelElement==null) {
            throw new RuntimeException("No existe el panel con nombre:"+panelName);
        }
        
        return XMLUtil.importElement(panelElement,targetDocument);
        
    }
    
}
