/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

public interface ResourceBuilder {
    ResourceBuilder component(String component);
    ResourceBuilder name(String name);
    ResourceBuilder description(String description);
    PrivilegeBuilder addPrivilege(String name);
    GrantPrivilegeBuilder addGrantPrivilege(String name);
    Resource create();

    interface PrivilegeBuilder {
        PrivilegeBuilder in(PrivilegeCategory category);
        ResourceBuilder add();

    }

    interface GrantPrivilegeBuilder {
        GrantPrivilegeBuilder in(PrivilegeCategory category);
        GrantPrivilegeBuilder forCategory(PrivilegeCategory privilegeCategory);
        ResourceBuilder add();
    }

}
