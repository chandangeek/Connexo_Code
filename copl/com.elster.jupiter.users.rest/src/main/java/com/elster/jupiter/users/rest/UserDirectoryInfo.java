package com.elster.jupiter.users.rest;


import com.elster.jupiter.users.LdapUserDirectory;

public class UserDirectoryInfo {

    public long id;
    public String name;
    public String prefix;
    public String url;
    public boolean isDefault;
    public String security;
    public String backupurl;
    public String baseUser;
    public String baseGroup;
    public String type;

    public UserDirectoryInfo(){

    }

    public UserDirectoryInfo(LdapUserDirectory ldapUserDirectory){
        id = ldapUserDirectory.getId();
        name = ldapUserDirectory.getDomain();
        prefix = ldapUserDirectory.getPrefix();
        url = ldapUserDirectory.getUrl();
        isDefault = ldapUserDirectory.isDefault();
        security = ldapUserDirectory.getSecurity();
        backupurl = ldapUserDirectory.getBackupUrl();
        baseGroup = ldapUserDirectory.getBaseGroup();
        baseUser = ldapUserDirectory.getBaseUser();
        type = ldapUserDirectory.getType();
    }


}
