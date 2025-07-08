package com.educaflow.common.mapper;

import com.axelor.db.Model;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeanMapperModel {

    public static Object getEntityCloned(Class<? extends Model> clazz, Model entity) {
        if (entity == null) {
            return null;
        }

        return getEntityCloned(clazz, entity, null, null);
    }

    public static Object getEntityCloned(Class<? extends Model> clazz, Model entity, String mappedBy, Model mappedByModel) {
        try {
            if (entity == null) {
                return null;
            }


            Model entityDest = clazz.getDeclaredConstructor().newInstance();
            copyEntityToEntity(clazz, entity, entityDest, mappedBy, mappedByModel);

            return entityDest;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyEntityToEntity(Class<? extends Model> clazz, Model entity, Model entityDest) {
        copyEntityToEntity(clazz, entity, entityDest, null, null);
    }

    public static void copyEntityToEntity(Class<? extends Model> clazz, Model entity, Model entityDest, String mappedBy, Model mappedByModel) {
        try {
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(clazz);

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getName().equals("class")) {
                    continue;
                }
                if (propertyDescriptor.getWriteMethod() == null) {
                    continue;
                }

                if (propertyDescriptor.getName().equals(mappedBy)) {
                    PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), mappedByModel);
                    continue;
                }

                if (ScalarMapper.isScalarType(propertyDescriptor.getPropertyType())) {
                    Object rawValue = PropertyUtils.getProperty(entity, propertyDescriptor.getName());

                    //Obtener valor real
                    Object value = ScalarMapper.getScalarFromObject(rawValue, propertyDescriptor.getPropertyType());

                    PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), value);
                } else if (Model.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    Object rawValue = PropertyUtils.getProperty(entity, propertyDescriptor.getName());
                    Object value = getEntityCloned((Class<? extends Model>) propertyDescriptor.getPropertyType(), (Model) rawValue);
                    PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), value);
                } else if (List.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    List<? extends Model> rawValue = (List<? extends Model>) PropertyUtils.getProperty(entity, propertyDescriptor.getName());
                    String mappedByRelation = BeanMapperUtil.getMappedByInOneToMany(clazz, propertyDescriptor.getName());

                    Class<? extends Model> tipoListaClass = (Class<? extends Model>) ((ParameterizedType) propertyDescriptor.getReadMethod().getGenericReturnType()).getActualTypeArguments()[0];
                    List value = new ArrayList();
                    for (Model model : rawValue) {
                        Object itemValue = getEntityCloned(tipoListaClass, model, mappedByRelation, entityDest);
                        value.add(itemValue);
                    }

                    PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), value);
                } else {
                    throw new RuntimeException("Unsupported property type: " + propertyDescriptor.getPropertyType());
                }

            }
        } catch (Exception ex) {
            throw new RuntimeException(clazz.getName() + " " + entity, ex);
        }
    }

    public static void copyMapToEntity(Class<? extends Model> clazz, Map<String, Object> entityMap, Model entityDest) {
        copyMapToEntity(clazz, entityMap, entityDest, null, null);
    }

    public static void copyMapToEntity(Class<? extends Model> clazz, Map<String, Object> entityMap, Model entityDest, String mappedBy, Model mappedByModel) {
        try {
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(clazz);

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                try {
                    if (propertyDescriptor.getName().equals(mappedBy)) {
                        PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), mappedByModel);
                        continue;
                    }

                    if (propertyDescriptor.getName().equals("class")) {
                        continue;
                    }

                    if (entityMap.containsKey(propertyDescriptor.getName())==false) {
                        continue;
                    }

                    if (propertyDescriptor.getWriteMethod() == null) {
                        continue;
                    }


                    if (ScalarMapper.isScalarType(propertyDescriptor.getPropertyType())) {
                        Object rawValue = entityMap.get(propertyDescriptor.getName());

                        //Obtener valor real
                        Object value = ScalarMapper.getScalarFromObject(rawValue, propertyDescriptor.getPropertyType());

                        PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), value);
                    } else if (Model.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                        if ((BeanMapperUtil.isOneToOne(clazz, propertyDescriptor.getName())==false) && (BeanMapperUtil.isManyToOne(clazz, propertyDescriptor.getName())==false)) {
                            throw new RuntimeException("Si una propiedad es un modelo debe ser One-to-one o Many-to-one: " + propertyDescriptor.getPropertyType());
                        }


                        Object rawValue = entityMap.get(propertyDescriptor.getName());
                        Model valueDest = (Model) PropertyUtils.getProperty(entityDest, propertyDescriptor.getName());


                        if ((rawValue == null) && (valueDest == null)) {
                            //No hacer nada
                        } else if ((rawValue == null) && (valueDest != null)) {
                            PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), null);
                        } else if ((rawValue != null) && (valueDest == null)) {
                            valueDest = ((Class<? extends Model>) propertyDescriptor.getPropertyType()).getDeclaredConstructor().newInstance();
                            copyValueToEntityAndNoChangeId((Class<? extends Model>) propertyDescriptor.getPropertyType(), rawValue, valueDest);
                            PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), valueDest);
                        } else if ((rawValue != null) && (valueDest != null)) {
                            copyValueToEntityAndNoChangeId((Class<? extends Model>) propertyDescriptor.getPropertyType(), rawValue, valueDest);
                        } else {
                            throw new RuntimeException("Error de lógica");
                        }
                    } else if (List.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                        List<Object> listSource = (List<Object>) entityMap.get(propertyDescriptor.getName());
                        List<Model> listTarget = (List<Model>) PropertyUtils.getProperty(entityDest, propertyDescriptor.getName());
                        Class<? extends Model> tipoListaClass = (Class<? extends Model>) ((ParameterizedType) propertyDescriptor.getReadMethod().getGenericReturnType()).getActualTypeArguments()[0];
                        String mappedByRelation = BeanMapperUtil.getMappedByInOneToMany(clazz, propertyDescriptor.getName());

                        if ((listSource == null) && (listTarget == null)) {
                            //No hacer nada

                        } else if ((listSource == null) && (listTarget != null)) {
                            PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), null);
                        } else if ((listSource != null) && (listTarget == null)) {
                            List<Model> listValues = new ArrayList<>();
                            for (Object rawValue : listSource) {
                                Model itemValue = ((Class<? extends Model>) tipoListaClass).getDeclaredConstructor().newInstance();
                                copyValueToEntityAndNoChangeId(tipoListaClass, rawValue, itemValue, mappedByRelation, entityDest);
                                listValues.add(itemValue);
                            }
                            PropertyUtils.setProperty(entityDest, propertyDescriptor.getName(), listValues);
                        } else if ((listSource != null) && (listTarget != null)) {
                            ModelListCompare modelListCompare = new ModelListCompare(listSource, listTarget);

                            for (Object rawValue : modelListCompare.getSourceWhereOnlySource()) {
                                Model itemValue = tipoListaClass.getDeclaredConstructor().newInstance();
                                copyValueToEntityAndNoChangeId(tipoListaClass, rawValue, itemValue, mappedByRelation, entityDest);
                                listTarget.add(itemValue);
                            }
                            for (int i = 0; i < modelListCompare.getTargetWhereSourceAndTarget().size(); i++) {
                                Model itemValue = modelListCompare.getTargetWhereSourceAndTarget().get(i);
                                Object rawValue = modelListCompare.getSourceWhereSourceAndTarget().get(i);
                                copyValueToEntityAndNoChangeId(tipoListaClass, rawValue, itemValue, mappedByRelation, entityDest);
                            }
                            for (int i = 0; i < modelListCompare.getTargetWhereOnlyTarget().size(); i++) {
                                Model itemValue = modelListCompare.getTargetWhereSourceAndTarget().get(i);
                                listTarget.remove(itemValue);
                            }


                        } else {
                            throw new RuntimeException("Error de lógica");
                        }


                    } else {
                        throw new RuntimeException("Unsupported property type: " + propertyDescriptor.getPropertyType());
                    }

                } catch (Exception ex) {
                   throw new RuntimeException("Nombre de la propiedad:"+propertyDescriptor.getName() ,ex);
                }

            }
        } catch (Exception ex) {
            throw new RuntimeException(clazz.getName(), ex);
        }
    }

    private static void copyValueToEntityAndNoChangeId(Class<? extends Model> clazz, Object rawValue, Model valueDest) {
        copyValueToEntityAndNoChangeId(clazz, rawValue, valueDest, null, null);
    }

    private static void copyValueToEntityAndNoChangeId(Class<? extends Model> clazz, Object rawValue, Model valueDest, String mappedBy, Model mappedByModel) {
        Long originalId = valueDest.getId();

        if (rawValue instanceof Model) {
            Model rawValueModel = (Model) rawValue;
            copyEntityToEntity(clazz, rawValueModel, valueDest, mappedBy, mappedByModel);
        } else if (rawValue instanceof Map) {
            Map<String, Object> rawValueMap = (Map<String, Object>) rawValue;
            copyMapToEntity(clazz, rawValueMap, valueDest, mappedBy, mappedByModel);
        } else {
            throw new RuntimeException("Unsupported property type: " + rawValue.getClass());
        }

        valueDest.setId(originalId);
    }


}
