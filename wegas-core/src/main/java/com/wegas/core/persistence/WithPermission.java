/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence;

import javax.json.bind.annotation.JsonbTransient;
import com.wegas.core.security.util.WegasPermission;
import java.util.Collection;

/**
 *
 * @author maxence
 */
public interface WithPermission {

    /**
     * Comma-separated list of permission, only one is required to grant the permission
     * <p>
     * <ul>
     * <li>null means no special permission required</li>
     * <li>empty string "" means completely forbidden</li>
     * </ul>
     *
     * @return
     */
    @JsonbTransient
    Collection<WegasPermission> getRequieredCreatePermission();

    @JsonbTransient
    Collection<WegasPermission> getRequieredDeletePermission();

    @JsonbTransient
    Collection<WegasPermission> getRequieredReadPermission();

    @JsonbTransient
    Collection<WegasPermission> getRequieredUpdatePermission();

}
