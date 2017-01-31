/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThesaurusResourceTest {

    private ThesaurusResource thesaurusResource;

    @Mock
    private NlsService nlsService;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private Thesaurus thesaurus1, thesaurus2;

    @Before
    public void setUp() {
        thesaurusResource = new ThesaurusResource(nlsService, new ThesaurusCache());

        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();

        map.add("cmp", "MTR");
        map.add("cmp", "VAL");

        when(uriInfo.getQueryParameters()).thenReturn(map);
        when(nlsService.getThesaurus("MTR", Layer.REST)).thenReturn(thesaurus1);
        when(nlsService.getThesaurus("VAL", Layer.REST)).thenReturn(thesaurus2);
        when(thesaurus1.getTranslationsForCurrentLocale()).thenReturn(ImmutableMap.of("key1", "value1", "key2", "value2"));
        when(thesaurus2.getTranslationsForCurrentLocale()).thenReturn(ImmutableMap.of("key3", "value3"));

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetThesaurus() {
        ThesaurusInfo thesaurusInfo = thesaurusResource.getThesaurus(uriInfo);

        assertThat(thesaurusInfo.translations).hasSize(3);
        assertThat(thesaurusInfo.translations.get(0).cmp).isEqualTo("MTR");
        assertThat(thesaurusInfo.translations.get(0).key).isEqualTo("key1");
        assertThat(thesaurusInfo.translations.get(0).value).isEqualTo("value1");
        assertThat(thesaurusInfo.translations.get(1).cmp).isEqualTo("MTR");
        assertThat(thesaurusInfo.translations.get(1).key).isEqualTo("key2");
        assertThat(thesaurusInfo.translations.get(1).value).isEqualTo("value2");
        assertThat(thesaurusInfo.translations.get(2).cmp).isEqualTo("VAL");
        assertThat(thesaurusInfo.translations.get(2).key).isEqualTo("key3");
        assertThat(thesaurusInfo.translations.get(2).value).isEqualTo("value3");
    }

    @Test
    public void testGetThesaurusIsCached() {
        thesaurusResource.getThesaurus(uriInfo);
        thesaurusResource.getThesaurus(uriInfo);
        thesaurusResource.getThesaurus(uriInfo);

        verify(nlsService, times(1)).getThesaurus("MTR", Layer.REST);
        verify(nlsService, times(1)).getThesaurus("VAL", Layer.REST);
    }

}
