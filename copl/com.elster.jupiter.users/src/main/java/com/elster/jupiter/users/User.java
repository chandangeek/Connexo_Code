package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import java.security.Principal;

public interface User extends Principal, HasName {

    long getId();

    boolean hasPrivilege(String privilege);

    boolean isMemberOf(String groupName);

    String getDescription();

    long getVersion();

    void setDescription(String description);

    void save();

    void delete();
}
