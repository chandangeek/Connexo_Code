/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;

import javax.inject.Inject;
import java.util.Optional;


public class WorkGroupBuilder extends NamedBuilder<WorkGroup, WorkGroupBuilder> {

    private final UserService userService;
    private String description;

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

    @Override
    public Optional<WorkGroup> find() {
        return Optional.of(create());
    }

    @Override
    public WorkGroup create() {
        Log.write(this);
        WorkGroup workGroup = userService.getWorkGroup(getName()).orElse(null);
        if(workGroup == null){
            workGroup = userService.createWorkGroup(getName(), this.description);
            userService.getUsers().stream().forEach(workGroup::grant);
            workGroup.update();
        }
        return workGroup;
    }
}
