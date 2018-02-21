/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.jcr.page;

import java.io.StringReader;
import javax.jcr.RepositoryException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonReader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PageTest {

    final static String PAGE_NAME = "First Page";
    final static JsonObject pageContent = Json.createObjectBuilder()
            .add("type", "AbsoluteLayout")
            .add("@name", PAGE_NAME).build();

    private static final long GAME_MODEL_ID = -100L;

    @BeforeClass
    public void setup() {
    }

    @Before
    public void before() throws RepositoryException {
        // Create
        try (final Pages pages = new Pages(GAME_MODEL_ID)) {
            final Page page = new Page("1", Json.createObjectBuilder(pageContent).build());
            pages.store(page);
        }

    }

    @After
    public void after() throws RepositoryException {
        // Delete
        try (final Pages pages = new Pages(GAME_MODEL_ID)) {
            pages.deletePage("1");
        }
        try (final Pages pages = new Pages(GAME_MODEL_ID)) {
            Assert.assertEquals(0, pages.size());
        }
    }

    @Test
    public void patch() throws RepositoryException {
        // patch the name.
        final String patchedPageName = "Patched Page";
        String strPatch = "[{\"op\": \"replace\", \"path\": \"/@name\", \"value\" : \"" + patchedPageName + "\"}]";
        try (JsonReader reader = Json.createReader(new StringReader(strPatch))) {
            JsonArray patchArray = reader.readArray();

            JsonPatch patch = Json.createPatchBuilder(patchArray).build();

            try (final Pages pages = new Pages(GAME_MODEL_ID)) {
                final Page page = pages.getPage("1");
                Assert.assertEquals(PAGE_NAME, page.getName());
                page.patch(patch);
                pages.store(page);
                System.out.println(page.getName());
            }
        }
        try (final Pages pages = new Pages(GAME_MODEL_ID)) {
            Assert.assertEquals(patchedPageName, pages.getPage("1").getName());
        }
    }

    @Test
    public void crud() throws RepositoryException {
        // Create done in before
        // Read
        try (final Pages pages = new Pages(GAME_MODEL_ID)) {
            final Page page = pages.getPage("1");
            Assert.assertEquals(1, pages.size());
            Assert.assertEquals(
                    Json.createObjectBuilder(pageContent).add("@index", 0).build(),
                    page.getContentWithMeta());
            Assert.assertEquals(PAGE_NAME, page.getName());
            Assert.assertEquals(0L, (long) page.getIndex());
        }
        // Update
        try (final Pages pages = new Pages(GAME_MODEL_ID)) {
            JsonObject jsonNode = Json.createObjectBuilder().add("@name", "Second page").build();
            final Page page1 = new Page("1", jsonNode);
            pages.store(page1);

        }
        try (final Pages pages = new Pages(GAME_MODEL_ID)) {
            Assert.assertEquals(1, pages.size());
            Assert.assertEquals("Second Page", pages.getPage("1").getName());
        }
        // Delete done in after
    }
}
