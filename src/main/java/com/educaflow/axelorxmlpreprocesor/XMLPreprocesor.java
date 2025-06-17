package com.educaflow.axelorxmlpreprocesor;

import java.util.ArrayList;
import java.util.List;
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

    public static Document process(Document document) {
        Document newDocument = XMLUtil.cloneDocument(document);

        List<Element> inheritElements = XMLUtil.getInheritElements(newDocument, ROOT_TAG_NAME);

        for (Element elementNuevoQueHereda : inheritElements) {
            String name = elementNuevoQueHereda.getAttribute("name");
            String tagName = elementNuevoQueHereda.getTagName();
            String inherit = elementNuevoQueHereda.getAttribute("inherit");

            Element baseElement = XMLUtil.findNodeByTagName(newDocument, ROOT_TAG_NAME, inherit, tagName);
            if (baseElement == null) {
                throw new RuntimeException("No existe el padre para el tag " + tagName + " y con padre " + inherit + " y nombre " + name);
            }

            Node clonedBaseNode = baseElement.cloneNode(true);

            // Ensure the cloned node is an Element
            if (!(clonedBaseNode instanceof Element)) {
                throw new ClassCastException("Cloned source node is not an Element.");
            }
            Element clonedBaseElement = (Element) clonedBaseNode;
            clonedBaseElement.setAttribute("name", name);

            XMLUtil.copyAttributesIgnoreNamespaces(elementNuevoQueHereda, clonedBaseElement);
            clonedBaseElement.removeAttribute("inherit");

            elementNuevoQueHereda = XMLUtil.replaceElementWithClone(document, clonedBaseElement, elementNuevoQueHereda);

            List<Element> extendsElements = XMLUtil.getExtends(elementNuevoQueHereda);

            for (Element extendElement : extendsElements) {
                String xpathTarget = extendElement.getAttribute("target");
                Element tareaInsert = XMLUtil.getChildElementUniqueByTagName(extendElement, "insert");
                if (tareaInsert != null) {
                    doTareaInsert(xpathTarget, tareaInsert, clonedBaseElement);
                }

                Element tareaReplace = XMLUtil.getChildElementUniqueByTagName(extendElement, "replace");
                if (tareaReplace != null) {
                    doTareaReplace(xpathTarget, tareaReplace, clonedBaseElement);
                }
                Element tareaMove = XMLUtil.getChildElementUniqueByTagName(extendElement, "move");
                if (tareaMove != null) {
                    doTareaMove(xpathTarget, tareaMove, clonedBaseElement);
                }

                List<Element> tareaAttributes = XMLUtil.getChildrenElementsByTagName(extendElement, "attribute");
                if ((tareaAttributes != null) && (!tareaAttributes.isEmpty())) {
                    doTareaAttributes(xpathTarget, tareaAttributes, clonedBaseElement);
                }
            }

        }

        return newDocument;
    }

    private static void doTareaInsert(String xpathTarget, Element tareaInsert, Element element) {

        if (xpathTarget == null || xpathTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("xpathTarget cannot be null or empty.");
        }
        if (tareaInsert == null || element == null) {
            // This is unlikely if called internally, but good practice
            throw new IllegalArgumentException("tareaInsert and element parameters cannot be null.");
        }

        // Get the position from tareaInsert's attribute
        String position = tareaInsert.getAttribute("position");
        if (position == null || position.trim().isEmpty()) {
            throw new IllegalArgumentException("The 'tareaInsert' element must have a 'position' attribute (before, after, or inside).");
        }
        position = position.trim().toLowerCase();

        // 1. Prepare XPath
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node targetNode = null;
        try {
            // Evaluate XPath relative to the 'element' parameter
            targetNode = (Node) xpath.evaluate(xpathTarget, element, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating XPath expression: " + xpathTarget, e);
        }

        if (targetNode == null) {
            throw new RuntimeException("XPath target '" + xpathTarget + "' did not find any node relative to element '" + element.getTagName() + "'.");
        }

        // Get the parent of the targetNode for 'before'/'after' operations
        Node targetNodeParent = targetNode.getParentNode();
        if ((position.equals("before") || position.equals("after")) && targetNodeParent == null) {
            throw new RuntimeException("Cannot insert 'before' or 'after' as the target node '" + targetNode.getNodeName() + "' has no parent.");
        }

        // Get the Document the targetNode belongs to
        Document targetDocument = targetNode.getOwnerDocument();
        if (targetDocument == null) {
            throw new RuntimeException("Target node does not belong to a document.");
        }

        // 2. Collect content to insert from tareaInsert
        // We need to collect them first because moving them will change tareaInsert's child list
        NodeList nodesToInsertList = tareaInsert.getChildNodes();
        List<Node> nodesToInsert = new ArrayList<>();
        for (int i = 0; i < nodesToInsertList.getLength(); i++) {
            nodesToInsert.add(nodesToInsertList.item(i));
        }

        // 3. Perform insertion based on position
        switch (position) {
            case "before":
                for (Node node : nodesToInsert) {
                    // Import node if it's from a different document (tareaInsert's owner doc != target's owner doc)
                    Node nodeToAppend = (node.getOwnerDocument() != targetDocument) ? targetDocument.importNode(node, true) : node;
                    targetNodeParent.insertBefore(nodeToAppend, targetNode);
                }
                break;
            case "after":
                // Insert after: equivalent to inserting before the next sibling (if it exists)
                // or appending to parent (if no next sibling)
                Node nextSibling = targetNode.getNextSibling();
                for (Node node : nodesToInsert) {
                    Node nodeToAppend = (node.getOwnerDocument() != targetDocument) ? targetDocument.importNode(node, true) : node;
                    if (nextSibling != null) {
                        targetNodeParent.insertBefore(nodeToAppend, nextSibling);
                    } else {
                        // If no next sibling, append to the end of the parent's children
                        targetNodeParent.appendChild(nodeToAppend);
                    }
                }
                break;
            case "inside":
                // Insert inside: append to the targetNode's children
                if (targetNode.getNodeType() != Node.ELEMENT_NODE) {
                    throw new RuntimeException("Cannot insert 'inside' a non-Element node. Target node: " + targetNode.getNodeName());
                }
                Element targetElementNode = (Element) targetNode;
                for (Node node : nodesToInsert) {
                    Node nodeToAppend = (node.getOwnerDocument() != targetDocument) ? targetDocument.importNode(node, true) : node;
                    targetElementNode.appendChild(nodeToAppend);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid 'position' attribute value: '" + position + "'. Must be 'before', 'after', or 'inside'.");
        }

        // Remove the original nodes from tareaInsert as they have been moved
        // This is important if you collect and then move.
        // If you intended to COPY, you'd clone them before the loop and not remove them here.
        for (Node node : nodesToInsert) {
            if (node.getParentNode() == tareaInsert) { // Only remove if still its child (not already moved in case of copy intent)
                tareaInsert.removeChild(node);
            }
        }
    }

    private static void doTareaReplace(String xpathTarget, Element tareaReplace, Element element) {
        if (xpathTarget == null || xpathTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("xpathTarget cannot be null or empty.");
        }
        if (tareaReplace == null || element == null) {
            throw new IllegalArgumentException("tareaReplace and element parameters cannot be null.");
        }

        // 1. Prepare XPath and Find Target Node
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node targetNode = null;
        try {
            // Evaluate XPath relative to the 'element' parameter
            targetNode = (Node) xpath.evaluate(xpathTarget, element, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating XPath expression: " + xpathTarget, e);
        }

        if (targetNode == null) {
            throw new RuntimeException("XPath target '" + xpathTarget + "' did not find any node relative to element '" + element.getTagName() + "'.");
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

        // 4. Clean up original nodes from tareaReplace (as they have been moved)
        for (Node node : nodesToInsert) {
            if (node.getParentNode() == tareaReplace) { // Only remove if still its child
                tareaReplace.removeChild(node);
            }
        }
    }

    private static void doTareaMove(String xpathTarget, Element tareaMove, Element clonedBaseElement) {
        if (xpathTarget == null || xpathTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("xpathTarget cannot be null or empty.");
        }
        if (tareaMove == null || clonedBaseElement == null) {
            throw new IllegalArgumentException("tareaMove and clonedBaseElement parameters cannot be null.");
        }

        // Get position and source XPaths from tareaMove attributes
        String position = tareaMove.getAttribute("position");
        String sourceXPath = tareaMove.getAttribute("source");

        if (position == null || position.trim().isEmpty()) {
            throw new IllegalArgumentException("The 'tareaMove' element must have a 'position' attribute (before, after, or inside).");
        }
        if (sourceXPath == null || sourceXPath.trim().isEmpty()) {
            throw new IllegalArgumentException("The 'tareaMove' element must have a 'source' attribute (XPath expression).");
        }

        position = position.trim().toLowerCase(); // Normalize position value

        XPath xpath = XPathFactory.newInstance().newXPath();

        // 1. Find the target node
        Node targetNode = null;
        try {
            targetNode = (Node) xpath.evaluate(xpathTarget, clonedBaseElement, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating xpathTarget expression: " + xpathTarget, e);
        }

        if (targetNode == null) {
            throw new RuntimeException("xpathTarget '" + xpathTarget + "' did not find any node relative to clonedBaseElement.");
        }

        // Get the parent of the targetNode for 'before'/'after' operations
        Node targetNodeParent = targetNode.getParentNode();
        if ((position.equals("before") || position.equals("after")) && targetNodeParent == null) {
            throw new RuntimeException("Cannot move 'before' or 'after' as the target node '" + targetNode.getNodeName() + "' has no parent.");
        }

        // 2. Find the SINGLE source node to move
        Node sourceNodeToMove = null;
        try {
            // *** KEY CHANGE: Use XPathConstants.NODE for a single node result ***
            sourceNodeToMove = (Node) xpath.evaluate(sourceXPath, clonedBaseElement, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating source XPath expression: " + sourceXPath, e);
        }

        if (sourceNodeToMove == null) {
            throw new IllegalArgumentException("Source XPath '" + sourceXPath + "' did not find a node to move. It must identify exactly one node.");
        }

        // Ensure the node has a parent before attempting to remove it
        Node originalParent = sourceNodeToMove.getParentNode();
        if (originalParent == null) {
            throw new RuntimeException("The source node '" + sourceNodeToMove.getNodeName() + "' has no parent and cannot be moved.");
        }

        // 3. Perform the move operation
        // Remove the node from its original position
        originalParent.removeChild(sourceNodeToMove);

        // Insert the node at the new target position
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

    private static void doTareaAttributes(String xpathTarget, List<Element> tareaAttributes, Element clonedBaseElement) {
        if (xpathTarget == null || xpathTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("xpathTarget cannot be null or empty.");
        }
        if (tareaAttributes == null || clonedBaseElement == null) {
            throw new IllegalArgumentException("tareaAttributes list and clonedBaseElement cannot be null.");
        }

        XPath xpath = XPathFactory.newInstance().newXPath();

        // 1. Find the target element
        Node targetNode = null;
        try {
            targetNode = (Node) xpath.evaluate(xpathTarget, clonedBaseElement, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error evaluating xpathTarget expression: " + xpathTarget, e);
        }

        if (targetNode == null) {
            throw new RuntimeException("Target element specified by xpathTarget '" + xpathTarget + "' not found.");
        }
        if (targetNode.getNodeType() != Node.ELEMENT_NODE) {
            throw new RuntimeException("Target node found by xpathTarget '" + xpathTarget + "' is not an Element. Cannot modify attributes on it.");
        }

        Element targetElement = (Element) targetNode;

        // 2. Process each attribute task
        for (Element attrTask : tareaAttributes) {
            String attrName = attrTask.getAttribute("name");
            String attrValue = attrTask.getAttribute("value"); // Can be empty or null

            if (attrName == null || attrName.trim().isEmpty()) {
                System.err.println("Warning: 'tareaAttribute' element missing 'name' attribute. Skipping this task.");
                continue; // Skip this attribute task if name is invalid
            }

            // Normalize attrValue for empty check
            if (attrValue == null) {
                attrValue = "";
            }

            // Remove the attribute if value is empty/blank
            if (attrValue.trim().isEmpty()) {
                // IMPORTANT: We use removeAttributeNS if the original attribute might have had a namespace
                // However, without knowing the original namespace, we can only remove by local name
                // or qualified name. For simplicity here, we assume non-namespaced attributes
                // or that getAttribute's behavior is sufficient for removal.
                // If you were dealing with true namespaced attributes (e.g., xml:lang), you'd need
                // to know their URI to remove them correctly using removeAttributeNS.
                targetElement.removeAttribute(attrName);
            } else {
                // Set the attribute with the given value
                // This implicitly overwrites if it already exists
                targetElement.setAttribute(attrName, attrValue);
            }
        }
    }

}
