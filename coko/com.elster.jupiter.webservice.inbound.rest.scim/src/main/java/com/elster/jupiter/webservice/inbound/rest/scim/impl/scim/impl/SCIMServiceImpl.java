package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.impl;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasExternalId;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.SCIMError;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.SCIMException;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.SCIMService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.GroupSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.SchemaType;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.attribute.Meta;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class SCIMServiceImpl implements SCIMService {

    private volatile UserService userService;

    @Inject
    public SCIMServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserSchema createUser(UserSchema userSchema) {
        final User user = userService.createSCIMUser(userSchema.getUserName(), "A user provisioned by client provisioning tool", userSchema.getExternalId());
        user.setLocale(Locale.forLanguageTag(userSchema.getLocale()));
        user.update();
        return createUserSchemaFromUser(user);
    }

    @Override
    public UserSchema getUser(String id) {
        final Optional<User> userByExternalId = userService.findUserByExternalId(id);

        if (!userByExternalId.isPresent()) {
            throw new SCIMException(SCIMError.NOT_FOUND);
        }

        final User user = userByExternalId.get();

        if (!user.getStatus()) {
            throw new SCIMException(SCIMError.NOT_FOUND);
        }

        return createUserSchemaFromUser(user);
    }

    @Override
    public UserSchema updateUser(UserSchema userSchema) {
        final Optional<User> userByExternalId = userService.findUserByExternalId(userSchema.getExternalId());

        if (!userByExternalId.isPresent()) {
            throw new SCIMException(SCIMError.NOT_FOUND);
        }

        final User userToBeUpdated = userByExternalId.get();
        userToBeUpdated.setLocale(new Locale(userSchema.getLocale()));
        userToBeUpdated.setStatus(userSchema.isActive());
        userToBeUpdated.update();

        return createUserSchemaFromUser(userToBeUpdated);
    }

    @Override
    public void deleteUser(String id) {
        final Optional<User> userByExternalId = userService.findUserByExternalId(id);

        if (!userByExternalId.isPresent()) {
            throw new SCIMException(SCIMError.NOT_FOUND);
        }

        final User userToBeDeleted = userByExternalId.get();

        // Deactivating user by changing his status to "false"
        userToBeDeleted.setStatus(false);
        userToBeDeleted.update();
    }

    @Override
    public GroupSchema createGroup(GroupSchema groupSchema) {
        final Group group = userService.createSCIMGroup(groupSchema.getDisplayName(), "A group provisioned by client provisioning tool", groupSchema.getExternalId());
        final List<User> newGroupMembers = findUsersForMembers(groupSchema.getMembers());
        newGroupMembers.forEach(user -> user.join(group));
        group.update();
        return createGroupSchemaFromGroup(group);
    }

    @Override
    public GroupSchema getGroup(String id) {
        final Optional<Group> groupByExternalId = userService.findGroupByExternalId(id);

        if (!groupByExternalId.isPresent()) {
            throw new SCIMException(SCIMError.NOT_FOUND);
        }

        final Group group = groupByExternalId.get();

        return createGroupSchemaFromGroup(group);
    }

    @Override
    public GroupSchema updateGroup(GroupSchema groupSchema) {
        final Optional<Group> groupByExternalId = userService.findGroupByExternalId(groupSchema.getExternalId());

        if (!groupByExternalId.isPresent()) {
            throw new SCIMException(SCIMError.NOT_FOUND);
        }

        final Group groupToBeUpdated = groupByExternalId.get();
        final List<User> oldGroupMembers = getGroupMembers(groupToBeUpdated.getName());
        oldGroupMembers.forEach(user -> user.leave(groupToBeUpdated));

        final List<User> newGroupMembers = findUsersForMembers(groupSchema.getMembers());
        newGroupMembers.forEach(user -> user.join(groupToBeUpdated));

        return createGroupSchemaFromGroup(groupToBeUpdated);
    }

    @Override
    public void deleteGroup(String id) {
        final Optional<Group> groupByExternalId = userService.findGroupByExternalId(id);

        if (!groupByExternalId.isPresent()) {
            throw new SCIMException(SCIMError.NOT_FOUND);
        }

        final Group groupToBeDeleted = groupByExternalId.get();

        // There is no deactivation mechanism on groups, so that's why we just simply delete it
        groupToBeDeleted.delete();
    }

    private UserSchema createUserSchemaFromUser(final User user) {
        final Meta meta = new Meta();
        meta.setCreated(user.getCreationDate().toString());
        meta.setLastModified(user.getModifiedDate().toString());
        meta.setLocation(String.valueOf(user.getId()));
        meta.setResourceType("User");
        meta.setVersion(String.valueOf(user.getVersion()));

        final UserSchema userSchema = new UserSchema();
        userSchema.setSchemas(new String[]{SchemaType.USER_SCHEMA.getId()});
        userSchema.setExternalId(String.valueOf(user.getExternalId()));
        userSchema.setId(String.valueOf(user.getId()));
        userSchema.setMeta(meta);
        userSchema.setUserName(user.getName());
        userSchema.setDisplayName(user.getName());
        userSchema.setActive(user.getStatus());
        userSchema.setLocale(user.getLanguage());

        return userSchema;
    }

    private GroupSchema createGroupSchemaFromGroup(final Group group) {
        final Meta meta = new Meta();
        meta.setCreated(group.getCreationDate().toString());
        meta.setLastModified(group.getModifiedDate().toString());
        meta.setLocation(String.valueOf(group.getId()));
        meta.setResourceType("Group");
        meta.setVersion(String.valueOf(group.getVersion()));

        final GroupSchema groupSchema = new GroupSchema();
        groupSchema.setSchemas(new String[]{SchemaType.GROUP_SCHEMA.getId()});
        groupSchema.setExternalId(group.getExternalId());
        groupSchema.setId(String.valueOf(group.getId()));
        groupSchema.setMeta(meta);
        groupSchema.setDisplayName(group.getName());
        groupSchema.setMembers(getGroupMembersAsArray(group.getName()));

        return groupSchema;
    }

    private List<User> getGroupMembers(final String groupName) {
        return userService.getGroupMembers(groupName);
    }

    private String[] getGroupMembersAsArray(final String groupName) {
        return userService.getGroupMembers(groupName).stream().map(HasExternalId::getExternalId).toArray(String[]::new);
    }

    private List<User> findUsersForMembers(final String[] members) {
        return Arrays.stream(members)
                .map(s -> userService.findUserByExternalId(s))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}
