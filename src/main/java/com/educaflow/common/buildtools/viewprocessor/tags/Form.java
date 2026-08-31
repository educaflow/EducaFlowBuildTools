/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.viewprocessor.tags;

import com.educaflow.common.buildtools.common.TextUtil;
import com.educaflow.common.buildtools.files.tipoexpediente.Fase;
import com.educaflow.common.buildtools.files.tipoexpediente.State;
import com.educaflow.common.buildtools.viewprocessor.TipoExpedienteViewsContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

/**
 *
 * @author logongas
 */
public class Form {

    /**
     * Convierte un {@code <form state="..." profile="...">} de una fase en un form de Axelor con
     * nombre propio.
     *
     * <p>En el XML se escribe el nombre del estado tal cual, que es lo natural dentro de la carpeta
     * de su fase; el nombre de la vista, en cambio, es global, así que lleva la fase y el estado
     * como dos segmentos. Es exactamente el mismo nombre que construye en runtime
     * {@code PhaseEventManager.getViewName} a partir del {@code codePhase} y el {@code codeState}
     * guardados, y por eso las vistas no necesitan localizador aunque estén repartidas por fases.
     *
     * @param templateForm el form {@code exp-<Code>-Templates} de la raíz de la versión.
     * @param fase la fase a la que pertenece el fichero de vistas, deducida de su carpeta.
     */
    public static void doForm(Element formElement, Element templateForm, Fase fase) {
        // El Code sale del TipoExpedienteInstance.xml, no del name del form de plantillas: derivarlo
        // del name hacía que un Code sin actualizar al duplicar una versión generase las vistas de la
        // versión nueva con el nombre global de las viejas, pisándolas. Que el name del form de
        // plantillas coincida con este Code lo comprueba TipoExpedienteViewsContext.
        String nombreExpediente = fase.getTipoExpediente().getCode();

        String profile = formElement.getAttribute("profile");
        String state = formElement.getAttribute("state");
        formElement.removeAttribute("profile");
        formElement.removeAttribute("state");

        try {
            checkEstadoDeLaFase(state, fase);
            checkPerfilDelTipo(profile, fase);

            doMergeAttributes(formElement, templateForm);

            formElement.setAttribute("name", getFormName(nombreExpediente, fase.getName(), state, profile));

            if (formElement.hasAttribute("title") == false) {
                formElement.setAttribute("title", TextUtil.getHumanNameFromExpedienteName(nombreExpediente));
            }
        } catch (Exception ex) {
            throw new RuntimeException("form con profile=" + profile + " state=" + state
                    + " en la fase " + fase.getName(), ex);
        }

    }

    /**
     * Un {@code views.xml} de fase solo puede tener formularios de estados de su propia fase: el
     * nombre corto que va en el atributo se interpreta siempre dentro de ella.
     */
    private static void checkEstadoDeLaFase(String state, Fase fase) {
        for (State estadoDeLaFase : fase.getStates()) {
            if (estadoDeLaFase.getName().equals(state)) {
                return;
            }
        }

        throw new RuntimeException("El estado '" + state + "' no existe en la fase '" + fase.getName()
                + "' de " + fase.getTipoExpediente().getCode() + ": sus estados son " + fase.getStates()
                + ". En el atributo 'state' va el nombre corto de un estado de la propia fase.");
    }


    /**
     * El {@code profile} de un form se valida contra la <b>unión de los perfiles de todos los estados
     * del tipo</b>, no contra el del estado del propio form.
     *
     * <p>El perfil de una vista es el del <b>actor</b> que la mira, no el del estado: es legítimo un
     * {@code <form state="ENTRADA_DATOS" profile="RESPONSABLE">}, porque hay listados
     * (`Abierto-Expediente.xml`) que abren con perfil {@code RESPONSABLE} expedientes que están en
     * estados de perfil {@code CREADOR}. Validar contra {@code state.getProfile()} sería demasiado
     * estricto y rechazaría vistas correctas.
     *
     * <p>La unión de perfiles del tipo es exactamente el conjunto que admite el runtime en
     * {@code ExpedienteController.checkProfileDelTipoExpediente}, y es subconjunto del enum global
     * {@code Profile} porque {@code checkProfiles} ya lo garantiza. Un perfil fuera de ella produce
     * una vista que nunca se pinta.
     *
     * <p>El valor en blanco se salta: los forms de fallback (sin perfil) no llevan el atributo.
     */
    private static void checkPerfilDelTipo(String profile, Fase fase) {
        if ((profile == null) || (profile.trim().isEmpty())) {
            return;
        }

        List<String> perfilesDelTipo = fase.getTipoExpediente().getProfiles();

        if (perfilesDelTipo.contains(profile) == false) {
            throw new RuntimeException("El perfil '" + profile + "' no lo usa ningún estado de "
                    + fase.getTipoExpediente().getCode() + ": sus perfiles son " + perfilesDelTipo
                    + ". Una vista con un perfil que el tipo no usa no se pinta nunca.");
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


    /**
     * El nombre global de la vista. Debe casar con VIEW_NAME_STATE_PROFILE_FORMAT y
     * VIEW_NAME_STATE_FORMAT de {@code PhaseEventManager}, en secretaria-virtual.
     */
    private static String getFormName(String nombreExpediente, String phaseCode, String stateCode, String profile) {
        if ((profile == null) || (profile.trim().isEmpty())) {
            return "exp-" + nombreExpediente + "-" + phaseCode + "-" + stateCode + "-form";
        } else {
            return "exp-" + nombreExpediente + "-" + phaseCode + "-" + stateCode + "-" + profile + "-form";
        }
    }

}
