/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2017 School of Business and Engineering Vaud, Comem
 * Licensed under the MIT License
 */
package com.wegas.core.merge.annotations;

import com.wegas.core.merge.utils.WegasCallback;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author maxence
 */
@Target(ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface WegasEntity {

    /**
     * postUpdate, preDestroy, postPersist callback
     *
     * @return entity level callbacks to apply
     */
    Class<? extends WegasCallback> callback();
}