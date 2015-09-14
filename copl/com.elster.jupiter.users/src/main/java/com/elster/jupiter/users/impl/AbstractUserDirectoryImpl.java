package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_USER_DIRECTORY + "}")
public abstract class AbstractUserDirectoryImpl implements UserDirectory {
    protected final UserService userService;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String domain;
    private String name;
    private boolean isDefault;
    private String prefix;
    protected final DataModel dataModel;
    private long version;
    private long id;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String type;
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

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public long getId(){
        return  id;
    }

    @Override
    public String getPrefix(){
        return prefix;
    }

    @Override
    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    public void save() {
//        version count
        if (getId() == 0) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    public void delete(){
        dataModel.remove(this);
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public UserImpl newUser(String userName, String description, boolean allowPwdChange) {
        return UserImpl.from(dataModel, this, userName, description, allowPwdChange);
    }
}
