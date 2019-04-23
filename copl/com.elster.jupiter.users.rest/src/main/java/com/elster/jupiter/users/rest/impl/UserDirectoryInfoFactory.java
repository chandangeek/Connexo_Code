/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.rest.UserDirectoryInfo;
import com.elster.jupiter.users.rest.UserDirectoryInfos;

import javax.inject.Inject;

public class UserDirectoryInfoFactory {
    private final SecurityManagementService securityManagementService;

    @Inject
    public UserDirectoryInfoFactory(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    public UserDirectoryInfo asInfo(LdapUserDirectory ldapUserDirectory) {
        UserDirectoryInfo userDirectoryInfo = new UserDirectoryInfo();
        userDirectoryInfo.id = ldapUserDirectory.getId();
        userDirectoryInfo.name = ldapUserDirectory.getDomain();
        userDirectoryInfo.url = ldapUserDirectory.getUrl();
        userDirectoryInfo.isDefault = ldapUserDirectory.isDefault();
        userDirectoryInfo.securityProtocol = ldapUserDirectory.getSecurity();
        userDirectoryInfo.backupUrl = ldapUserDirectory.getBackupUrl();
        userDirectoryInfo.baseGroup = ldapUserDirectory.getBaseGroup();
        userDirectoryInfo.baseUser = ldapUserDirectory.getBaseUser();
		userDirectoryInfo.groupName = ldapUserDirectory.getGroupName();
        userDirectoryInfo.type = ldapUserDirectory.getType();
        userDirectoryInfo.directoryUser = ldapUserDirectory.getDirectoryUser();
        securityManagementService.getUserDirectoryCertificateUsage(ldapUserDirectory).ifPresent(directoryCertificateUsage -> {
            userDirectoryInfo.certificateAlias = directoryCertificateUsage.getCertificate().map(CertificateWrapper::getAlias).orElse(null);
            userDirectoryInfo.trustStore = directoryCertificateUsage.getTrustStore().map(ts -> new LongIdWithNameInfo(ts.getId(), ts.getName())).orElse(null);
        });
        return userDirectoryInfo;
    }

    public UserDirectoryInfos asInfoList(Iterable<? extends LdapUserDirectory> userDirectories) {
        UserDirectoryInfos userDirectoryInfos = new UserDirectoryInfos();
        for (LdapUserDirectory userDirectory : userDirectories) {
            userDirectoryInfos.add(asInfo(userDirectory));
        }
        return userDirectoryInfos;
    }
}
