/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.LdapGroup;

public class LdapGroupsInfo {

    public String name;

    public String description;

    public LdapGroupsInfo() {
    }

    public LdapGroupsInfo(LdapGroup ldapGroup) {
        name = ldapGroup.getName();
        description = ldapGroup.getDescription();
    }

}
