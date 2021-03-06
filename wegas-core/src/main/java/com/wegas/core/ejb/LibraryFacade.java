/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.ejb;

import com.wegas.core.exception.client.WegasConflictException;
import com.wegas.core.exception.client.WegasErrorMessage;
import com.wegas.core.persistence.game.GameModel;
import com.wegas.core.persistence.game.GameModelContent;
import java.util.*;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 *
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Stateless
@LocalBean
public class LibraryFacade {

    /**
     *
     */
    @Inject
    private GameModelFacade gameModelFacade;

    private List<GameModelContent> getLibrary(Long gameModelId, String name) {

        GameModel gameModel = gameModelFacade.find(gameModelId);
        switch (name) {
            case "Script":
                return gameModel.getScriptLibraryList();

            case "ClientScript":
                return gameModel.getClientScriptLibraryList();

            case "CSS":
                return gameModel.getCssLibraryList();

            default:
                throw new RuntimeException("Unable to find associated library: " + name);
        }
    }

    public Map<String, GameModelContent> findLibrary(Long gameModelId, String name) {
        Map<String, GameModelContent> library = new HashMap<>();
        List<GameModelContent> list = this.getLibrary(gameModelId, name);

        for (GameModelContent gmc : list) {
            library.put(gmc.getContentKey(), gmc);
        }

        return library;
    }

    /**
     *
     * @param gameModelId
     * @param name library name
     *
     * @return get all content from the library identified by name
     */
    public String getLibraryContent(Long gameModelId, String name) {
        StringBuilder ret = new StringBuilder();
        for (GameModelContent c : this.getLibrary(gameModelId, name)) {
            ret.append(c.getContent().replaceAll("\\.\\./", ""));
            //ret.append(c.getContent());
        }
        return ret.toString();
    }

    public void create(Long gameModelId, String library, String key, GameModelContent content) {
        List<GameModelContent> lib = this.getLibrary(gameModelId, library);

        GameModel gameModel = gameModelFacade.find(gameModelId);

        if (gameModel.getGameModelContent(lib, key) == null) {
            content.setContentKey(key);
            lib.add(content);

            switch (library) {
                case "Script":
                    content.setScriptlibrary_GameModel(gameModel);
                    break;
                case "ClientScript":
                    content.setClientscriptlibrary_GameModel(gameModel);
                    break;
                case "CSS":
                    content.setCsslibrary_GameModel(gameModel);
                    break;
            }
        } else {
            throw new WegasConflictException();
        }

    }

    public void update(Long gameModelId, String library, String key, GameModelContent content) {
        List<GameModelContent> lib = this.getLibrary(gameModelId, library);

        GameModel gameModel = gameModelFacade.find(gameModelId);

        GameModelContent gameModelContent = gameModel.getGameModelContent(lib, key);
        if (gameModelContent != null) {
            gameModelContent.setContent(content.getContent());
        } else {
            throw WegasErrorMessage.error("Library does not exists");
        }
    }

    public void delete(Long gameModelId, String library, String key) {
        List<GameModelContent> lib = this.getLibrary(gameModelId, library);

        GameModel gameModel = gameModelFacade.find(gameModelId);

        GameModelContent gameModelContent = gameModel.getGameModelContent(lib, key);
        if (gameModelContent != null) {
            lib.remove(gameModelContent);
        }
    }
}
