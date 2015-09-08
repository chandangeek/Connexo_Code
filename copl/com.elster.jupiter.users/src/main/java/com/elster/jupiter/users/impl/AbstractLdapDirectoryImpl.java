package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.UserService;

import javax.naming.Context;
import java.util.Hashtable;

public abstract class AbstractLdapDirectoryImpl extends AbstractUserDirectoryImpl implements LdapUserDirectory{
    private String directoryUser;
    private String password;
    private String description;
    private String url;
    private String backupurl;
    private String security;
    private String baseUser;
    private String baseGroup;
    private boolean manageGroupsInternal;

    final Hashtable<String, Object> commonEnvLDAP = new Hashtable<String, Object>(){{
        put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        put(Context.SECURITY_AUTHENTICATION, "simple");
    }};

    public AbstractLdapDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
    }

    @Override
    public boolean isManageGroupsInternal() {
        return manageGroupsInternal;
    }

    @Override
    public String getDirectoryUser() {
        return directoryUser;
    }

    @Override
    public void setDirectoryUser(String directoryUser) {
        this.directoryUser = directoryUser;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getSecurity(){
        return security;
    }

    @Override
    public String getBackupUrl(){
        return backupurl;
    }

    @Override
    public String getDescription(){
        return description;
    }

    @Override
    public void setSecurity(String security){
        this.security = security;
    }

    @Override
    public void setDescription(String description){
        this.description = description;
    }

    @Override
    public void setBackupUrl(String backupUrl){
        this.backupurl = backupUrl;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getBaseUser(){
        return baseUser;
    }

    @Override
    public void setBaseUser(String baseUser) {
        this.baseUser = baseUser;
    }

    @Override
    public String getBaseGroup() {
        return baseGroup;
    }

    @Override
    public void setBaseGroup(String baseGroup) {
        this.baseGroup = baseGroup;
    }
}
