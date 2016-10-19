package com.elster.jupiter.users;

import com.elster.jupiter.users.impl.UsersInWorkGroup;

import java.time.Instant;
import java.util.List;

/**
 * Created by albertv on 10/19/2016.
 */
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

}
