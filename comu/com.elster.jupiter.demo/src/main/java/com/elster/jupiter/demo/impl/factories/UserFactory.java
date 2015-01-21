package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class UserFactory extends NamedFactory<UserFactory, User> {

    private final Store store;
    private final UserService userService;

    private String description;
    private String password;
    private List<String> roles;
    private String language;

    @Inject
    public UserFactory(Store store, UserService userService) {
        super(UserFactory.class);
        this.store = store;
        this.userService = userService;
        this.password = "D3moAdmin";
        this.description = "";
        this.language = Locale.ENGLISH.toLanguageTag();
    }

    public UserFactory withPassword(String password){
        this.password = password;
        return this;
    }

    public UserFactory withRoles(String... roles){
        if (roles != null) {
            this.roles = Arrays.asList(roles);
        }
        return this;
    }

    public UserFactory withDescription(String description){
        this.description = description;
        return this;
    }

    public UserFactory withLanguage(String lang){
        this.language = lang;
        return this;
    }

    @Override
    public User get() {
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
        store.add(User.class, user);
        return user;
    }
}
