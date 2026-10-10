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
        
        Element footerPanel = panelFinder.findAndImport("subsysExpedientes-template-footer-panel",ownerDocument);

        
        int itemSpan = XMLUtil.getIntegerAttribute(footerPanel,"itemSpan", 1);
        List<Element> slotsLeft = groupAlternatives(childButtonsLeftElements, footerPanel, itemSpan);
        List<Element> slotsRight = groupAlternatives(childButtonsRightElements, footerPanel, itemSpan);

        // El colOffset del primer botón de la derecha se sobrescribe, así que no cuenta.
        if (slotsRight.size() > 0) {
            slotsRight.get(0).removeAttribute("colOffset");
        }
        int sumColSpam = sumColumns(slotsLeft, itemSpan) + sumColumns(slotsRight, itemSpan);

        int colOffset = 12 - sumColSpam;

        if (slotsRight.size() > 0) {
            slotsRight.get(0).setAttribute("colOffset", colOffset + "");
        }
        
        for (Element slot : slotsLeft) {
            footerPanel.appendChild(slot);
        }
        for (Element slot : slotsRight) {
            footerPanel.appendChild(slot);
        }
        
        
        Node parent = footer.getParentNode();
        parent.insertBefore(footerPanel,footer);
        if (isFooterDeFormDeEstado(footer)) {
            Element notasPanel = panelFinder.findAndImport("subsysExpedientes-template-notas-panel",ownerDocument);
            parent.insertBefore(notasPanel,footer);
        }
        parent.removeChild(footer);

    }

    /**
     * Agrupa los botones en los huecos que ocupan en el footer. Dos botones cuyas condiciones de
     * visibilidad son una la negación de la otra (p.ej. {@code showIf="x"} y {@code showIf="!x"}) nunca
     * se ven juntos, así que van a un mismo hueco: un panel sin marco del ancho del mayor, con los dos
     * dentro a todo lo ancho.
     *
     * <p>No basta con contarlos una sola vez al calcular el colOffset: el layout de Axelor calcula la
     * columna de cada elemento sumando la de <b>todos</b> los anteriores, también la de los ocultos
     * ({@code computeLayout} de {@code axelor-front/src/views/form/builder/form-layouts.tsx}), así que un
     * botón oculto suelto empuja a los siguientes y el último salta de fila.
     */
    private static List<Element> groupAlternatives(List<Element> buttons, Element footerPanel, int itemSpan) {
        List<Element> slots = new ArrayList<>();
        List<Element> paired = new ArrayList<>();

        for (Element button : buttons) {
            if (paired.contains(button)) {
                continue;
            }

            Element alternative = findAlternative(button, buttons, paired);
            if (alternative == null) {
                slots.add(button);
            } else {
                paired.add(alternative);
                slots.add(createAlternativesPanel(button, alternative, footerPanel, itemSpan));
            }
        }

        return slots;
    }

    private static Element createAlternativesPanel(Element button, Element alternative, Element footerPanel, int itemSpan) {
        int colSpan = Math.max(XMLUtil.getIntegerAttribute(button, "colSpan", itemSpan), XMLUtil.getIntegerAttribute(alternative, "colSpan", itemSpan));

        Document document = footerPanel.getOwnerDocument();
        String namespace = footerPanel.getNamespaceURI();
        Element panel = (namespace == null) ? document.createElement("panel") : document.createElementNS(namespace, "panel");
        panel.setAttribute("colSpan", colSpan + "");
        panel.setAttribute("showFrame", "false");
        if (button.hasAttribute("colOffset")) {
            panel.setAttribute("colOffset", button.getAttribute("colOffset"));
        }

        for (Element element : List.of(button, alternative)) {
            element.removeAttribute("colOffset");
            element.setAttribute("colSpan", "12");
            panel.appendChild(element);
        }

        return panel;
    }

    /** Las columnas que ocupan los huecos: su colSpan más su colOffset. */
    private static int sumColumns(List<Element> slots, int itemSpan) {
        int sum = 0;
        for (Element slot : slots) {
            sum = sum + XMLUtil.getIntegerAttribute(slot, "colSpan", itemSpan) + XMLUtil.getIntegerAttribute(slot, "colOffset", 0);
        }

        return sum;
    }

    private static Element findAlternative(Element button, List<Element> buttons, List<Element> paired) {
        String condition = getVisibleCondition(button);
        if (condition == null) {
            return null;
        }

        for (Element other : buttons) {
            if ((other != button) && (paired.contains(other) == false)) {
                String otherCondition = getVisibleCondition(other);
                if ((otherCondition != null) && negate(condition).equals(otherCondition)) {
                    return other;
                }
            }
        }

        return null;
    }

    /**
     * La condición en la que el botón es visible, normalizada sin espacios, o {@code null} si no tiene.
     */
    private static String getVisibleCondition(Element button) {
        String showIf = button.getAttribute("showIf").replaceAll("\\s", "");
        String hideIf = button.getAttribute("hideIf").replaceAll("\\s", "");

        if (showIf.isEmpty() == false) {
            return removeDoubleNegation(showIf);
        } else if (hideIf.isEmpty() == false) {
            return negate(hideIf);
        } else {
            return null;
        }
    }

    private static String negate(String condition) {
        if (isNegation(condition)) {
            return removeDoubleNegation(unwrap(condition.substring(1)));
        } else {
            return "!" + wrap(unwrap(condition));
        }
    }

    private static String removeDoubleNegation(String condition) {
        String unwrapped = unwrap(condition);
        if (isNegation(unwrapped)) {
            String negated = unwrap(unwrapped.substring(1));
            if (isNegation(negated)) {
                return removeDoubleNegation(negated.substring(1));
            } else {
                return "!" + wrap(negated);
            }
        } else {
            return unwrapped;
        }
    }

    /**
     * Si la condición entera es la negación de algo ({@code !x} o {@code !(...)}), no de solo su primer término
     * como en {@code !a || b}.
     */
    private static boolean isNegation(String condition) {
        if (condition.startsWith("!") == false) {
            return false;
        }
        String operand = condition.substring(1);

        return operand.matches("!*[\\w.$]+") || (operand.startsWith("(") && operand.endsWith(")") && isOuterParenthesis(operand));
    }

    /**
     * Pone entre paréntesis la condición salvo que sea un identificador simple (p.ej. {@code presentadoEnPapel}),
     * para que {@code !x} y {@code !(x)} se normalicen igual.
     */
    private static String wrap(String condition) {
        if (condition.matches("[\\w.$]+")) {
            return condition;
        } else {
            return "(" + condition + ")";
        }
    }

    private static String unwrap(String condition) {
        if (condition.startsWith("(") && condition.endsWith(")") && isOuterParenthesis(condition)) {
            return unwrap(condition.substring(1, condition.length() - 1));
        } else {
            return condition;
        }
    }

    private static boolean isOuterParenthesis(String condition) {
        int depth = 0;
        for (int i = 0; i < condition.length(); i++) {
            char c = condition.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if ((depth == 0) && (i < condition.length() - 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * El panel de notas es del expediente, así que solo va tras el footer de un {@code <form state=...>}.
     * Un {@code <footer>} de cualquier otro form (el de una entidad hija) no lo lleva: su modelo no es
     * un expediente.
     */
    private static boolean isFooterDeFormDeEstado(Element footer) {
        Node parent = footer.getParentNode();

        return (parent instanceof Element) && ((Element) parent).hasAttribute("state");
    }
    
    
  

}
