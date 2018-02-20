/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.rest.util;

import com.wegas.core.ejb.RequestFacade;
import com.wegas.core.persistence.views.WegasViewStrategy;
import javax.ejb.EJB;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maxece Laurent <maxence.laurent at gmail.com>
 */
@Provider
public class JsonbProvider implements ContextResolver<Jsonb> {

    @EJB
    private RequestFacade requestFacade;

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(JsonbProvider.class);

    /**
     *
     */
    //ObjectMapper mapper;
    /**
     * {@inheritDoc}
     */
    @Override
    public Jsonb getContext(Class<?> aClass) {
        Class currentView = requestFacade.getRequestManager().getView();
        logger.error("JSONB with view: {}", currentView);
        return JsonbProvider.getMapper(currentView);
    }

    /**
     *
     * @param view
     *
     * @return an ObjectMapper
     */
    public static Jsonb getMapper(Class view) {
        WegasJsonSerializer deserializer = new WegasJsonSerializer();
        JsonbConfig config= new JsonbConfig().withFormatting(true).withDeserializers(deserializer);

        if (view != null) {
            WegasViewStrategy strategy = new WegasViewStrategy(view);
            config = config.withPropertyVisibilityStrategy(strategy);
        }

        return JsonbBuilder.create(config);
    }
}
