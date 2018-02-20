/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence.views;

import com.wegas.core.security.persistence.Permission;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.json.bind.config.PropertyVisibilityStrategy;

/**
 *
 * @author maxence
 */
public class WegasViewStrategy implements PropertyVisibilityStrategy {

    private final Class currentView;

    public WegasViewStrategy(Class view) {
        currentView = view;
    }

    @Override
    public boolean isVisible(Field field) {
        Permission.logger.trace("{} Field : {}", this.getClass().getSimpleName(), field);
        return processAnnotation(field.getAnnotation(WegasJsonView.class));
    }

    public static Field getFieldFromMethod(Method method) throws IntrospectionException, NoSuchFieldException {
        Class<?> klass = method.getDeclaringClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(klass);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (method.equals(pd.getWriteMethod()) || method.equals(pd.getReadMethod())) {
                return klass.getDeclaredField(pd.getName());
            }
        }
        return null;
    }

    private boolean processAnnotation(WegasJsonView annotation) {
        // true if no annotation or annotation view match currentView
        return annotation == null || annotation.value().isAssignableFrom(currentView);
    }

    @Override
    public boolean isVisible(Method method) {
        Permission.logger.trace("{} Method : {}", this.getClass().getSimpleName(), method);
        WegasJsonView anno = method.getAnnotation(WegasJsonView.class);
        if (anno == null) {
            try {
                Field field = getFieldFromMethod(method);
                if (field != null) {
                    anno = field.getAnnotation(WegasJsonView.class);
                }
            } catch (IntrospectionException | NoSuchFieldException ex) {
            }
        }
        return processAnnotation(anno);
    }

}
