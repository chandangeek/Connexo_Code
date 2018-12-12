/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.transaction.TransactionService;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WhiteBoardTest {

    @Mock
    private BundleContext bundleContext;
    @Mock
    private TransactionService transactionService;
    @Mock
    private QueryService queryService;
    @Mock
    private HttpAuthenticationService httpAuthenticationService;

    @Test
    public void testAddResource() {
        TranslationKeyProvider translationKeyProvider = new WhiteBoardImpl(bundleContext, transactionService,
                queryService, httpAuthenticationService);
        List<TranslationKey> translationKeysList = translationKeyProvider.getKeys();
        assertThat(translationKeysList.size()).isEqualTo(2);
        verify(bundleContext, times(1)).getProperty(WhiteBoardProperties.SYSTEM_IDENTIFIER.getKey());
    }
}