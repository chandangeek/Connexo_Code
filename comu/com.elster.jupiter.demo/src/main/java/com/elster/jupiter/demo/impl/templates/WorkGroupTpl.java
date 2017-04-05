/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.WorkGroupBuilder;
import com.elster.jupiter.users.WorkGroup;

import java.util.Arrays;
import java.util.List;


public enum WorkGroupTpl implements Template<WorkGroup, WorkGroupBuilder> {

    SYSTEM_ADMINISTRATORS("System administraters", "The group for system administrators."),
    METER_OPERATORS("Meter operators", "The group for meter operators."),
    DATA_OPERATORS("Data operators", "The group for data operators.");

    private String name;
    private String description;
    private List<UserTpl> users;

    WorkGroupTpl(String name, String description, UserTpl... users){
        this.name = name;
        this.description = description;
        this.users = Arrays.asList(users);
    }

    @Override
    public Class<WorkGroupBuilder> getBuilderClass() {
        return WorkGroupBuilder.class;
    }

    @Override
    public WorkGroupBuilder get(WorkGroupBuilder builder) {
        return builder.withName(this.name).withDescription(this.description).withUsers(users);
    }
}
