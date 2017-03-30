/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.nls.impl.NlsServiceImpl;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ReadingTypeTranslationKeys} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeTranslationKeysTest {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }


    @Test
    @Transactional
    public void testAddAllTranslations() {
        NlsServiceImpl nlsService = (NlsServiceImpl) inMemoryBootstrapModule.getNlsService();

        // Business method
        nlsService.addTranslationKeyProvider(new ReadingTypeTranslationKeyProvider());

        // Asserts
        Thesaurus thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
        ReadingTypeTranslationKeys.allKeys().forEach(translationKey ->
                assertThat(thesaurus.getFormat(translationKey).format())
                        .as("Translation for " + translationKey.getKey() + " with default format " + translationKey.getDefaultFormat() + " is null or empty")
                        .isNotEmpty());
    }

    private static class ReadingTypeTranslationKeyProvider implements TranslationKeyProvider {
        @Override
        public String getComponentName() {
            return MeteringService.COMPONENTNAME;
        }

        @Override
        public Layer getLayer() {
            return Layer.DOMAIN;
        }

        @Override
        public List<TranslationKey> getKeys() {
            return ReadingTypeTranslationKeys.allKeys();
        }
    }
}