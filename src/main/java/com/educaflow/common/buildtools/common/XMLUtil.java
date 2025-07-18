package com.educaflow.common.buildtools.common;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;

/**
 *
 * @author logongas
 */
public class XMLUtil {

    public static List<Element> getChilds(Element parentElement) {
        List<Element> elements = new ArrayList<>();

        NodeList children = parentElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) currentNode);

            }
        }
        return elements;
    }    
    
    public static Element getChildFilterByTagName(Element parentElement, String tagName) {
        List<Element> elements = getChildsFilterByTagName(parentElement, tagName);

        if (elements.size() == 1) {
            return elements.get(0);
        } else if (elements.size() == 0) {
            return null;
        } else {
            throw new RuntimeException("Hay mas de un elemento con tag:" + tagName);
        }

    }

    public static List<Element> getChildsFilterByTagName(Element parentElement, String tagName) {
        List<Element> childs = XMLUtil.getChilds(parentElement);
        List<Element> filter = new ArrayList<>();

        for (Element element : childs) {
            if (element.getTagName().equals(tagName)) {
                filter.add(element);
            }
        }
        return filter;
    }


    public static List<Element> getClonedChildElements(Element parentElement) {
        List<Element> childs = XMLUtil.getChilds(parentElement);
        List<Element> clonedElements = new ArrayList<>();

        for (Element element : childs) {
            clonedElements.add(XMLUtil.cloneElement((Element) element, (Element) element));
        }
        return clonedElements;
    }
    public static Element cloneElement(Element targetElement, Element sourceElement) {

        Element importedNode = (Element) targetElement.getOwnerDocument().importNode(sourceElement, true);

        return importedNode;
    }

    

    
    
    public static List<Element> getChildsFilterByTagNameAndAttributeName(Element parentElement, String name, String tagName) {
        List<Element> childs = XMLUtil.getChildsFilterByTagName(parentElement,tagName);
        List<Element> filter = new ArrayList<>();

        for (Element element : childs) {
            if (element.hasAttribute("name") && element.getAttribute("name").equals(name)) {
                filter.add(element);
            }
        }
        return filter;
    }

    public static Element getChildFilterByTagNameAndAttributeName(Element parentElement, String name, String tagName) {
        List<Element> elements = getChildsFilterByTagNameAndAttributeName(parentElement,name, tagName);

        if (elements.size() == 1) {
            return elements.get(0);
        } else if (elements.size() == 0) {
            return null;
        } else {
            throw new RuntimeException("Hay mas de un elemento con tag:" + tagName + " y atributo name="+ name);
        }
    }

    public static Document cloneDocument(Document originalDocument) {
        if (originalDocument == null) {
            return null;
        }


        Node clonedNode = originalDocument.cloneNode(true);

        if (clonedNode instanceof Document) {
            return (Document) clonedNode;
        } else {
            throw new IllegalStateException("Failed to clone Document. Cloned node is not a Document type.");
        }
    }



    public static NodeList getNodeListFromEvaluateXPath(String expression, Element element) {
        try {
            if (expression == null || expression.trim().isEmpty()) {
                throw new IllegalArgumentException("expression no puede ser null o vacio.");
            }
            if (element == null) {
                throw new IllegalArgumentException("element no puede ser null.");
            }
            if (expression.startsWith("//")) {
                throw new IllegalArgumentException("expression no puede empezar por //:" + expression);
            }

            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList result = (NodeList) xpath.evaluate(expression, element, XPathConstants.NODESET);

            return result;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating XPath expression: " + expression, e);
        }
    }
    
    public static Node getNodeFromEvaluateXPath(String expression, Element element) {
        try {
            if (expression == null || expression.trim().isEmpty()) {
                throw new IllegalArgumentException("expression no puede ser null o vacio.");
            }
            if (element == null) {
                throw new IllegalArgumentException("element no puede ser null.");
            }
            if (expression.startsWith("//")) {
                throw new IllegalArgumentException("expression no puede empezar por //:" + expression);
            }

            XPath xpath = XPathFactory.newInstance().newXPath();
            Node result = (Node) xpath.evaluate(expression, element, XPathConstants.NODE);

            return result;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating XPath expression: " + expression, e);
        }
    }
    public static void printNode(Node node) {
        if (node == null) {
            System.out.println("El documento es nulo y no puede ser impreso.");
            return;
        }
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); 
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); 


            DOMSource source = new DOMSource(node);
            StreamResult result = new StreamResult(System.out);


            transformer.transform(source, result);
            System.out.println(); 
        } catch (Exception e) {
            System.err.println("Error al imprimir el documento XML: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static Node getNodeFromString(Document document, String xmlFragmentString) {
        try {

            DocumentBuilderFactory fragmentDbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder fragmentDb = fragmentDbf.newDocumentBuilder();

            Document fragmentDoc = fragmentDb.parse(new InputSource(new StringReader(xmlFragmentString)));
            Node fragmentRootNode = fragmentDoc.getDocumentElement();


            Node importedNode = document.importNode(fragmentRootNode, true); 

            return importedNode;
        } catch (Exception ex) {
            throw new RuntimeException("Error al obtener un XML de:\n" + xmlFragmentString, ex);
        }        

    }
    
    
}
