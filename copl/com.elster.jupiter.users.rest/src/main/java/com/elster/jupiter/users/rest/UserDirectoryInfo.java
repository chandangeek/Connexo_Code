/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;


import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.impl.AbstractLdapDirectoryImpl;

public class UserDirectoryInfo {

    public long id;
    public String name;
    public String url;
    public boolean isDefault;
    public String securityProtocol;
    public String backupUrl;
    public String baseUser;
    public String baseGroup;
    public String type;
    public String password;
    public String directoryUser;

    public UserDirectoryInfo(){

    }

    public UserDirectoryInfo(LdapUserDirectory ldapUserDirectory){
        id = ldapUserDirectory.getId();
        name = ldapUserDirectory.getDomain();
        url = ldapUserDirectory.getUrl();
        isDefault = ldapUserDirectory.isDefault();
        securityProtocol = ldapUserDirectory.getSecurity();
        backupUrl = ldapUserDirectory.getBackupUrl();
        baseGroup = ldapUserDirectory.getBaseGroup();
        baseUser = ldapUserDirectory.getBaseUser();
        type = ldapUserDirectory.getType();
        directoryUser = ldapUserDirectory.getDirectoryUser();
    }


}
