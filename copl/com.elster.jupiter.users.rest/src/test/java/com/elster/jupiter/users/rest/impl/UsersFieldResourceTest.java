/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.jayway.jsonpath.JsonModel;

public class UsersFieldResourceTest extends UsersRestApplicationJerseyTest {
    
    @Test
    public void testGetLocales() {
        when(userPreferencesService.getSupportedLocales()).thenReturn(Arrays.asList(Locale.ENGLISH, Locale.FRENCH));
        when(threadPrincipalService.getLocale()).thenReturn(Locale.ENGLISH);
        
        String response = target("/field/locales").request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        
        assertThat(model.<List<String>>get("$.locales[*].languageTag")).containsExactly("en", "fr");
        assertThat(model.<List<String>>get("$.locales[*].displayValue")).containsExactly("English", "French");
    }
}
