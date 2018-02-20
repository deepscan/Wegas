/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence;

import com.wegas.core.rest.util.JsonbProvider;
import javax.json.bind.JsonbException;
import javax.json.bind.annotation.JsonbProperty;

/**
 *
 * @author maxence
 */
public interface JsonSerializable {

    @JsonbProperty("@class")
    default public String getWegasJsonTypeName() {
        // TODO @WegasJsonType({MainSimpleName, SimpleName2, ...})
        WegasJsonTypeName cName = this.getClass().getAnnotation(WegasJsonTypeName.class);
        if (cName != null && cName.value().length > 1) {
            // specific class name exist: use the first as @class json property
            return cName.value()[0];
        } else {
            // @class is the simpleName
            return this.getClass().getSimpleName();
        }
    }

    /**
     * Serialize to JSON
     *
     * @return JSON String representing this
     *
     * @throws JsonbException fails to serialize
     *
     */
    default public String toJson() throws JsonbException {
        return this.toJson(null);
    }

    /**
     * Serialize to JSON with view
     *
     * @param view the view to use to export this
     *
     * @return JSON String representing this
     *
     * @throws JsonbException fails to serialize
     */
    default public String toJson(Class view) throws JsonbException {
        return JsonbProvider.getMapper(view).toJson(this);
    }

}
