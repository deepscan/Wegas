/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.exception.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wegas.core.persistence.JsonSerializable;
import javax.ejb.ApplicationException;


/**
 *
 * @author Cyril Junod (cyril.junod at gmail.com)
 */

@JsonIgnoreProperties({"cause", "stackTrace", "suppressed"})
@ApplicationException(rollback = true)
public abstract class WegasRuntimeException extends RuntimeException implements JsonSerializable{

    private static final long serialVersionUID = 1484932586696706035L;

    /**
     *
     */
    public WegasRuntimeException() {
        super();
    }

    public WegasRuntimeException (final Throwable t){
        super(t);
    }
    
    /**
     *
     * @param message
     */
    public WegasRuntimeException(String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public WegasRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
