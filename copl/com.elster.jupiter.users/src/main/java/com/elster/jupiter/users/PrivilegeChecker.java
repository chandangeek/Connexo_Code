/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

public interface PrivilegeChecker {
    boolean allowed(User user);
}
