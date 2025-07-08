package com.educaflow.common.util;

import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.inject.Beans;

public class AxelorDBUtil {


    public static <T extends Model> JpaRepository<T> getRepository(Class<T> classModel) {
        String fqcnRepository = getFQCNRepository(classModel);
        try {
            Class<?> repositoryClass = Class.forName(fqcnRepository);
            return (JpaRepository<T>) Beans.get(repositoryClass);
        } catch (Exception ex) {
            throw new RuntimeException("No se encontró el repositorio para la clase: " + classModel, ex);
        }
    }


    private static String getFQCNRepository(Class<? extends Model> modelClass) {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass cannot be null");
        }
        String packageName = modelClass.getPackage() != null ? modelClass.getPackage().getName() : "";
        String className = modelClass.getSimpleName();
        return packageName + ".repo." + className + "Repository";
    }
}
