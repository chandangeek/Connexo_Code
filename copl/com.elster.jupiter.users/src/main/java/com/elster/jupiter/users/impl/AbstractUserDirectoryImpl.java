package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.Map;

public abstract class AbstractUserDirectoryImpl implements UserDirectory {
    protected final UserService userService;
    private String domain;
    private boolean isDefault;
    protected final DataModel dataModel;
    private long version;
    private Instant createTime;
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    static final Map<String, Class<? extends UserDirectory>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends UserDirectory>>of(InternalDirectoryImpl.TYPE_IDENTIFIER, InternalDirectoryImpl.class, ActiveDirectoryImpl.TYPE_IDENTIFIER, ActiveDirectoryImpl.class, ApacheDirectoryImpl.TYPE_IDENTIFIER, ApacheDirectoryImpl.class);

    public AbstractUserDirectoryImpl(DataModel dataModel, UserService userService) {
        this.userService = userService;
        this.dataModel = dataModel;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getDomain() {
        return domain;
    }

    public void save() {
        dataModel.persist(this);
    }

    void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public UserImpl newUser(String userName, String description, boolean allowPwdChange) {
        return UserImpl.from(dataModel, this, userName, description, allowPwdChange);
    }
}
