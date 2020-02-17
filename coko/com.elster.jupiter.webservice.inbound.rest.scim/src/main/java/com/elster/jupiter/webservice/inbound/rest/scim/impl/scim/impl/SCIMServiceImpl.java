package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.impl;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.SCIMError;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.SCIMException;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.SCIMService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.SchemaType;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.attribute.Meta;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Optional;

public class SCIMServiceImpl implements SCIMService {

    private volatile UserService userService;

    @Inject
    public SCIMServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserSchema createUser(UserSchema userSchema) {
        final User user = userService.createSCIMUser(userSchema.getUserName(), "A user provisioned by enexis provisioning tool", userSchema.getExternalId());
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

        // We are simply deactivating user
        userToBeDeleted.setStatus(false);
        userToBeDeleted.update();
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
        userSchema.setExternalId(String.valueOf(user.getId()));
        userSchema.setId(String.valueOf(user.getId()));
        userSchema.setMeta(meta);
        userSchema.setUserName(user.getName());
        userSchema.setDisplayName(user.getName());
        userSchema.setActive(user.getStatus());
        userSchema.setLocale(user.getLanguage());

        return userSchema;
    }
}
