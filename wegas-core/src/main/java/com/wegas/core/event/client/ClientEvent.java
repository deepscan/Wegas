/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.event.client;

import com.wegas.core.persistence.JsonSerializable;
import java.io.Serializable;

/**
 *
 * @author Yannick Lagger (lagger.yannick at gmail.com)
 */
abstract public class ClientEvent implements Serializable, JsonSerializable {

    private static final long serialVersionUID = -3358736311025273367L;
}
