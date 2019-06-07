/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapGroup;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.rest.LdapGroupsInfo;
import com.elster.jupiter.users.rest.LdapGroupsInfos;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        List<LdapGroup> ldapGroups = createLdapGroups("group2", "group3", "group1");
        when(userDirectory.getLdapGroups()).thenReturn(ldapGroups);

        String response = target("/userdirectories/1/extgroups").request().get(String.class);

        verify(userDirectory).getLdapGroups();

        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.extgroups[0].name")).isEqualTo("group1");
        assertThat(model.<String>get("$.extgroups[1].name")).isEqualTo("group2");
        assertThat(model.<String>get("$.extgroups[2].name")).isEqualTo("group3");
    }

    private List<LdapGroup> createLdapGroups(String... names) {
        List<LdapGroup> groups = new ArrayList<>();
        for (String name : names) {
            LdapGroup group = mockLdapGroup(name);
            groups.add(group);
        }
        return groups;
    }

    private LdapGroup mockLdapGroup(String name) {
        LdapGroup group = mock(LdapGroup.class);
        when(group.getName()).thenReturn(name);
        return group;
    }

    @Test
    public void testGetExtImportedGroups() {
        when(userService.getLdapUserDirectory(1L)).thenReturn(userDirectory);
        List<LdapGroup> ldapGroups = createLdapGroups("group2", "group3", "group5", "group4", "group1");
        when(userDirectory.getLdapGroups()).thenReturn(ldapGroups);
        when(userService.findGroup(anyString())).thenReturn(Optional.empty());
        when(userService.findGroup("group5")).thenReturn(Optional.of(mock(Group.class)));
        when(userService.findGroup("group1")).thenReturn(Optional.of(mock(Group.class)));

        String response = target("/userdirectories/1/extimportedgroups").request().get(String.class);

        verify(userDirectory).getLdapGroups();

        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.extimportedgroups[0].name")).isEqualTo("group1");
        assertThat(model.<String>get("$.extimportedgroups[1].name")).isEqualTo("group5");
    }

    @Test
    public void testSaveGroups() {
        LdapGroupsInfos infos = new LdapGroupsInfos();
        infos.total = 2;
        String groupName1 = "gr1";
        String groupName2 = "gr2";
        String groupDescr2 = "descr2";
        LdapGroupsInfo group1 = new LdapGroupsInfo();
        group1.name = groupName1;
        LdapGroupsInfo group2 = new LdapGroupsInfo();
        group2.name = groupName2;
        group2.description = groupDescr2;
        infos.ldapGroups = Arrays.asList(group1, group2);
        Entity<LdapGroupsInfos> entity = Entity.json(infos);
        when(userService.findGroup(groupName1)).thenReturn(Optional.of(mock(Group.class)));
        when(userService.findGroup(groupName2)).thenReturn(Optional.empty());

        target("/userdirectories/groups").request().post(entity);

        verify(userService).findGroup(groupName1);
        verify(userService).findGroup(groupName2);
        verify(userService).createGroup(groupName2, groupDescr2);
        verify(userService, never()).createGroup(eq(groupName1), anyString());
    }
}
