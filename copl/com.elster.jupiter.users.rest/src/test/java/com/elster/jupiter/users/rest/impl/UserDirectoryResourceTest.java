/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.rest.LdapGroupsInfo;
import com.elster.jupiter.users.rest.LdapGroupsInfos;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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
        when(securityManagementService.getUserDirectoryCertificateUsage(userDirectory))
                .thenReturn(Optional.of(directoryCertificateUsage));
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

    @Test
    public void testGetExtGroups() {
        when(userService.getLdapUserDirectory(1L)).thenReturn(userDirectory);
        when(userDirectory.getGroupNames()).thenReturn(Arrays.asList("group2", "group3", "group1"));

        String response = target("/userdirectories/1/extgroups").request().get(String.class);

        verify(userDirectory).getGroupNames();

        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.extgroups[0].name")).isEqualTo("group1");
        assertThat(model.<String>get("$.extgroups[1].name")).isEqualTo("group2");
        assertThat(model.<String>get("$.extgroups[2].name")).isEqualTo("group3");
    }

    @Test
    public void testGetExtImportedGroups() {
        when(userService.getLdapUserDirectory(1L)).thenReturn(userDirectory);
        when(userDirectory.getGroupNames()).thenReturn(Arrays.asList("group2", "group3", "group5", "group4", "group1"));
        when(userService.findGroup(anyString())).thenReturn(Optional.empty());
        when(userService.findGroup("group5")).thenReturn(Optional.of(mock(Group.class)));
        when(userService.findGroup("group1")).thenReturn(Optional.of(mock(Group.class)));

        String response = target("/userdirectories/1/extimportedgroups").request().get(String.class);

        verify(userDirectory).getGroupNames();

        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.extimportedgroups[0].name")).isEqualTo("group1");
        assertThat(model.<String>get("$.extimportedgroups[1].name")).isEqualTo("group5");
    }

    @Test
    public void testSaveGroups() {
        when(userService.getLdapUserDirectory(1L)).thenReturn(userDirectory);
        when(userDirectory.getGroupNames()).thenReturn(Arrays.asList("group2", "group3", "group1"));
        LdapGroupsInfos infos = new LdapGroupsInfos();
        infos.total = 2;
        String groupName1 = "gr1";
        String groupName2 = "gr2";
        LdapGroupsInfo group1 = new LdapGroupsInfo(groupName1);
        LdapGroupsInfo group2 = new LdapGroupsInfo(groupName2);
        infos.ldapGroups = Arrays.asList(group1, group2);
        Entity<LdapGroupsInfos> entity = Entity.json(infos);

        target("/userdirectories/1/groups").request().post(entity);

        verify(userService).findOrCreateGroup(groupName1);
        verify(userService).findOrCreateGroup(groupName2);
    }
}
