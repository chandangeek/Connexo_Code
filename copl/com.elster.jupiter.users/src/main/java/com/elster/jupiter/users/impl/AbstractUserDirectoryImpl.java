package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_USER_DIRECTORY + "}")
public abstract class AbstractUserDirectoryImpl implements UserDirectory {
    protected final UserService userService;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(max = 128, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_128 + "}")
    private String name;
    private boolean isDefault;
    @Size(max = 128, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_128 + "}")
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
        return name;
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
        if (getId() == 0) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    public void delete(){
        if(!isDefault()) {
            dataModel.remove(this);
        }
    }

    @Override
    public void setDomain(String domain) {
        this.name = domain;
    }

    @Override
    public UserImpl newUser(String userName, String description, boolean allowPwdChange,boolean status) {
        return UserImpl.from(dataModel, this, userName, description, allowPwdChange,status);
    }

    protected Optional<User> findUser(String name) {
        Condition userCondition = Operator.EQUALIGNORECASE.compare("authenticationName", name);
        Condition domainCondition = Operator.EQUALIGNORECASE.compare("userDirectory.name", this.name);
        Condition activeCondition = Operator.EQUALIGNORECASE.compare("status", true);
        List<User> users = dataModel.query(User.class, UserDirectory.class).select(userCondition.and(domainCondition).and(activeCondition));
        if(users.isEmpty()){
            return Optional.empty();
        }else {
            return Optional.of(users.get(0));
        }
    }
}
