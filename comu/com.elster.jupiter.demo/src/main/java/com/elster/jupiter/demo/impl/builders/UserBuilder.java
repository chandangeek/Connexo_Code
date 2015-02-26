package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserBuilder extends NamedBuilder<User, UserBuilder> {

    private final UserService userService;

    private String description;
    private String password;
    private List<String> roles;
    private String language;

    @Inject
    public UserBuilder(UserService userService) {
        super(UserBuilder.class);
        this.userService = userService;
        this.password = "admin";
        this.description = "";
        this.language = Locale.ENGLISH.toLanguageTag();
    }

    public UserBuilder withPassword(String password){
        this.password = password;
        return this;
    }

    public UserBuilder withRoles(List<String> roles){
        this.roles = roles;
        return this;
    }

    public UserBuilder withDescription(String description){
        this.description = description;
        return this;
    }

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
            Map<String, Group> allGroups = userService.getGroups().stream().collect(Collectors.toMap(g -> g.getName(), g -> g));
            for (String role : roles) {
                Group group = allGroups.get(role);
                if (group == null){
                    throw new UnableToCreate("Role '" + role + "' doesn't exist! Full list: " + allGroups.keySet());
                }
                user.join(group);
            }
        }
        user.save();
        return user;
    }
}
