/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Test;

import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.rest.UserInfo;
import com.jayway.jsonpath.JsonModel;

public class CurrentUserResourceTest extends UsersRestApplicationJerseyTest {

    @Test
    public void testGetCurrentUser() {
        User user = mock(User.class);
        when(securityContext.getUserPrincipal()).thenReturn(user);
        when(userService.getUser(13L)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(13L);
        when(user.getName()).thenReturn("Admin");
        when(user.getDescription()).thenReturn("Administrator");
        when(user.getDomain()).thenReturn("Local");
        when(user.getLocale()).thenReturn(Optional.of(Locale.ENGLISH));
        when(user.getCreationDate()).thenReturn(Instant.now());
        when(user.getModifiedDate()).thenReturn(Instant.now());
        when(user.getGroups()).thenReturn(Arrays.asList());
        when(user.getPrivileges(anyString())).thenReturn(Collections.emptySet());
        when(user.getVersion()).thenReturn(1L);
        
        UserInfo response = target("/currentuser").request().get(UserInfo.class);
        
        assertThat(response.id).isEqualTo(13);
        assertThat(response.authenticationName).isEqualTo("Admin");
        assertThat(response.description).isEqualTo("Administrator");
        assertThat(response.domain).isEqualTo("Local");
        assertThat(response.language.languageTag).isEqualTo("en");
        assertThat(response.language.displayValue).isEqualTo("English");
        assertThat(response.groups).isEmpty();
        assertThat(response.createdOn).isNotEmpty();
        assertThat(response.modifiedOn).isNotEmpty();
    }
    
    @Test
    public void testGetPreferences() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(13L);
        when(userService.getUser(13L)).thenReturn(Optional.of(user));
        when(securityContext.getUserPrincipal()).thenReturn(user);
        when(user.getLocale()).thenReturn(Optional.of(Locale.ENGLISH));
        List<UserPreference> preferences = Arrays.asList(
            mockUserPreference(PreferenceType.SHORT_DATE, "short date"),
            mockUserPreference(PreferenceType.SHORT_TIME, "short time"),
            mockUserPreference(PreferenceType.DATETIME_ORDER, "DT"),
            mockUserPreference(PreferenceType.DATETIME_SEPARATOR, "-")
        );
        when(userPreferencesService.getPreferences(user)).thenReturn(preferences);
        
        String response = target("/currentuser/preferences").request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        assertThat(model.<List<Object>>get("$.preferences")).hasSize(4);
        assertThat(model.<List<String>>get("$.preferences[*].key")).containsExactly(PreferenceType.SHORT_DATE.getTranslationKey(), PreferenceType.SHORT_TIME.getTranslationKey(), PreferenceType.DATETIME_ORDER.getTranslationKey(), PreferenceType.DATETIME_SEPARATOR.getTranslationKey());
        assertThat(model.<List<String>>get("$.preferences[*].value")).containsExactly("short date", "short time", "DT", "-");
    }
    
    private UserPreference mockUserPreference(PreferenceType key, String formatFE) {
        UserPreference up = mock(UserPreference.class);
        when(up.getType()).thenReturn(key);
        when(up.getDisplayFormat()).thenReturn(formatFE);
        return up;
    }
}
