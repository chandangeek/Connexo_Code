/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

public interface LdapGroup {

    void setName(String name);

    String getName();

    void setDescription(String description);

    String getDescription();
}
