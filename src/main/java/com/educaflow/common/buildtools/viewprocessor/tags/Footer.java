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
        
        Element footerPanel = panelFinder.findAndImport("exp-Expediente-footer",ownerDocument);

        
        int sumColSpam = 0;
        {
            int itemSpan = XMLUtil.getIntegerAttribute(footerPanel,"itemSpan", 1);


            for (Element childElement : childButtonsLeftElements) {
                sumColSpam = sumColSpam + XMLUtil.getIntegerAttribute(childElement,"colSpan", itemSpan);
            }
            for (Element childElement : childButtonsRightElements) {
                sumColSpam = sumColSpam + XMLUtil.getIntegerAttribute(childElement,"colSpan", itemSpan);
            }
        }

        int colOffset = 12 - sumColSpam;

        if (childButtonsRightElements.size() > 0) {
            childButtonsRightElements.get(0).setAttribute("colOffset", colOffset + "");
        }
        
        for (Element childElement : childButtonsLeftElements) {
            footerPanel.appendChild(childElement);
        }
        for (Element childElement : childButtonsRightElements) {
            footerPanel.appendChild(childElement);
        }
        
        
        Node parent = footer.getParentNode();
        parent.insertBefore(footerPanel,footer);
        parent.removeChild(footer);
        
    }
    
    
  

}
