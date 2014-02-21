package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.UserService;

public abstract class AbstractLdapDirectoryImpl extends AbstractUserDirectoryImpl implements LdapUserDirectory{
    private String directoryUser;
    private String password;
    private String url;
    private boolean manageGroupsInternal;

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
}
