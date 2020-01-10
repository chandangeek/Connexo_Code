package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.impl;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.SCIMService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.SchemaType;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.attribute.Meta;

import javax.inject.Inject;
import java.util.Locale;

public class SCIMServiceImpl implements SCIMService {

    private volatile UserService userService;

    @Inject
    public SCIMServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserSchema createUser(UserSchema userSchema) {
        final User user = userService.createUser(userSchema.getUserName(), "A user provisioned by enexis provisioning tool");
        user.setLocale(Locale.forLanguageTag(userSchema.getLocale()));
        user.update();
        return createUserSchemaFromUser(user);
    }

    @Override
    public UserSchema getUser(String id) {
        return null;
    }

    @Override
    public UserSchema updateUser(UserSchema userSchema) {
        return null;
    }

    @Override
    public void deleteUser(String id) {

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
