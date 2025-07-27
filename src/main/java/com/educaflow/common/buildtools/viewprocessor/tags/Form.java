/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.viewprocessor.tags;

import com.educaflow.common.buildtools.common.TextUtil;
import com.educaflow.common.buildtools.common.XMLUtil;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author logongas
 */
public class Form {
    
    

    public static void doForm(Element formElement) {
        Element templateForm=getTemplateForm(formElement.getOwnerDocument());
        String nombreExpediente = getNombreExpediente(formElement.getOwnerDocument());
        
        String profile = formElement.getAttribute("profile");
        String state = formElement.getAttribute("state");
        formElement.removeAttribute("profile");
        formElement.removeAttribute("state");

        try {
            
            doMergeAttributes(formElement, templateForm);

            formElement.setAttribute("name", getFormName(nombreExpediente, state, profile));

            if (formElement.hasAttribute("title") == false) {
                formElement.setAttribute("title", TextUtil.getHumanNameFromExpedienteName(nombreExpediente));
            }
        } catch (Exception ex) {
            throw new RuntimeException("step con profile=" + profile + " state=" + state, ex);
        }

    }


    private static void doMergeAttributes(Element mainElementForIncludesAndExtends, Element baseElement) {
        for (int i = 0; i < baseElement.getAttributes().getLength(); i++) {
            Node attribute = baseElement.getAttributes().item(i);

            if (mainElementForIncludesAndExtends.hasAttribute(attribute.getNodeName()) == false) {
                //Si no existe lo añadimos
                mainElementForIncludesAndExtends.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
            } else if (mainElementForIncludesAndExtends.getAttribute(attribute.getNodeName()).isBlank()) {
                //Si existe en los dos pero es vacio en el destino, es que hay que borrarlo
                mainElementForIncludesAndExtends.removeAttribute(attribute.getNodeName());
            }
        }
    }





    private static Element getTemplateForm(Document document) {
        Element templateForm=null;

        List<Element> elements = XMLUtil.getChildsFilterByTagName(document.getDocumentElement(), "form");

        Pattern pattern = Pattern.compile("exp-([a-zA-Z0-9]+)-Templates");

        for (Element element : elements) {
            Matcher matcher = pattern.matcher(element.getAttribute("name"));
            if (matcher.find()) {

                if (templateForm == null) {
                    templateForm = element;
                } else {
                    throw new RuntimeException("Existen al menos 2 nodos de plantilla:" + element.getAttribute("name"));
                }
            }
        }

        return templateForm;
    }
    

    private static String getNombreExpediente(Document document) {
        String nombreExpediente;
        Element templateForm=getTemplateForm(document);


        Pattern pattern = Pattern.compile("exp-([a-zA-Z0-9]+)-Templates");
        Matcher matcher = pattern.matcher(templateForm.getAttribute("name"));
            if (matcher.find()) {
                nombreExpediente = matcher.group(1);
            } else {
                throw new RuntimeException("El atributo 'name'  del templateForm no es válido" + templateForm.getAttribute("name"));
            }


        return nombreExpediente;

    }

    private static String getFormName(String nombreExpediente, String state, String profile) {
        if ((profile == null) || (profile.trim().isEmpty())) {
            return "exp-" + nombreExpediente + "-" + state + "-form";
        } else {
            return "exp-" + nombreExpediente + "-" + state + "-" + profile + "-form";
        }
    }    
    
}
