/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013, 2014, 2015 School of Business and Engineering Vaud, Comem
 * Licensed under the MIT License
 */
package com.wegas.core.security.util;

import java.lang.annotation.*;
import javax.interceptor.InterceptorBinding;

/**
 *
 * @author Francois-Xavier Aeberhard <fx@red-agent.com>
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
@Deprecated
public @interface Secured {
}
