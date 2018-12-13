/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Collections;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThesaurusImplTest {

    private static final String COMPONENT = "DUM";
    private ThesaurusImpl thesaurus;

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private DataModel dataModel;
    @Mock
    private QueryExecutor<NlsKeyImpl> queryExecutor;

    @Before
    public void setUp() {
        when(threadPrincipalService.getLocale()).thenReturn(Locale.ITALY);
        when(dataModel.query(NlsKeyImpl.class, NlsEntry.class)).thenReturn(queryExecutor);
        when(dataModel.isInstalled()).thenReturn(true);

        NlsKeyImpl key = new NlsKeyImpl(dataModel).init(COMPONENT, Layer.DOMAIN, "coat");
        key.setDefaultMessage("coat");
        key.add(Locale.ITALY, "cappotto");

        when(queryExecutor.select(any(Condition.class))).thenReturn(Collections.singletonList(key));

        thesaurus = new ThesaurusImpl(dataModel, threadPrincipalService, () -> new NlsKeyImpl(dataModel)).init(COMPONENT, Layer.DOMAIN);
    }

    @Test
    public void testGetString() throws Exception {
        assertThat(thesaurus.getString("coat", "WRONG")).isEqualTo("cappotto");
    }

    @Test
    public void testGetStringNoKey() throws Exception {
        assertThat(thesaurus.getString("noKey", "default")).isEqualTo("default");
    }

    @Test
    public void testKeyButNoEntry() throws Exception {
        when(threadPrincipalService.getLocale()).thenReturn(Locale.ENGLISH);
        String translation = thesaurus.getString("coat", "WRONG");
        assertThat(translation).isEqualTo("coat");
    }

    @Test
    public void testGetStringNoLocale() throws Exception {
        when(threadPrincipalService.getLocale()).thenReturn(Locale.GERMAN);

        assertThat(thesaurus.getString("noKey", "default")).isEqualTo("default");
    }

}
