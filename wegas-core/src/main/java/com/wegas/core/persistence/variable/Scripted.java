/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence.variable;

import javax.json.bind.annotation.JsonbTransient;
import com.wegas.core.persistence.game.Script;
import java.util.List;

/**
 * Contains script(s), tells scripts are contained in this object.
 *
 * @author Cyril Junod (cyril.junod at gmail.com)
 */
public interface Scripted {

    /**
     *
     * @return List all contained scripts
     */
    @JsonbTransient
    List<Script> getScripts();

}
