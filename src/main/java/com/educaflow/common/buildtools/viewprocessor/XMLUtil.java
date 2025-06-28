package com.educaflow.common.buildtools.viewprocessor;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter; // To capture output as a string if needed
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author logongas
 */
public class XMLUtil {

    public static Element findNodeByTagName(Document doc, String rootTagName, String name, String tagName) {

        NodeList objectViewsList = doc.getElementsByTagName(rootTagName);
        if (objectViewsList.getLength() == 0) {
            return null;
        }
        Node objectViewsNode = objectViewsList.item(0);

        if (objectViewsNode.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        Element objectViewsElement = (Element) objectViewsNode;

        NodeList children = objectViewsElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = (Element) currentNode;

                if (currentElement.getTagName().equals(tagName)) {

                    if (currentElement.hasAttribute("name") && currentElement.getAttribute("name").equals(name)) {
                        return (Element) currentNode; // Found the node!
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds and returns a list of all direct child nodes of "object-views" that
     * have the "Inherit" attribute.
     *
     * @param doc The XML Document to search within.
     * @return A List of Node objects that are children of "object-views" and
     * have the "Inherit" attribute. Returns an empty list if "object-views" is
     * not found or no matching nodes are found.
     */
    public static List<Element> getElementsWithIncludeAttribute(Document doc, String rootTagName) {
        List<Element> inheritNodes = new ArrayList<>();

        NodeList objectViewsList = doc.getElementsByTagName(rootTagName);
        if (objectViewsList.getLength() == 0) {
            return inheritNodes;
        }
        Node objectViewsNode = objectViewsList.item(0);

        if (objectViewsNode.getNodeType() != Node.ELEMENT_NODE) {
            return inheritNodes;
        }
        Element objectViewsElement = (Element) objectViewsNode;

        NodeList children = objectViewsElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = (Element) currentNode;

                if (currentElement.hasAttribute("include")) {
                    inheritNodes.add((Element) currentNode);
                }
            }
        }
        return inheritNodes;
    }

    public static List<Element> getInclude(Element parentElement) {
        List<Element> includeElements = new ArrayList<>();

        if (parentElement == null) {
            throw new RuntimeException("El parametro parentelement es null");
        }

        NodeList children = parentElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = (Element) node;

                if (currentElement.getNodeName().equals("include")) {
                    // Validar la combinación de atributos
                    boolean hasTarget = currentElement.hasAttribute("target");
                    boolean hasSourceFileName = currentElement.hasAttribute("sourceFileName");
                    boolean hasReadOnly = currentElement.hasAttribute("readOnly");
                    int numAttributes = currentElement.getAttributes().getLength();

                    if (hasTarget && !hasSourceFileName && numAttributes == 1) {
                        includeElements.add(currentElement);
                    } else if (hasTarget && hasSourceFileName && numAttributes == 2) {
                        includeElements.add(currentElement);
                    } else if (hasTarget && hasReadOnly && numAttributes == 2) {
                        includeElements.add(currentElement);
                    } else if (hasTarget && hasSourceFileName && hasReadOnly && numAttributes == 3) {
                        includeElements.add(currentElement);                        
                    } else {
                        String errorMessage = "El elemento <include> tiene una combinación de atributos no permitida. ";
                        if (!hasTarget) {
                            errorMessage += "Falta el atributo obligatorio 'target'. ";
                        }
                        errorMessage += "Atributos encontrados: [";
                        for (int k = 0; k < currentElement.getAttributes().getLength(); k++) {
                            errorMessage += currentElement.getAttributes().item(k).getNodeName();
                            if (k < currentElement.getAttributes().getLength() - 1) {
                                errorMessage += ", ";
                            }
                        }
                        errorMessage += "]. Solo se permiten 'target' o 'target' y 'file'.";
                        throw new IllegalArgumentException(errorMessage);
                    }
                }

                try {
                    includeElements.addAll(getInclude(currentElement));
                } catch (IllegalArgumentException e) {
                    // Propagar la excepción hacia arriba si un descendiente falla la validación
                    throw e;
                }
            }
        }
        return includeElements;
    }

    public static List<Element> getExtends(Element parentElement) {
        List<Element> extendElements = new ArrayList<>();
        NodeList children = parentElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {

                Element currentElement = (Element) currentNode;
                if ("extend".equals(currentElement.getTagName())) {
                    // Check if it has ONLY the "target" attribute
                    if (currentElement.hasAttribute("target") && currentElement.getAttributes().getLength() == 1) {
                        extendElements.add(currentElement);
                    } else {
                        throw new RuntimeException("Found 'extend' element with invalid attributes. Expected only 'target'. Element: " + currentElement.getTagName());
                    }
                }

                try {
                    extendElements.addAll(getExtends(currentElement));
                } catch (IllegalArgumentException e) {
                    // Propagar la excepción hacia arriba si un descendiente falla la validación
                    throw e;
                }

            }
        }
        return extendElements;
    }

    public static Element getChildElementUniqueByTagName(Element parentElement, String tagName) {
        if (parentElement == null) {
            throw new IllegalArgumentException("parentElement cannot be null");
        }
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("tagName cannot be null or empty.");
        }

        Element foundElement = null;
        NodeList children = parentElement.getChildNodes();
        int elementChildCount = 0; // To count actual Element nodes

        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);

            switch (currentNode.getNodeType()) {
                case Node.ELEMENT_NODE:
                    elementChildCount++;
                    Element currentElement = (Element) currentNode;
                    if (currentElement.getTagName().equals(tagName)) {
                        if (foundElement != null) {
                            // This means we've already found one matching element, and now found another.
                            // This violates the "only one Element child" rule if both match the tagName
                            // or if we already found the target and now found another element (even if it doesn't match tagName).
                            throw new IllegalStateException(
                                    "Parent element '" + parentElement.getTagName()
                                    + "' contains more than one Element child. Expected only one with tag '" + tagName + "'."
                            );
                        }
                        foundElement = currentElement;
                    } else {
                        return null;
                    }
                    break;
                case Node.TEXT_NODE:
                    // Allow whitespace text nodes, but throw an error if it's non-whitespace text
                    if (!currentNode.getNodeValue().trim().isEmpty()) {
                        throw new IllegalStateException(
                                "Parent element '" + parentElement.getTagName()
                                + "' contains unexpected non-whitespace text node: '" + currentNode.getNodeValue().trim() + "'."
                        );
                    }
                    break;
                case Node.COMMENT_NODE:
                    // Allow comment nodes, do nothing
                    break;
                default:
                    // Disallow other node types explicitly
                    throw new IllegalStateException(
                            "Parent element '" + parentElement.getTagName()
                            + "' contains an unexpected node type: " + currentNode.getNodeType() + "."
                    );
            }
        }

        // After iterating through all children, check if we found exactly one element and it matched
        // If elementChildCount is 0 and foundElement is null, it means no element child was found.
        // This is not an error based on the requirement "no more Element children".
        // It simply returns null, indicating no matching element was found under the strict conditions.
        return foundElement;
    }

    public static List<Element> getChildrenElementsByTagName(Element parentElement, String tagName) {
        List<Element> matchingElements = new ArrayList<>();

        if (parentElement == null) {
            throw new IllegalArgumentException("parentElement cannot be null.");
        }
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("tagName cannot be null or empty.");
        }

        NodeList children = parentElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);

            switch (currentNode.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element currentElement = (Element) currentNode;
                    if (currentElement.getTagName().equals(tagName)) {
                        matchingElements.add(currentElement);
                    } else {
//                        // If it's an Element node but doesn't match the desired tagName, throw an error
//                        throw new IllegalStateException(
//                                "Parent element '" + parentElement.getTagName()
//                                + "' contains an unexpected Element child with tag: '" + currentElement.getTagName()
//                                + "'. Expected only elements with tag '" + tagName + "', comments, or whitespace text."
//                        );
                    }
                    break;
                case Node.TEXT_NODE:
                    // Allow whitespace text nodes, but throw an error if it's non-whitespace text
                    if (!currentNode.getNodeValue().trim().isEmpty()) {
                        throw new IllegalStateException(
                                "Parent element '" + parentElement.getTagName()
                                + "' contains unexpected non-whitespace text node: '" + currentNode.getNodeValue().trim() + "'."
                        );
                    }
                    break;
                case Node.COMMENT_NODE:
                    // Allow comment nodes, do nothing
                    break;
                default:
                    // Disallow other node types explicitly
                    throw new IllegalStateException(
                            "Parent element '" + parentElement.getTagName()
                            + "' contains an unexpected node type: " + currentNode.getNodeType() + "."
                    );
            }
        }
        return matchingElements;
    }

    public static Document cloneDocument(Document originalDocument) {
        if (originalDocument == null) {
            return null; // Handle null input gracefully
        }

        // The cloneNode(true) method performs a deep clone.
        // It returns a Node, which then needs to be cast to a Document.
        // The Document object itself is a special type of Node.
        Node clonedNode = originalDocument.cloneNode(true);

        // Cast the cloned Node back to a Document
        if (clonedNode instanceof Document) {
            return (Document) clonedNode;
        } else {
            // This case should theoretically not happen if originalDocument is a valid Document,
            // but it's good for robustness.
            throw new IllegalStateException("Failed to clone Document. Cloned node is not a Document type.");
        }
    }

    public static Element replaceElementWithClone(Document doc, Element clonedSourceElement, Element targetElement) {
        if (doc == null || clonedSourceElement == null || targetElement == null) {
            return null; // Handle null inputs gracefully
        }

        // Ensure both elements belong to the same document
        if (clonedSourceElement.getOwnerDocument() != doc || targetElement.getOwnerDocument() != doc) {
            //throw new IllegalArgumentException(                "Both sourceElement and targetElement must belong to the provided Document."            );
        }

        Node targetParent = targetElement.getParentNode();
        if (targetParent == null) {
            throw new IllegalStateException("Target element has no parent and cannot be replaced.");
        }

        // 2. Replace targetElement with the cloned source element
        // replaceChild returns the old child (targetElement in this case).
        Node oldChild = targetParent.replaceChild(clonedSourceElement, targetElement);

        // oldChild will be the original targetElement, which is no longer in the document tree.
        // It should still be an Element if targetElement was one.
        if (oldChild instanceof Element) {
            return (Element) oldChild;
        } else {
            // This might happen if targetElement itself was not an Element,
            // which is contrary to the function's contract accepting Element for targetElement.
            throw new IllegalStateException("The replaced node was not an Element as expected.");
        }
    }

    public static void copyAttributesIgnoreNamespaces(Element sourceElement, Element targetElement) {
        if (sourceElement == null) {
            throw new IllegalArgumentException("Source element cannot be null.");
        }
        if (targetElement == null) {
            throw new IllegalArgumentException("Target element cannot be null.");
        }

        NamedNodeMap sourceAttributes = sourceElement.getAttributes();

        for (int i = 0; i < sourceAttributes.getLength(); i++) {
            Attr attribute = (Attr) sourceAttributes.item(i);

            // Get the local name of the attribute (ignores prefix like "ns1:")
            // For attributes without a namespace (like "id"), getLocalName() == getName()
            String attrLocalName = attribute.getLocalName();
            if (attrLocalName == null) { // Fallback for attributes without explicit local name (e.g., if parser not namespace-aware)
                attrLocalName = attribute.getName();
            }

            String attrValue = attribute.getValue();
            String attrQualifiedName = attribute.getName(); // The full name, including prefix if present

            // IMPORTANT: Still skip namespace declaration attributes (xmlns or xmlns:prefix)
            // These are not "regular" attributes and can break XML if treated as such.
            if (attrQualifiedName.startsWith("xmlns:") || attrQualifiedName.equals("xmlns")) {
                continue; // Skip namespace declaration attributes
            }

            // Always use setAttribute with the local name (or qualified name if local name is null)
            // This implicitly ignores namespaces for the attribute when setting.
            targetElement.setAttribute(attrLocalName, attrValue);
        }
    }

    public static Node replaceElementWithCopy(Element targetElement, NodeList sourceElements) {
        Node returnNode=null;
        
        if (targetElement == null || sourceElements == null) {
            throw new IllegalArgumentException("El elemento de destino y la lista de origen no pueden ser nulos.");
        }

        Node parent = targetElement.getParentNode();
        Document doc = parent.getOwnerDocument();

        // Insertar cada nodo clonado antes del elemento objetivo
        for (int i = 0; i < sourceElements.getLength(); i++) {
            Node imported = doc.importNode(sourceElements.item(i), true);
            
            if (returnNode==null) {
                returnNode=imported;
            }
            
            parent.insertBefore(imported, targetElement);
        }

        // Eliminar el nodo objetivo original
        parent.removeChild(targetElement);
        
        return returnNode;
    }

    
    public static NodeList getNodeListFromEvaluateXPath(String expression,Element element) {
        try {
            if (expression == null || expression.trim().isEmpty()) {
                throw new IllegalArgumentException("expression no puede ser null o vacio.");
            }
            if (element == null) {
                throw new IllegalArgumentException("element no puede ser null.");
            }
            if (expression.startsWith("//")) {
                throw new IllegalArgumentException("expression no puede empezar por //:"+expression);
            }
            
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList result = (NodeList) xpath.evaluate(expression, element, XPathConstants.NODESET);
            
            return result;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating XPath expression: " + expression, e);
        }        
    }
    
    public static Node getNodeFromEvaluateXPath(String expression,Element element) {
        try {
            if (expression == null || expression.trim().isEmpty()) {
                throw new IllegalArgumentException("expression no puede ser null o vacio.");
            }
            if (element == null) {
                throw new IllegalArgumentException("element no puede ser null.");
            }
            if (expression.startsWith("//")) {
                throw new IllegalArgumentException("expression no puede empezar por //:"+expression);
            }
            
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node result = (Node) xpath.evaluate(expression, element, XPathConstants.NODE);
            
            return result;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating XPath expression: " + expression, e);
        }        
    }    
    
    
    public static void printDocument(Document doc) {
        if (doc == null) {
            System.out.println("El documento es nulo y no puede ser impreso.");
            return;
        }
        try {
            // Crea un TransformerFactory
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            // Crea un Transformer a partir del factory
            Transformer transformer = transformerFactory.newTransformer();

            // Configura las propiedades de salida para una impresión legible
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Activa la indentación
            // Propiedad específica de Apache Xalan para el número de espacios de indentación
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); // Incluye la declaración XML (<?xml version="1.0" ...?>)
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // Asegura la codificación correcta

            // Prepara la fuente del documento (el DOMSource)
            DOMSource source = new DOMSource(doc);

            // Prepara el resultado para la salida (la consola en este caso)
            StreamResult result = new StreamResult(System.out);

            // Realiza la transformación y escribe el XML a la consola
            transformer.transform(source, result);
            System.out.println(); // Añade una nueva línea al final para mejor formato
        } catch (Exception e) {
            System.err.println("Error al imprimir el documento XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
