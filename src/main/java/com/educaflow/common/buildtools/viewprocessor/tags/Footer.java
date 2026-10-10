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
        int sumColSpam = sumVisibleColSpan(childButtonsLeftElements, itemSpan) + sumVisibleColSpan(childButtonsRightElements, itemSpan);

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
        if (isFooterDeFormDeEstado(footer)) {
            Element notasPanel = panelFinder.findAndImport("subsysExpedientes-template-notas-panel",ownerDocument);
            parent.insertBefore(notasPanel,footer);
        }
        parent.removeChild(footer);

    }

    /**
     * Suma el colSpan de los botones que pueden verse a la vez. Dos botones cuyas condiciones de
     * visibilidad son una la negación de la otra (p.ej. {@code showIf="x"} y {@code showIf="!x"}) nunca
     * se ven juntos, así que de esa pareja solo cuenta el de mayor colSpan.
     */
    private static int sumVisibleColSpan(List<Element> buttons, int itemSpan) {
        int sum = 0;
        List<Element> paired = new ArrayList<>();

        for (Element button : buttons) {
            if (paired.contains(button)) {
                continue;
            }
            int colSpan = XMLUtil.getIntegerAttribute(button, "colSpan", itemSpan);

            Element alternative = findAlternative(button, buttons, paired);
            if (alternative != null) {
                paired.add(alternative);
                colSpan = Math.max(colSpan, XMLUtil.getIntegerAttribute(alternative, "colSpan", itemSpan));
            }
            sum = sum + colSpan;
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
