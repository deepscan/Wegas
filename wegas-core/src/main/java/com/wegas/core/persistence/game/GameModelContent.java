/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence.game;

import com.wegas.core.persistence.JsonSerializable;
import com.wegas.core.persistence.views.Views;
import com.wegas.core.persistence.views.WegasJsonView;
import java.io.Serializable;
import java.util.Objects;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

/**
 *
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Entity
@Table(indexes = {
    @Index(columnList = "clientscriptlibrary_gamemodelid"),
    @Index(columnList = "scriptlibrary_gamemodelid"),
    @Index(columnList = "csslibrary_gamemodelid"),
    @Index(columnList = "csslibrary_gamemodelid, scriptlibrary_gamemodelid, clientscriptlibrary_gamemodelid, contentKey", unique = true)
})
public class GameModelContent implements Serializable, JsonSerializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @WegasJsonView(Views.IndexI.class)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "csslibrary_gamemodelid")
    @JsonbTransient
    private GameModel csslibrary_GameModel;

    @ManyToOne
    @JoinColumn(name = "scriptlibrary_gamemodelid")
    @JsonbTransient
    private GameModel scriptlibrary_GameModel;

    @ManyToOne
    @JoinColumn(name = "clientscriptlibrary_gamemodelid")
    @JsonbTransient
    private GameModel clientscriptlibrary_GameModel;

    private String contentKey;

    /**
     *
     */
    private String contentType;
    /**
     *
     */
    @Lob
    @Basic(optional = false, fetch = FetchType.EAGER) // CARE, lazy fetch on Basics has some trouble.
    //@Column(columnDefinition = "text")
    //@WegasJsonView({Views.Export.class})
    private String content = "";

    /**
     *
     */
    public GameModelContent() {
    }

    /**
     *
     * @param key
     * @param content
     * @param contentType
     */
    public GameModelContent(String key, String content, String contentType) {
        this.contentKey = key;
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GameModelContent other = (GameModelContent) obj;
        if (!Objects.equals(this.contentKey, other.contentKey)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.csslibrary_GameModel, other.csslibrary_GameModel)) {
            return false;
        }
        if (!Objects.equals(this.scriptlibrary_GameModel, other.scriptlibrary_GameModel)) {
            return false;
        }
        if (!Objects.equals(this.clientscriptlibrary_GameModel, other.clientscriptlibrary_GameModel)) {
            return false;
        }
        return true;
    }

    @JsonbTransient
    public GameModel getClientscriptlibrary_GameModel() {
        return clientscriptlibrary_GameModel;
    }

    @JsonbTransient
    public void setClientscriptlibrary_GameModel(GameModel clientscriptlibrary_GameModel) {
        this.clientscriptlibrary_GameModel = clientscriptlibrary_GameModel;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    @JsonbTransient
    public GameModel getCsslibrary_GameModel() {
        return csslibrary_GameModel;
    }

    @JsonbTransient
    public void setCsslibrary_GameModel(GameModel csslibrary_GameModel) {
        this.csslibrary_GameModel = csslibrary_GameModel;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    @JsonbTransient
    public String getContentKey() {
        return contentKey;
    }

    @JsonbTransient
    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    @JsonbTransient
    public GameModel getScriptlibrary_GameModel() {
        return scriptlibrary_GameModel;
    }

    @JsonbTransient
    public void setScriptlibrary_GameModel(GameModel scriptlibrary_GameModel) {
        this.scriptlibrary_GameModel = scriptlibrary_GameModel;
    }

    @JsonbTransient
    private GameModel getGameModel() {
        if (this.clientscriptlibrary_GameModel != null) {
            return clientscriptlibrary_GameModel;
        } else if (this.scriptlibrary_GameModel != null) {
            return scriptlibrary_GameModel;
        } else {
            return csslibrary_GameModel;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 29 * hash + Objects.hashCode(csslibrary_GameModel);
        hash = 29 * hash + Objects.hashCode(scriptlibrary_GameModel);
        hash = 29 * hash + Objects.hashCode(clientscriptlibrary_GameModel);
        hash = 29 * hash + Objects.hashCode(this.contentKey);
        return hash;
    }

}
