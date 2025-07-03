package com.educaflow.common.buildtools.viewprocessor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.NodeList;

/**
 *
 * @author logongas
 */
public class XMLPreprocesor {

    private static final String ROOT_TAG_NAME = "object-views";

    private static String getNombreExpediente(Path filePath) {
        Path fileNamePath = filePath.getFileName();


        String fileName = fileNamePath.toString();

        int dotIndex = fileName.lastIndexOf('.');

        String fileNameSinExtension;
        if (dotIndex > 0) {
            fileNameSinExtension=fileName.substring(0, dotIndex);
        } else {
            fileNameSinExtension=fileName;
        }
        
        String firstLetter = fileNameSinExtension.substring(0, 1).toUpperCase();
        String restOfText = fileNameSinExtension.substring(1);
        
        String nombreExpediente=firstLetter + restOfText;
        
        return nombreExpediente;
        
    }
    
    
    
    public static void doIncludePanels(Path filePath,Element elementIncludePanel,Element formElement,Element baseElement) {
            String content=elementIncludePanel.getTextContent();
            String[] lines = content.split("\n");

            doInclude(formElement,"./panel[@name='head']",getBaseElement(filePath,"../view-templates/template.xml"),false);
            
            
            for (String line : lines) {
                String trimmedLine = line.trim();

                String includeName;
                boolean readonly;
                if (!trimmedLine.isEmpty()) {
                    if (trimmedLine.startsWith("-")) {
                        readonly = true; // Si al menos uno empieza con '-', readonly es true
                        includeName = trimmedLine.substring(1); // Quita el guion
                    } else {
                        readonly=false;
                        includeName=trimmedLine;
                    }
                    
 
                    
                    doInclude(formElement,"./*[@name='" + includeName +"']",baseElement,readonly);
                    
                }
            }
        
            doInclude(formElement,"./panel[@name='footer']",getBaseElement(filePath,"../view-templates/template.xml"),false);
       

            
    }
    
    public static void doLeftRight(Element leftElement,Element rightElement,Element formElement) {
        Element cloneLeftElement=XMLUtil.cloneElement(formElement, leftElement);
        Element cloneRightElement=XMLUtil.cloneElement(formElement, rightElement);
        
        Element footerElement=(Element)XMLUtil.getNodeFromEvaluateXPath("./panel[@name='footer']",formElement);
        
        
        List<Element> childLeftElements=XMLUtil.getClonedChildElements(cloneLeftElement);
        List<Element> childRightElements=XMLUtil.getClonedChildElements(cloneRightElement);
        
        
        int sumColSpam=0;
        {
            int itemSpan=1;
            if (footerElement.hasAttribute("itemSpan")) {
                itemSpan=Integer.parseInt(footerElement.getAttribute("itemSpan"));
            }
            
            for(Element childElement:childLeftElements) {
                sumColSpam=sumColSpam+getColSpam(childElement,itemSpan);
            }
            for(Element childElement:childRightElements) {
                sumColSpam=sumColSpam+getColSpam(childElement,itemSpan);
            }
        }
        
        
        int colOffset=12-sumColSpam;
        
        if (childRightElements.size()>0) {
            childRightElements.get(0).setAttribute("colOffset", colOffset+"");
        }
        
        for(Element childElement:childLeftElements) {
            footerElement.appendChild(childElement);
        }
        for(Element childElement:childRightElements) {
            footerElement.appendChild(childElement);
        } 
        

        
    }
    
    
    public static int getColSpam(Element element,int defaultColSpam) {
        if (element.hasAttribute("colSpan")) {
            return Integer.parseInt(element.getAttribute("colSpan"));
        } else {
            return defaultColSpam;
        }
    }
    
    public static Element getBaseElement(Path filePath,String sourceFileName) {
        try {
            Element realBaseElement;

            Path baseDirectory = filePath.getParent();
            Path absolutePath = baseDirectory.resolve(sourceFileName).normalize();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            DocumentBuilder dBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(absolutePath.toFile());
            realBaseElement = doc.getDocumentElement();


            return realBaseElement;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void doFormStateRole(Path filePath,Element formElement) {
        String role = formElement.getAttribute("role");
        String state = formElement.getAttribute("state");
        formElement.removeAttribute("role");
        formElement.removeAttribute("state");
        
        String nombreExpediente=getNombreExpediente(filePath);
        
        try {
            String nameAttributeValue="exp-"+nombreExpediente+"-Base";
            String tagName="form";
            
            Element baseElement = XMLUtil.findNodeByTagName(getRootElement(formElement.getOwnerDocument()),  nameAttributeValue, tagName);
            if (baseElement == null) {
                throw new RuntimeException("No existe el nodo para el tag " + tagName + " y cuyo atributo name es  " + nameAttributeValue);
            }
            
       
            
            doMergeAttributes(formElement, baseElement);
            
            formElement.setAttribute("name", "exp-"+nombreExpediente+"-"+role+"-"+state+"-form");
            
            
            List<Element> hijos = XMLUtil.findElementsByTag(formElement);
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

            doIncludePanels(filePath,includePanelsElement, formElement,baseElement);
            doLeftRight(leftElement, rightElement, formElement);


        } catch (Exception ex) {
            throw new RuntimeException("step con role=" + role + " state=" + state, ex);
        }
        
    }
    
    
    public static Element getRootElement(Document document) {
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
    

    public static Document process(Path filePath, Document document) {
        
        try {
            
            Document newDocument = XMLUtil.cloneDocument(document);

            List<Element> elementsStep = XMLUtil.getFormElementsWithRoleStateAttributes(getRootElement(newDocument));
            for (Element elementStep : elementsStep) {

                doFormStateRole(filePath, elementStep);

            }

            List<Element> elementsWithIncludeAttribute = XMLUtil.getElementsWithIncludeAttribute(newDocument, ROOT_TAG_NAME);

            for (Element mainElementForIncludesAndExtends : elementsWithIncludeAttribute) {
                String name = mainElementForIncludesAndExtends.getAttribute("name");
                String tagName = mainElementForIncludesAndExtends.getTagName();
                String include = mainElementForIncludesAndExtends.getAttribute("include");

                try {
                    Element objectViewsElement = getRootElement(document);
                    
                    
                    Element baseElement = XMLUtil.findNodeByTagName(objectViewsElement, include, tagName);
                    if (baseElement == null) {
                        throw new RuntimeException("No existe el padre para el tag " + tagName + " y con padre " + include + " y nombre " + name);
                    }

                    List<Element> includesElements = XMLUtil.getInclude(mainElementForIncludesAndExtends);
                    for (Element includeElement : includesElements) {
                        String xpathTarget = includeElement.getAttribute("target");
                        if (xpathTarget.startsWith("//")) {
                            throw new RuntimeException("El atributo target no puede empezar por // ya que todo debe ser relativo al element include con el que estamos trabajando:" + xpathTarget);
                        }

                        boolean readOnly = false;
                        if (includeElement.hasAttribute("readonly")) {
                            String readOnlyValue = includeElement.getAttribute("readonly");
                            if ("true".equalsIgnoreCase(readOnlyValue)) {
                                readOnly = true;
                            } else if ("false".equalsIgnoreCase(readOnlyValue)) {
                                readOnly = false;
                            } else {
                                throw new RuntimeException("El valor del atributo 'readonly' no es válido:" + readOnlyValue);
                            }
                        }

                        Element realBaseElement;
                        Path absolutePathSourceFileName = null;
                        if (includeElement.hasAttribute("sourceFileName")) {
                            Path baseDirectory = filePath.getParent();
                            Path absolutePath = baseDirectory.resolve(includeElement.getAttribute("sourceFileName")).normalize();

                            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                            documentBuilderFactory.setNamespaceAware(false);
                            DocumentBuilder dBuilder = documentBuilderFactory.newDocumentBuilder();
                            Document doc = dBuilder.parse(absolutePath.toFile());
                            realBaseElement = doc.getDocumentElement();

                        } else {
                            realBaseElement = baseElement;
                        }

                        includeElement.getAttribute("sourceFileName");
                        doTareaInclude(includeElement, realBaseElement, xpathTarget, readOnly);
                    }

                    List<Element> extendsElements = XMLUtil.getExtends(mainElementForIncludesAndExtends);
                    applyExtends(extendsElements, mainElementForIncludesAndExtends);

                    doMergeAttributes(mainElementForIncludesAndExtends, baseElement);

                    mainElementForIncludesAndExtends.removeAttribute("include");
                } catch (Exception ex) {
                    throw new RuntimeException("tag=" + tagName + " name=" + name + " include=" + include, ex);
                }
            }

            return newDocument;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void applyExtends(List<Element> extendsElements, Element mainElementForIncludesAndExtends) {
        for (Element extendElement : extendsElements) {
            String xpathTarget = extendElement.getAttribute("target");
            if (xpathTarget.startsWith("//")) {
                throw new RuntimeException("El atributo target no puede empezar por // ya que todo debe ser relativo al element include con el que estamos trabajando:" + xpathTarget);
            }
            Element tareaInsert = XMLUtil.getChildElementUniqueByTagName(extendElement, "insert");
            if (tareaInsert != null) {
                doTareaInsert(xpathTarget, tareaInsert, mainElementForIncludesAndExtends);
            }

            Element tareaReplace = XMLUtil.getChildElementUniqueByTagName(extendElement, "replace");
            if (tareaReplace != null) {
                doTareaReplace(xpathTarget, tareaReplace, mainElementForIncludesAndExtends);
            }
            Element tareaMove = XMLUtil.getChildElementUniqueByTagName(extendElement, "move");
            if (tareaMove != null) {
                doTareaMove(xpathTarget, tareaMove, mainElementForIncludesAndExtends);
            }

            List<Element> tareaAttributes = XMLUtil.getChildrenElementsByTagName(extendElement, "attribute");
            if ((tareaAttributes != null) && (!tareaAttributes.isEmpty())) {
                doTareaAttributes(xpathTarget, tareaAttributes, mainElementForIncludesAndExtends);
            }

            //Los extends luego hay que eliminarlos
            Node parentNode = extendElement.getParentNode();
            parentNode.removeChild(extendElement);
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

    private static void doTareaInsert(String xpathTarget, Element tareaInsert, Element elementGridOrFormWithIncludeAttribute) {

        if (xpathTarget == null || xpathTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("xpathTarget cannot be null or empty.");
        }
        if (tareaInsert == null || elementGridOrFormWithIncludeAttribute == null) {
            throw new IllegalArgumentException("tareaInsert and element parameters cannot be null.");
        }

        String position = tareaInsert.getAttribute("position");
        if (position == null || position.trim().isEmpty()) {
            throw new IllegalArgumentException("The 'tareaInsert' element must have a 'position' attribute (before, after, or inside).");
        }
        position = position.trim().toLowerCase();

        XPath xpath = XPathFactory.newInstance().newXPath();
        Node targetNode = null;
        try {
            targetNode = (Node) xpath.evaluate(xpathTarget, elementGridOrFormWithIncludeAttribute, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating XPath expression: " + xpathTarget, e);
        }

        if (targetNode == null) {
            throw new RuntimeException("XPath target '" + xpathTarget + "' did not find any node relative to element '" + elementGridOrFormWithIncludeAttribute.getTagName() + "'.");
        }

        // Get the parent of the targetNode for 'before'/'after' operations
        Node targetNodeParent = targetNode.getParentNode();

        // Obtener los hijos del elemento tareaInsert que se van a copiar e insertar
        NodeList nodesToInsert = tareaInsert.getChildNodes();
        // Obtener el documento del nodo de destino (donde se realizará la inserción)
        Document targetDocument = targetNode.getOwnerDocument();

        switch (position) {
            case "before":
                for (int i = 0; i < nodesToInsert.getLength(); i++) {
                    Node node = nodesToInsert.item(i);
                    // IMPORTANTE: Clonar e importar el nodo al documento de destino
                    Node importedNode = targetDocument.importNode(node, true); // 'true' para copia profunda
                    targetNodeParent.insertBefore(importedNode, targetNode);
                }
                break;
            case "after":
                Node refNodeForAfter = targetNode.getNextSibling();
                // Iterar en orden inverso para mantener el orden original después del targetNode.
                // Si insertamos en orden normal, el primer nodo insertado se convertiría en el "más a la izquierda"
                // después del targetNode, y los siguientes se insertarían antes de él, invirtiendo el orden.
                for (int i = nodesToInsert.getLength() - 1; i >= 0; i--) {
                    Node node = nodesToInsert.item(i);
                    // IMPORTANTE: Clonar e importar el nodo al documento de destino
                    Node importedNode = targetDocument.importNode(node, true); // 'true' para copia profunda

                    if (refNodeForAfter != null) {
                        targetNodeParent.insertBefore(importedNode, refNodeForAfter);
                    } else {
                        targetNodeParent.appendChild(importedNode);
                    }
                }
                break;
            case "inside-before":
                if (targetNode.getNodeType() != Node.ELEMENT_NODE) {
                    throw new IllegalArgumentException("Cannot insert 'inside' a non-element node. Target node type: " + targetNode.getNodeName());
                }

                for (int i = nodesToInsert.getLength() - 1; i > 0; i--) {
                    Node node = nodesToInsert.item(i);
                    // IMPORTANTE: Clonar e importar el nodo al documento de destino
                    Node importedNode = targetDocument.importNode(node, true); // 'true' para copia profunda
                    Node firstChild = ((Element) targetNode).getFirstChild();
                    if (firstChild == null) {
                        targetNode.appendChild(importedNode);
                    } else {
                        targetNode.insertBefore(importedNode, firstChild);
                    }
                }
                break;
            case "inside":
            case "inside-after":
                if (targetNode.getNodeType() != Node.ELEMENT_NODE) {
                    throw new IllegalArgumentException("Cannot insert 'inside' a non-element node. Target node type: " + targetNode.getNodeName());
                }
                Element targetElement = (Element) targetNode;
                for (int i = 0; i < nodesToInsert.getLength(); i++) {
                    Node node = nodesToInsert.item(i);
                    // IMPORTANTE: Clonar e importar el nodo al documento de destino
                    Node importedNode = targetDocument.importNode(node, true); // 'true' para copia profunda
                    targetElement.appendChild(importedNode);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid 'position' attribute value: '" + position + "'. Must be 'before', 'after', or 'inside'.");
        }

    }

    private static void doTareaReplace(String xpathTarget, Element tareaReplace, Element elementGridOrFormWithIncludeAttribute) {
        if (xpathTarget == null || xpathTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("xpathTarget cannot be null or empty.");
        }
        if (tareaReplace == null || elementGridOrFormWithIncludeAttribute == null) {
            throw new IllegalArgumentException("tareaReplace and element parameters cannot be null.");
        }

        // 1. Prepare XPath and Find Target Node
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node targetNode = null;
        try {
            // Evaluate XPath relative to the 'element' parameter
            targetNode = (Node) xpath.evaluate(xpathTarget, elementGridOrFormWithIncludeAttribute, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating XPath expression: " + xpathTarget, e);
        }

        if (targetNode == null) {
            throw new RuntimeException("XPath target '" + xpathTarget + "' did not find any node relative to element '" + elementGridOrFormWithIncludeAttribute.getTagName() + "'.");
        }

        // Get the parent of the targetNode
        Node targetNodeParent = targetNode.getParentNode();
        if (targetNodeParent == null) {
            throw new RuntimeException("Target node '" + targetNode.getNodeName() + "' has no parent and cannot be replaced.");
        }

        // Get the Document where the replacement will occur
        Document targetDocument = targetNode.getOwnerDocument();
        if (targetDocument == null) {
            throw new RuntimeException("Target node does not belong to a document.");
        }

        // 2. Collect content to insert from tareaReplace
        // It's crucial to collect them first as moving them will modify tareaReplace's child list
        NodeList nodesToInsertList = tareaReplace.getChildNodes();
        List<Node> nodesToInsert = new ArrayList<>();
        for (int i = 0; i < nodesToInsertList.getLength(); i++) {
            nodesToInsert.add(nodesToInsertList.item(i));
        }

        // 3. Perform Replacement
        // Insert all new nodes before the targetNode, then remove the targetNode.
        // This is the most straightforward way to replace one node with multiple.
        for (Node node : nodesToInsert) {
            // Import node if it's from a different document (or even if it's the same, it ensures ownership)
            Node nodeToAppend = (node.getOwnerDocument() != targetDocument) ? targetDocument.importNode(node, true) : node;
            targetNodeParent.insertBefore(nodeToAppend, targetNode);
        }

        // If nodesToInsert was empty, targetNode is simply removed.
        // If nodesToInsert had content, they are now inserted, and targetNode is removed.
        targetNodeParent.removeChild(targetNode);

    }

    private static void doTareaMove(String xpathTarget, Element tareaMove, Element elementGridOrFormWithIncludeAttribute) {
        if (xpathTarget == null || xpathTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("xpathTarget cannot be null or empty.");
        }
        if (tareaMove == null) {
            throw new IllegalArgumentException("tareaMove  cannot be null.");
        }
        if (elementGridOrFormWithIncludeAttribute == null) {
            throw new IllegalArgumentException("clonedBaseElement cannot be null.");
        }

        String position = tareaMove.getAttribute("position");
        if (position == null || position.trim().isEmpty()) {
            throw new IllegalArgumentException("The 'tareaMove' element must have a 'position' attribute (before, after, or inside).");
        }
        position = position.trim().toLowerCase(); // Normalize position value

        String sourceXPath = tareaMove.getAttribute("source");
        if (sourceXPath == null || sourceXPath.trim().isEmpty()) {
            throw new IllegalArgumentException("The 'tareaMove' element must have a 'source' attribute (XPath expression).");
        }

        XPath xpath = XPathFactory.newInstance().newXPath();
        Node targetNode = null;
        try {
            targetNode = (Node) xpath.evaluate(xpathTarget, elementGridOrFormWithIncludeAttribute, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating xpathTarget expression: " + xpathTarget, e);
        }
        if (targetNode == null) {
            throw new RuntimeException("xpathTarget '" + xpathTarget + "' did not find any node relative to clonedBaseElement.");
        }

        XPath xpath2 = XPathFactory.newInstance().newXPath();
        Node sourceNodeToMove = null;
        try {
            sourceNodeToMove = (Node) xpath2.evaluate(sourceXPath, elementGridOrFormWithIncludeAttribute, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating source XPath expression: " + sourceXPath, e);
        }
        if (sourceNodeToMove == null) {
            throw new IllegalArgumentException("Source XPath '" + sourceXPath + "' did not find a node to move. It must identify exactly one node.");
        }

        Node originalParent = sourceNodeToMove.getParentNode();
        if (originalParent == null) {
            throw new RuntimeException("The source node '" + sourceNodeToMove.getNodeName() + "' has no parent and cannot be moved.");
        }
        Node targetNodeParent = targetNode.getParentNode();
        if (targetNodeParent == null) {
            throw new RuntimeException("The source node '" + targetNode.getNodeName() + "' has no parent and cannot be moved.");
        }

        originalParent.removeChild(sourceNodeToMove);
        switch (position) {
            case "before":
                targetNodeParent.insertBefore(sourceNodeToMove, targetNode);
                break;
            case "after":
                Node nextSibling = targetNode.getNextSibling();
                if (nextSibling != null) {
                    targetNodeParent.insertBefore(sourceNodeToMove, nextSibling);
                } else {
                    targetNodeParent.appendChild(sourceNodeToMove); // Append to parent if no next sibling
                }
                break;
            case "inside":
                if (targetNode.getNodeType() != Node.ELEMENT_NODE) {
                    throw new RuntimeException("Cannot move 'inside' a non-Element node. Target node: " + targetNode.getNodeName());
                }
                ((Element) targetNode).appendChild(sourceNodeToMove);
                break;
            default:
                throw new IllegalArgumentException("Invalid 'position' attribute value: '" + position + "'. Must be 'before', 'after', or 'inside'.");
        }
    }

    private static void doTareaAttributes(String xpathTarget, List<Element> tareaAttributes, Element elementGridOrFormWithIncludeAttribute) {
        if (xpathTarget == null || xpathTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("xpathTarget cannot be null or empty.");
        }
        if (tareaAttributes == null || elementGridOrFormWithIncludeAttribute == null) {
            throw new IllegalArgumentException("tareaAttributes list and clonedBaseElement cannot be null.");
        }

        XPath xpath = XPathFactory.newInstance().newXPath();

        // 1. Find the target element
        NodeList targetNodes = null;
        try {
            targetNodes = (NodeList) xpath.evaluate(xpathTarget, elementGridOrFormWithIncludeAttribute, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating xpathTarget expression: " + xpathTarget, e);
        }

        if (targetNodes == null) {
            throw new RuntimeException("Target elements specified by xpathTarget '" + xpathTarget + "' not found.");
        }
        if (targetNodes.getLength() == 0) {
            throw new RuntimeException("Con xpathTarget='" + xpathTarget + "' targetNodes no tiene elementos  con respecto a tag '" + elementGridOrFormWithIncludeAttribute.getTagName() + "'.");
        }
        for (int i = 0; i < targetNodes.getLength(); i++) {
            Node targetNode = targetNodes.item(i);
            if (targetNode.getNodeType() != Node.ELEMENT_NODE) {
                throw new RuntimeException("Target node found by xpathTarget '" + xpathTarget + "' is not an Element. Cannot modify attributes on it.");
            }

            Element targetElement = (Element) targetNode;

            for (Element attrTask : tareaAttributes) {
                String attrName = attrTask.getAttribute("name");
                String attrValue = attrTask.getAttribute("value"); // Can be empty or null

                if (attrName == null || attrName.trim().isEmpty()) {
                    throw new RuntimeException("Hay un atributo con el name vacio");
                }

                if (attrValue == null) {
                    attrValue = "";
                }

                if (attrValue.trim().isEmpty()) {
                    targetElement.removeAttribute(attrName);
                } else {
                    targetElement.setAttribute(attrName, attrValue);
                }
            }
        }
    }

    private static void doTareaInclude(Element tareaInclude, Element baseElement, String xpathTarget, boolean readOnly) {
        try {
            if (tareaInclude == null) {
                throw new IllegalArgumentException("tareaReplace  parameters cannot be null.");
            }

            NodeList targetElementsToInclude = XMLUtil.getNodeListFromEvaluateXPath(xpathTarget, baseElement);

            if (targetElementsToInclude == null) {
                throw new RuntimeException("Con xpathTarget='" + xpathTarget + "' targetElementsToInclude es null con respecto a tag '" + baseElement.getTagName() + "'.");
            }
            if (targetElementsToInclude.getLength() == 0) {
                throw new RuntimeException("Con xpathTarget='" + xpathTarget + "' targetElementsToInclude no tiene elementos  con respecto a tag '" + baseElement.getTagName() + "'.");
            }

            Element newElementIncluded = (Element) XMLUtil.replaceElementWithCopy(tareaInclude, targetElementsToInclude);

            List<Element> extendsElements = XMLUtil.getExtends(tareaInclude);
            applyExtends(extendsElements, newElementIncluded);

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

    private static void doInclude(Element formElement, String xpathTarget, Element baseElement, boolean readOnly) {
        try {


            NodeList targetElementsToInclude = XMLUtil.getNodeListFromEvaluateXPath(xpathTarget, baseElement);

            if (targetElementsToInclude == null) {
                throw new RuntimeException("Con xpathTarget='" + xpathTarget + "' targetElementsToInclude es null con respecto a tag '" + baseElement.getTagName() + "'.");
            }
            if (targetElementsToInclude.getLength() != 1) {
                throw new RuntimeException("Con xpathTarget='" + xpathTarget + "' deberia tener solo un elemento pero tiene" + targetElementsToInclude.getLength());
            }

            Element newElementIncluded = (Element) XMLUtil.cloneElement(formElement, (Element)targetElementsToInclude.item(0));
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
    
}
