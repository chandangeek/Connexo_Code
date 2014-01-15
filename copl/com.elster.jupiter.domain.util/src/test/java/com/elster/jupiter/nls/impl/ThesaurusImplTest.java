package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.google.inject.Provider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThesaurusImplTest {

    private static final String COMPONENT = "DUM";
    private ThesaurusImpl thesaurus;

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private DataModel dataModel;

    @Before
    public void setUp() {
        when(threadPrincipalService.getLocale()).thenReturn(Locale.ITALY);

        NlsKeyImpl key = new NlsKeyImpl(dataModel).init(COMPONENT, Layer.DOMAIN, "coat");
        key.add(Locale.ITALY, "cappotto");

        thesaurus = new ThesaurusImpl(threadPrincipalService, new Provider<NlsKeyImpl>() {
            @Override
            public NlsKeyImpl get() {
                return new NlsKeyImpl(dataModel);
            }
        }).init(COMPONENT, Arrays.asList(key));
    }

    @After
    public void tearDown() {

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
    public void testGetStringNoLocale() throws Exception {
        when(threadPrincipalService.getLocale()).thenReturn(Locale.GERMAN);

        assertThat(thesaurus.getString("noKey", "default")).isEqualTo("default");
    }


}
