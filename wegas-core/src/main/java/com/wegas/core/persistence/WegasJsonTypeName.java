/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wegas.core.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To override @class == class simpleName
 JsonSerializable class annotated with a WegasJsonTypeName will use the
 first name in value as @class property when serialising toJson.
 *
 * @author maxence
 */
@Target(ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface WegasJsonTypeName {

    String[] value();
}
