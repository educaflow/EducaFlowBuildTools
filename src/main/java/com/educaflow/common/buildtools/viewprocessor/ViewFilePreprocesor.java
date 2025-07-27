package com.educaflow.common.buildtools.viewprocessor;

import com.educaflow.common.buildtools.common.TextUtil;
import com.educaflow.common.buildtools.common.XMLUtil;
import com.educaflow.common.buildtools.viewprocessor.tags.Footer;
import com.educaflow.common.buildtools.viewprocessor.tags.Form;
import com.educaflow.common.buildtools.viewprocessor.tags.IncludePanels;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author logongas
 */
public class ViewFilePreprocesor {


    public static Document process(Document document, List<Element> templateForms) {
        Document newDocument = XMLUtil.cloneDocument(document);

        Element formTipoExpedienteTemplate=getTemplateForm(newDocument);

        PanelFinder panelFinder=new PanelFinder(formTipoExpedienteTemplate,templateForms);



        List<Element> includePanelsList=XMLUtil.getElementsFromEvaluateXPath(".//include-panels", newDocument.getDocumentElement());
        for(Element element:includePanelsList) {
            IncludePanels.doIncludePanels(element, panelFinder);
        }

        List<Element> footerList=XMLUtil.getElementsFromEvaluateXPath(".//footer", newDocument.getDocumentElement());
        for(Element element:footerList) {
            Footer.doFooter(element, panelFinder);
        }

        List<Element> formElements = getFormElementsWithProfileStateAttributes(newDocument.getDocumentElement());
        for (Element formElement : formElements) {

            Form.doForm(formElement);

        }

        return newDocument;
    }





    
    private static List<Element> getFormElementsWithProfileStateAttributes(Element parentElement) {
        List<Element> childs = XMLUtil.getChildsFilterByTagName(parentElement, "form");
        List<Element> filter = new ArrayList<>();

        for (Element formElement : childs) {
            if (formElement.hasAttribute("state")) {
                filter.add((Element) formElement);
            }
        }

        return filter;

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
    



}
