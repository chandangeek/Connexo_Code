package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import javax.inject.Inject;
import javax.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;


public class WorkGroupImpl implements WorkGroup {

    @SuppressWarnings("unused")
    private long id;
    @Size(max = NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String name;
    @Size(max = DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String description;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private List<UsersInWorkGroup> usersInWorkGroups = new ArrayList<>();
    private final DataModel dataModel;

    @Inject
    WorkGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    WorkGroupImpl init(String name, String description) {
        this.name = name;
        this.description = description;
        return this;
    }

    static WorkGroupImpl from(DataModel dataModel, String name, String description) {
        return dataModel.getInstance(WorkGroupImpl.class).init(name, description);
    }

    public void update() {
        Save.action(this.id).save(this.dataModel, this);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public List<User> getUsersInWorkGroup(){
        return usersInWorkGroups.stream().map(UsersInWorkGroup::getUser).collect(Collectors.toList());
    }

    @Override
    public void revoke(User user){
        usersInWorkGroups.stream().filter(userInGroup -> userInGroup.getUser().equals(user)).forEach(UsersInWorkGroup::delete);
    }

    @Override
    public void grant(User user){
        if(!hasUser(user)){
            UsersInWorkGroup userInWorkGroup = UsersInWorkGroup.from(dataModel, this, user);
            userInWorkGroup.persist();
            usersInWorkGroups.add(userInWorkGroup);
        }
    }

    @Override
    public boolean hasUser(User user){
        return usersInWorkGroups.stream().anyMatch(userInWorkGroup -> userInWorkGroup.getUser().equals(user));
    }
}
