/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence.game;

import com.wegas.core.Helper;
import com.wegas.core.persistence.JsonSerializable;
import com.wegas.core.persistence.variable.Searchable;
import java.io.Serializable;
import java.util.List;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

/**
 *
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Embeddable
public class Script implements Serializable, Searchable, JsonSerializable {

    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @Lob
    private String content = "";
    /**
     *
     */
    @JsonbTransient
    private String lang = "JavaScript";

    /**
     *
     */
    public Script() {
    }

    /**
     *
     * @param content
     */
    public Script(String content) {
        this.content = content;
    }

    /**
     *
     * @param language
     * @param content
     */
    public Script(String language, String content) {
        this.lang = language;
        this.content = content;
    }

    @Override
    public Boolean containsAll(final List<String> criterias) {
        return Helper.insensitiveContainsAll(this.getContent(), criterias);
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

    /**
     * @return the language
     */
    public String getLanguage() {
        return this.lang;
    }

    /**
     *
     * @param language
     */
    public void setLanguage(String language) {
        this.lang = language;
    }

    @Override
    public String toString() {
        return "ScriptEntity(" + "language:" + this.lang + ", content:{\n" + this.content + "\n})";
    }
}
