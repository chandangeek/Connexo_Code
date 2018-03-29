/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.rest.LocaleInfo;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.rest.UserInfoFactory;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserDirectoryResourceTest extends UsersRestApplicationJerseyTest {

    @Mock
    DirectoryCertificateUsage directoryCertificateUsage;
    @Mock
    LdapUserDirectory userDirectory;
    @Mock
    TrustStore trustStore;
    @Mock
    TrustedCertificate trustedCertificate;
    @Mock
    LdapUser ldapUser;
    @Mock
    X509Certificate certificate;

    @Test
    public void testGetUsersFromDirectory() {
        when(userService.getLdapUserDirectory(1L)).thenReturn(userDirectory);
        when(securityManagementService.getUserDirectoryCertificateUsage(userDirectory)).thenReturn(Optional.of(directoryCertificateUsage));
        when(directoryCertificateUsage.getDirectory()).thenReturn(userDirectory);
        when(userDirectory.getId()).thenReturn(1L);
        when(userDirectory.getLdapUsers()).thenReturn(Collections.singletonList(ldapUser));
        when(directoryCertificateUsage.getTrustStore()).thenReturn(Optional.of(trustStore));
        when(trustStore.getCertificates()).thenReturn(Collections.singletonList(trustedCertificate));
        when(trustedCertificate.getCertificate()).thenReturn(Optional.of(certificate));
        when(trustedCertificate.getAlias()).thenReturn("testAlias");
        when(ldapUser.getUserName()).thenReturn("testUser");

        String response = target("/userdirectories/1/extusers").request().get(String.class);

        verify(userDirectory).getLdapUsers();

        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.extusers[0].name")).isEqualTo("testUser");
    }
}
