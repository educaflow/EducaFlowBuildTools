package com.educaflow.common.buildtools.viewprocessor;

import com.educaflow.common.buildtools.common.TemplateUtil;
import com.educaflow.common.buildtools.common.XMLUtil;
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
public class XMLPreprocesor {

    private static final String ROOT_TAG_NAME = "object-views";

    public static Document process(Path filePath, Document document) {

        try {

            Document newDocument = XMLUtil.cloneDocument(document);

            List<Element> elementsStep = getFormElementsWithProfileStateAttributes(getRootElement(newDocument));
            for (Element elementStep : elementsStep) {

                doFormStateProfile(filePath, elementStep);

            }

            return newDocument;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void doFormStateProfile(Path filePath, Element formElement) {
        String profile = formElement.getAttribute("profile");
        String state = formElement.getAttribute("state");
        formElement.removeAttribute("profile");
        formElement.removeAttribute("state");

        String nombreExpediente = getNombreExpediente(formElement.getOwnerDocument());

        try {
            String nameAttributeValue = "exp-" + nombreExpediente + "-Base";
            String tagName = "form";

            Element baseElement = XMLUtil.getChildFilterByTagNameAndAttributeName(getRootElement(formElement.getOwnerDocument()), nameAttributeValue, tagName);
            if (baseElement == null) {
                throw new RuntimeException("No existe el nodo para el tag " + tagName + " y cuyo atributo name es  " + nameAttributeValue);
            }

       
            
            doMergeAttributes(formElement, baseElement);

            formElement.setAttribute("name", getFormName(nombreExpediente, state, profile));

            if (formElement.hasAttribute("title") == false) {
                formElement.setAttribute("title", StringUtil.getHumanNameFromExpedienteName(nombreExpediente));
            }
            if (baseElement.hasAttribute("title") == false) {
                baseElement.setAttribute("title", StringUtil.getHumanNameFromExpedienteName(nombreExpediente));
            }

            List<Element> hijos = XMLUtil.getChilds(formElement);
            if (hijos.size() != 3) {
                throw new RuntimeException("Debe tener 3 hijos pero solo tiene:" + hijos.size());
            }
            Element includePanelsElement = hijos.get(0);
            formElement.removeChild(includePanelsElement);
            if (includePanelsElement.getTagName().equals("include-panels") == false) {
                throw new RuntimeException("El primer hijo debe ser include-panels pero es:" + includePanelsElement.getTagName());
            }

            Element leftElement = hijos.get(1);
            formElement.removeChild(leftElement);
            if (leftElement.getTagName().equals("left") == false) {
                throw new RuntimeException("El segundo hijo debe ser left pero es:" + leftElement.getTagName());
            }

            Element rightElement = hijos.get(2);
            formElement.removeChild(rightElement);
            if (rightElement.getTagName().equals("right") == false) {
                throw new RuntimeException("El tercer hijo debe ser right pero es:" + rightElement.getTagName());
            }

            doIncludePanels(includePanelsElement, formElement, baseElement);
            doLeftRight(leftElement, rightElement, formElement);

        } catch (Exception ex) {
            throw new RuntimeException("step con profile=" + profile + " state=" + state, ex);
        }

    }

    private static void doIncludePanels(Element elementIncludePanel, Element formElement, Element baseElement) {
        String content = elementIncludePanel.getTextContent();
        String[] lines = content.split("\n");

        Element headElement = createElementPanelInclude(formElement.getOwnerDocument(), "exp-Expediente-Base-Head-form", null);
        formElement.appendChild(headElement);

        for (String line : lines) {
            String trimmedLine = line.trim();

            String includeName;
            boolean readonly;
            if (!trimmedLine.isEmpty()) {
                if (trimmedLine.startsWith("-")) {
                    readonly = true; // Si al menos uno empieza con '-', readonly es true
                    includeName = trimmedLine.substring(1); // Quita el guion
                } else {
                    readonly = false;
                    includeName = trimmedLine;
                }

                doInclude(formElement, "./*[@name='" + includeName + "']", baseElement, readonly);

            }
        }

        Element footerElement = createElementPanel(formElement.getOwnerDocument(), "exp-Expediente-Base-Footer-form");
        footerElement.setAttribute("itemSpan", "2");
        footerElement.setAttribute("colSpan", "12");

        formElement.appendChild(footerElement);

    }



    private static void doLeftRight(Element leftElement, Element rightElement, Element formElement) {
        Element cloneLeftElement = XMLUtil.cloneElement(formElement, leftElement);
        Element cloneRightElement = XMLUtil.cloneElement(formElement, rightElement);

        Element footerElement = (Element) XMLUtil.getNodeFromEvaluateXPath("./panel[@name='exp-Expediente-Base-Footer-form']", formElement);

        List<Element> childLeftElements = XMLUtil.getClonedChildElements(cloneLeftElement);
        List<Element> childRightElements = XMLUtil.getClonedChildElements(cloneRightElement);

        int sumColSpam = 0;
        {
            int itemSpan = 1;
            if (footerElement.hasAttribute("itemSpan")) {
                itemSpan = Integer.parseInt(footerElement.getAttribute("itemSpan"));
            }

            for (Element childElement : childLeftElements) {
                sumColSpam = sumColSpam + getColSpam(childElement, itemSpan);
            }
            for (Element childElement : childRightElements) {
                sumColSpam = sumColSpam + getColSpam(childElement, itemSpan);
            }
        }

        int colOffset = 12 - sumColSpam;

        if (childRightElements.size() > 0) {
            childRightElements.get(0).setAttribute("colOffset", colOffset + "");
        }

        for (Element childElement : childLeftElements) {
            footerElement.appendChild(childElement);
        }
        for (Element childElement : childRightElements) {
            footerElement.appendChild(childElement);
        }

        String xmlErrorMessages = getFileContent("view-error-messages.template");
        Node errorMessages = XMLUtil.getNodeFromString(footerElement.getOwnerDocument(), xmlErrorMessages);

        Node primerHijo = footerElement.getFirstChild();
        if (primerHijo != null) {
            footerElement.insertBefore(errorMessages, primerHijo);
        } else {
            footerElement.appendChild(errorMessages);
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

    private static void doInclude(Element formElement, String xpathTarget, Element baseElement, boolean readOnly) {
        try {

            NodeList targetElementsToInclude = XMLUtil.getNodeListFromEvaluateXPath(xpathTarget, baseElement);

            if (targetElementsToInclude == null) {
                throw new RuntimeException("Con xpathTarget='" + xpathTarget + "' targetElementsToInclude es null con respecto a tag '" + baseElement.getTagName() + "'.");
            }
            if (targetElementsToInclude.getLength() != 1) {
                throw new RuntimeException("Con xpathTarget='" + xpathTarget + "' deberia tener solo un elemento pero tiene" + targetElementsToInclude.getLength());
            }

            Element newElementIncluded = (Element) XMLUtil.cloneElement(formElement, (Element) targetElementsToInclude.item(0));
            formElement.appendChild(newElementIncluded);

            if (readOnly == true) {
                String fieldsExpresionXPath = ".//field";
                NodeList fields = XMLUtil.getNodeListFromEvaluateXPath(fieldsExpresionXPath, newElementIncluded);
                for (int i = 0; i < fields.getLength(); i++) {
                    Node fieldNode = fields.item(i);
                    if (fieldNode.getNodeType() != Node.ELEMENT_NODE) {
                        throw new RuntimeException("Target node found by xpathTarget '" + fieldsExpresionXPath + "' is not an Element. Cannot modify attributes on it.");
                    }

                    Element fieldElement = (Element) fieldNode;
                    fieldElement.setAttribute("readonly", "true");

                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("xpathTarget=" + xpathTarget + " readOnly=" + readOnly, ex);
        }
    }

    
    /*******************************************************************************************/
    /*******************************************************************************************/
    /************************** Métodos de utilidad ********************************************/
    /*******************************************************************************************/
    /*******************************************************************************************/
    
    public static List<Element> getFormElementsWithProfileStateAttributes(Element parentElement) {
        List<Element> childs = XMLUtil.getChildsFilterByTagName(parentElement, "form");
        List<Element> filter = new ArrayList<>();

        for (Element formElement : childs) {
            if (formElement.hasAttribute("state")) {
                filter.add((Element) formElement);
            }
        }

        return filter;

    }

    private static int getColSpam(Element element, int defaultColSpam) {
        if (element.hasAttribute("colSpan")) {
            return Integer.parseInt(element.getAttribute("colSpan"));
        } else {
            return defaultColSpam;
        }
    }

    private static String getNombreExpediente(Document document) {
        String nombreExpediente = null;

        List<Element> elements = XMLUtil.getChildsFilterByTagName(document.getDocumentElement(), "form");

        Pattern pattern = Pattern.compile("exp-([a-zA-Z0-9]+)-Base");

        for (Element element : elements) {
            Matcher matcher = pattern.matcher(element.getAttribute("name"));
            if (matcher.find()) {

                if (nombreExpediente == null) {
                    nombreExpediente = matcher.group(1);
                } else {
                    throw new RuntimeException("Existen al menos 2 nodos base:" + nombreExpediente + " y " + matcher.group(1));
                }
            }
        }

        return nombreExpediente;

    }

    private static Element createElementPanelInclude(Document document, String viewValue, String fromValue) {
        Element panelInclude = document.createElement("panel-include");

        if (viewValue != null) {
            panelInclude.setAttribute("view", viewValue);
        }

        if (fromValue != null) {
            panelInclude.setAttribute("from", fromValue);
        }

        return panelInclude;
    }

    private static Element createElementPanel(Document document, String name) {
        Element panel = document.createElement("panel");

        panel.setAttribute("name", name);

        return panel;
    }

    private static Element getRootElement(Document document) {
        NodeList objectViewsList = document.getElementsByTagName(ROOT_TAG_NAME);
        if (objectViewsList.getLength() == 0) {
            return null;
        }
        Node objectViewsNode = objectViewsList.item(0);

        if (objectViewsNode.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        Element objectViewsElement = (Element) objectViewsNode;

        return objectViewsElement;
    }

    private static String getFileContent(String resourcePath) {
        ClassLoader classLoader = XMLPreprocesor.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Recurso no encontrado: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getFormName(String nombreExpediente, String state, String profile) {
        if ((profile == null) || (profile.trim().isEmpty())) {
            return "exp-" + nombreExpediente + "-" + state + "-form";
        } else {
            return "exp-" + nombreExpediente + "-" + state + "-" + profile + "-form";
        }
    }

}
