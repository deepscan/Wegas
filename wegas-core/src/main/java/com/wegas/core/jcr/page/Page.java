/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.jcr.page;

import com.wegas.core.Helper;
import com.wegas.core.rest.util.JsonbProvider;
import java.io.StringReader;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.annotation.JsonbTransient;
import org.slf4j.LoggerFactory;

/**
 * @author Cyril Junod (cyril.junod at gmail.com)
 */
public class Page {

    static final private org.slf4j.Logger logger = LoggerFactory.getLogger(Page.class);

    static final protected String INDEX_KEY = "index";

    static final protected String NAME_KEY = "pageName";

    private static Jsonb jsonb = null;

    private String id;

    private JsonObject content;

    private String name;

    private Integer index = 0;

    /**
     * @param id
     * @param content
     */
    public Page(String id, JsonObject content) {
        this.id = id;
        this.setContent(content);
    }

    /**
     * @param n
     *
     * @throws RepositoryException
     */
    public Page(Node n) throws RepositoryException {
        this.id = n.getName();
        this.setContent(n.getProperty("content").getString());
        if (n.hasProperty(NAME_KEY)) {
            this.name = n.getProperty(NAME_KEY).getString();
        }
        if (n.hasProperty(INDEX_KEY)) {
            this.index = Helper.longToInt(n.getProperty(INDEX_KEY).getLong());
        }
    }

    /**
     *
     */
    public Page() {
    }

    /**
     * @return page id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return page content as a JSONNode
     */
    public JsonObject getContent() {
        return content;
    }

    public JsonObject getContentWithMeta() {
        return Json.createObjectBuilder(content)
                .add("@name", this.name)
                .add("@index", this.index).build();
    }

    /**
     * @param content
     */
    @JsonbTransient
    public final void setContent(JsonObject content) {
        this.content = content;
        this.extractAttrs();
    }

    private static synchronized Jsonb getJsonb() {
        if (Page.jsonb == null) {
            Page.jsonb = JsonbProvider.getMapper(null);
        }
        return Page.jsonb;
    }

    /**
     * @param content
     *
     */
    @JsonbTransient
    public final void setContent(String content) {
        try (JsonReader reader = Json.createReader(new StringReader(content))) {
            this.content = reader.readObject();
        }
        this.extractAttrs();
    }

    /**
     * @return page name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * if @name or @index exists in content, remove them
     */
    @JsonbTransient
    private void extractAttrs() {

        try {
            String v = this.content.getString("@name");
            this.name = v;
            this.content.remove("@name");
        } catch (Exception e) {
            // no name, ok
        }
        try {
            int i = this.content.getInt("@index");
            this.index = i;
            this.content.remove("@index");
        } catch (Exception e) {
            // no index, ok
        }

    }

    /**
     * @param patch RFC6902: patch Array
     */
    public void patch(JsonPatch patch) {

        JsonObject patched = patch.apply(this.getContentWithMeta());
        logger.info("INPUT\n" + this.content.toString() + "\nPATCH\n" + patch + "\nRESULT\n" + patched);
        this.setContent(patched);
    }

    //@TODO : tokenizer
    /**
     * @param jsonPath
     *
     * @return some extracted node as text
     */
    public String extract(String jsonPath) {
        JsonValue value = this.content.getValue(jsonPath);
        if (value instanceof JsonString) {
            return ((JsonString) value).getString();
        } else {
            return null;
        }
        /*
        final String[] xpaths = jsonPath.trim().split("\\.|\\[|\\]");
        for (String xpath : xpaths) {
        if (!xpath.equals("")) {
        if (node.isArray() && xpath.matches("[0-9]+")) {
        node = node.path(Integer.parseInt(xpath));
        } else {
        node = node.path(xpath);
        }
        }
        }
        return node.asText();
         */
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
