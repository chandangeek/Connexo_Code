package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.UserService;
import com.google.inject.Inject;

import javax.naming.Context;
import java.util.Hashtable;

public abstract class AbstractLdapDirectoryImpl extends AbstractUserDirectoryImpl implements LdapUserDirectory{
    private String directoryUser;
    private String password;
    private String url;
    private String baseDN;
    private boolean manageGroupsInternal;

    final Hashtable<String, Object> commonEnvLDAP = new Hashtable<String, Object>(){{
        put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        put(Context.SECURITY_AUTHENTICATION, "simple");
        put(Context.PROVIDER_URL, getUrl());
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
    public String getBaseDN(){
        return baseDN;
    }

    @Override
    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }
}
