/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface LdapUserDirectory extends UserDirectory {

    void setDirectoryUser(String directoryUser);

    String getDirectoryUser();

    String getUrl();

    String getSecurity();

    void setSecurity(String security);

    String getBackupUrl();

    String getDescription();

    void setBackupUrl(String backupUrl);

    void setDescription(String description);

    void setUrl(String url);

    String getPassword();

    void setPassword(String password);

    String getBaseUser();

    void setBaseUser(String baseUser);

    String getBaseGroup();

    void setBaseGroup(String baseGroup);

    String getGroupName();

    void setGroupName(String groupName);

    /**
     * Applicable only when baseGroup is not null
     *
     * @return names of groups which are children of base group
     */
    List<String> getGroupNames();

}
