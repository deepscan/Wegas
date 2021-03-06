/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.i18n;

import com.wegas.core.i18n.ejb.I18nFacade;
import com.wegas.core.persistence.game.GameModelLanguage;
import com.wegas.test.arquillian.AbstractArquillianTest;
import javax.inject.Inject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author maxence
 */
public class GameModelI18nTest extends AbstractArquillianTest {

    @Inject
    private I18nFacade i18nFacade;

    @Test
    public void testI18n() {
        GameModelLanguage defaultLanguage = i18nFacade.findLanguageByName(gameModel, "default");

        Assert.assertNotNull("No default language", defaultLanguage);

        i18nFacade.createLanguage(gameModel, "en", "english");
    }
}
