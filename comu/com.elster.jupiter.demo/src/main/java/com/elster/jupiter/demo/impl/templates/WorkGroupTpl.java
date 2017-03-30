/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.WorkGroupBuilder;
import com.elster.jupiter.users.WorkGroup;


public enum WorkGroupTpl implements Template<WorkGroup, WorkGroupBuilder> {

    ALL_USERS("All Connexo users", "This is a default work group.");

    private String name;
    private String description;

    WorkGroupTpl(String name, String description){
        this.name = name;
        this.description = description;
    }

    @Override
    public Class<WorkGroupBuilder> getBuilderClass() {
        return WorkGroupBuilder.class;
    }

    @Override
    public WorkGroupBuilder get(WorkGroupBuilder builder) {
        return builder.withName(this.name).withDescription(this.description);
    }
}
