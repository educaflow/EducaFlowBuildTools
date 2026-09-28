package com.educaflow.common.buildtools.viewprocessor.tags;

import org.w3c.dom.Element;

/**
 * Añade a cada {@code <menuitem>} el {@code if} que delega su visibilidad en
 * {@code MenuSecurityService.isVisible(name)} (expuesto como {@code __config__.menuSecurity}).
 * El {@code if} de un menú no se escribe nunca a mano: quién ve cada menú se decide en
 * {@code MenuSecurityServiceImpl}, por eso si el fuente ya trae uno es un error.
 *
 * @author logongas
 */
public class MenuItem {

    public static void doMenuItem(Element menuItem) {
        String name = menuItem.getAttribute("name");

        if (menuItem.hasAttribute("if")) {
            throw new RuntimeException("El <menuitem name=\"" + name + "\"> no puede tener el atributo 'if': lo añade el preprocesador. "
                    + "Quién ve cada menú se decide en MenuSecurityServiceImpl.isVisible()");
        }

        menuItem.setAttribute("if", "__config__.menuSecurity.isVisible(\"" + name + "\")");
    }

}
