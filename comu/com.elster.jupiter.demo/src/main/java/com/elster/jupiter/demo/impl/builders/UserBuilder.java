/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link Builder} for creating {@link User}
 * and join it to the groups defined by <code>roles</code>
*/
public class UserBuilder extends NamedBuilder<User, UserBuilder> {

    public static final String DEMO_ADMIN_PASSWORD = "D3moAdmin";
    public static final String DEMO_PASSWORD = "D3mo";

    private final UserService userService;

    private String description;
    private String password;
    private String[] roles;
    private String language;

    @Inject
    public UserBuilder(UserService userService) {
        super(UserBuilder.class);
        this.userService = userService;
        this.password = DEMO_ADMIN_PASSWORD;
        this.description = "";
        this.language = Locale.ENGLISH.toLanguageTag();
    }

    /**
     * Sets the user's password
     * @param password the new password
     * @return itself (allowing method chaining)
     */
    public UserBuilder withPassword(String password){
        this.password = password;
        return this;
    }

    /**
     * The user will belong to the {@link Group} with the given name
     * @param role of the group(s) to join
     * @return itself (allowing method chaining)
     */
    public UserBuilder withRoles(String... role){
        this.roles = role;
        return this;
    }

    /**
     * Sets the user's description
     * @param description the new value for description
     * @return itself (allowing method chaining)
     */
    @SuppressWarnings("unused")
    public UserBuilder withDescription(String description){
        this.description = description;
        return this;
    }

    /**
     * Sets the user's language
     * @param lang the language
     * @return itself (allowing method chaining)
     */
    public UserBuilder withLanguage(String lang){
        this.language = lang;
        return this;
    }

    @Override
    public Optional<User> find() {
        return Optional.of(create());
    }

    @Override
    public User create() {
        Log.write(this);
        User user = userService.findUser(getName()).orElse(null);
        if (user == null){
            user = userService.createUser(getName(), description);
        }
        user.setPassword(this.password);
        user.setLocale(Locale.forLanguageTag(language));
        if (roles != null){
            Map<String, Group> allGroups = userService.getGroups().stream().collect(Collectors.toMap(Group::getName, g -> g));
            for (String role : roles) {
                Group group = allGroups.get(role);
                if (group == null){
                    throw new UnableToCreate("Role '" + role + "' doesn't exist! Full list: " + allGroups.keySet());
                }
                user.join(group);
            }
        }
        user.update();
        return user;
    }

}