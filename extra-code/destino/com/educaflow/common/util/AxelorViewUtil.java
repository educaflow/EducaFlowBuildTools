package com.educaflow.common.util;

import com.axelor.db.Model;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.rpc.ActionResponse;
import com.oracle.truffle.api.profiles.Profile;

import java.util.Map;

public class AxelorViewUtil {
    public static void doResponseViewForm(ActionResponse response, String viewName, Class<? extends Model> modelClass, Model entity, String title, String profile) {
        ActionView.ActionViewBuilder actionViewBuilder=ActionView.define(title)
                .model(modelClass.getName())
                .add("form", viewName)
                .param("forceEdit", "true")
                .param("forceTitle", "true")
                .param("show-confirm", "false")
                .param("show-toolbar", "false")
                .context("_profile",profile);

        if ((entity != null)  && (entity.getId() != null)) {
            actionViewBuilder.context("_showRecord", entity.getId()).param("forceEdit", "true");
        } else {
            actionViewBuilder.context("newEntity", entity);
        }


        response.setView(actionViewBuilder.map());
    }

    public static void doResponseViewGrid(ActionResponse response, String viewName, Class<? extends Model> modelClass) {
        ActionView.ActionViewBuilder actionViewBuilder=ActionView.define("Hola")
                .model(modelClass.getName())
                .add("grid", viewName)
                .name("Pepe");

        response.setView(actionViewBuilder.map());
    }

    public static boolean existsView(String name, String type, String model) {
        AbstractView data = XMLViews.findView(name, type, model);
        if (data==null) {
            return false;
        } else {
            return true;
        }
    }

}
