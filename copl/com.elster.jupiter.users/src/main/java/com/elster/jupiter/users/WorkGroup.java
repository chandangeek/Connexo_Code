/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import com.elster.jupiter.users.impl.UsersInWorkGroup;

import java.time.Instant;
import java.util.List;


public interface WorkGroup {

    long getId();

    String getName();

    String getDescription();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    List<User> getUsersInWorkGroup();

    void revoke(User user);

    void grant(User user);

    boolean hasUser(User user);

    void delete();

    void update();

    void setDescription(String description);

}
