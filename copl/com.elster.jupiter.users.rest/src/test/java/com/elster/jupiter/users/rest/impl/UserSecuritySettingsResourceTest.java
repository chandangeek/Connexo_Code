package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.users.UserSecuritySettings;
import com.elster.jupiter.users.rest.UserSecuritySettingsInfo;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Optional;

import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserSecuritySettingsResourceTest  extends UsersRestApplicationJerseyTest{

    private final static Boolean LOCK_ACCOUNT_ACTIVE = true;
    private final static long ID = 1L;
    private final static int FAILED_LOGIN_ATTEMPTS = 2;
    private final static int LOCK_OUT_MINUTES = 5;

    private UserSecuritySettings mockActiveUserSecuritySettings() {
        UserSecuritySettings userSecuritySettings = mock(UserSecuritySettings.class);
        when(userSecuritySettings.getId()).thenReturn(ID);
        when(userSecuritySettings.isLockAccountActive()).thenReturn(LOCK_ACCOUNT_ACTIVE);
        when(userSecuritySettings.getFailedLoginAttempts()).thenReturn(FAILED_LOGIN_ATTEMPTS);
        when(userSecuritySettings.getLockOutMinutes()).thenReturn(LOCK_OUT_MINUTES);
        return userSecuritySettings;
    }

    @Test
    public void testUpdateUserSecuritySettings() {
        UserSecuritySettings userSecuritySettings = mockActiveUserSecuritySettings();
        when(userService.getLockingAccountSettings()).thenReturn(Optional.of(userSecuritySettings));

        UserSecuritySettingsInfo info = new UserSecuritySettingsInfo();
        info.id = 1L;
        info.lockAccountOption = true;
        info.failedLoginAttempts = 3;
        info.lockOutMinutes = 10;

        when(userService.findOrCreateUserSecuritySettings(info.lockAccountOption, info.failedLoginAttempts, info.lockOutMinutes)).thenReturn(userSecuritySettings);
        Entity<UserSecuritySettingsInfo> json = Entity.json(info);

        Response response = target("/userSecuritySettings/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(userSecuritySettings, VerificationModeFactory.times(1)).update();
    }

    @Test
    public void testNothingToUpdate() {
        UserSecuritySettings userSecuritySettings = mockActiveUserSecuritySettings();
        when(userService.getLockingAccountSettings()).thenReturn(Optional.of(userSecuritySettings));


        UserSecuritySettingsInfo info = new UserSecuritySettingsInfo();
        info.id = 1L;
        info.lockAccountOption = true;
        info.failedLoginAttempts = FAILED_LOGIN_ATTEMPTS;
        info.lockOutMinutes = LOCK_OUT_MINUTES;

        when(userService.findOrCreateUserSecuritySettings(info.lockAccountOption, info.failedLoginAttempts, info.lockOutMinutes)).thenReturn(userSecuritySettings);
        Entity<UserSecuritySettingsInfo> json = Entity.json(info);

        target("/userSecuritySettings/1").request().put(json);
        verify(userSecuritySettings, VerificationModeFactory.times(0)).update();
    }

    @Test
    public void testSaveUserSecuritySettings() {
        UserSecuritySettings userSecuritySettings = mockActiveUserSecuritySettings();
        when(userService.getLockingAccountSettings()).thenReturn(Optional.of(userSecuritySettings));


        UserSecuritySettingsInfo info = new UserSecuritySettingsInfo(userSecuritySettings);

        when(userService.getLockingAccountSettings()).thenReturn(Optional.empty());
        when(userService.findOrCreateUserSecuritySettings(info.lockAccountOption, info.failedLoginAttempts, info.lockOutMinutes)).thenReturn(userSecuritySettings);
        Entity<UserSecuritySettingsInfo> json = Entity.json(info);

        target("/userSecuritySettings/1").request().put(json);
        verify(userSecuritySettings, VerificationModeFactory.times(0)).update();
        verify(userSecuritySettings, VerificationModeFactory.times(1)).save();
    }
}