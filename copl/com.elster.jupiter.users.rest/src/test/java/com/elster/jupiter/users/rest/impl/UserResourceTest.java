package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.rest.LocaleInfo;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.rest.UserInfoFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserResourceTest extends UsersRestApplicationJerseyTest {

    private UserInfoFactory userInfoFactory;
    private GroupInfoFactory groupInfoFactory = new GroupInfoFactory(threadPrincipalService);

    @Before
    public void setupFactories() {
        userInfoFactory = new UserInfoFactoryImpl(threadPrincipalService, groupInfoFactory);
    }

    @Test
    public void testNothingToUpdate() {
        User user = mockUser(1L);
        UserInfo info = userInfoFactory.from(mock(NlsService.class), user);

        target("/users/1").request().put(Entity.json(info));

        verify(user, VerificationModeFactory.times(0)).setDescription("description");
        verify(user, VerificationModeFactory.times(0)).setLocale(Locale.ENGLISH);
        verify(user, VerificationModeFactory.times(0)).update();
    }

    @Test
    public void testUpdateUserLocale() {
        User user = mockUser(1L);
        UserInfo info = userInfoFactory.from(mock(NlsService.class), user);
        info.language = new LocaleInfo();
        info.language.languageTag = Locale.US.toLanguageTag();

        target("/users/1").request().put(Entity.json(info));

        verify(user).setLocale(Locale.US);
        verify(user).update();
    }

    @Test
    public void testReleaseUserLocale() {
        User user = mockUser(1L);
        UserInfo info = userInfoFactory.from(mock(NlsService.class), user);
        info.language = null;

        target("/users/1").request().put(Entity.json(info));

        verify(user).setLocale(null);
        verify(user).update();
    }

    @Test
    public void testUpdateDescription() {
        User user = mockUser(1L);
        UserInfo info = userInfoFactory.from(mock(NlsService.class), user);
        info.description = "new description";

        target("/users/1").request().put(Entity.json(info));

        verify(user).setDescription("new description");
        verify(user).update();
    }

    @Test
    public void testUpdateWithConcurrentModification() {
        User user = mockUser(1L);
        reset(userService);
        when(userService.findAndLockUserByIdAndVersion(1L, 1L)).thenReturn(Optional.empty());
        when(userService.getUser(1L)).thenReturn(Optional.empty());
        UserInfo info = userInfoFactory.from(mock(NlsService.class), user);

        Response response = target("/users/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    private User mockUser(long id) {
        User user = mock(User.class);
        when(userService.getUser(id)).thenReturn(Optional.of(user));
        when(userService.findAndLockUserByIdAndVersion(id, 1L)).thenReturn(Optional.of(user));
        when(user.getLocale()).thenReturn(Optional.of(Locale.ENGLISH));
        when(user.getLanguage()).thenReturn("en");
        when(user.getCreationDate()).thenReturn(Instant.now());
        when(user.getModifiedDate()).thenReturn(Instant.now());
        when(user.getDescription()).thenReturn("description");
        when(user.getId()).thenReturn(id);
        when(user.getVersion()).thenReturn(1L);
        return user;
    }
}
