/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.templates.UserTpl;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class WorkGroupBuilder extends NamedBuilder<WorkGroup, WorkGroupBuilder> {

    private final UserService userService;
    private String description;
    private List<UserTpl> users = new ArrayList<>();

    @Inject
    public WorkGroupBuilder(UserService userService) {
        super(WorkGroupBuilder.class);
        this.userService = userService;
        this.description = "";
    }

    /**
     * Sets the user work group description
     * @param description the new value for description
     * @return itself (allowing method chaining)
     */
    public WorkGroupBuilder withDescription(String description){
        this.description = description;
        return this;
    }

    public WorkGroupBuilder withUsers(List<UserTpl> users) {
        this.users = users;
        return this;
    }

    @Override
    public Optional<WorkGroup> find() {
        return Optional.of(create());
    }

    @Override
    public WorkGroup create() {
        Log.write(this);
        WorkGroup workGroup = userService.getWorkGroup(getName()).orElse(null);
        List<String> eligibleUsers = users.stream().map(UserTpl::name).collect(Collectors.toList());
        if(workGroup == null){
            workGroup = userService.createWorkGroup(getName(), this.description);
            userService.getUsers().stream()
                    .filter(user -> eligibleUsers.contains(user.getName()))
                    .forEach(workGroup::grant);
            workGroup.update();
        }
        return workGroup;
    }
}
