package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import java.security.Principal;
import java.util.List;

public interface User extends Principal, HasName {

    long getId();

    boolean hasPrivilege(String privilege);

    boolean isMemberOf(String groupName);

    String getDescription();

    long getVersion();

    void setDescription(String description);

    void save();

    void delete();

    /**
     * @param group
     * @return true, if a new membership was created, false if it already existed.
     */
    boolean join(Group group);

    /**
     * @param group
     * @return true if the membership existed, false otherwise.
     */
    boolean leave(Group group);

    boolean isMemberOf(Group group);

    List<Group> getGroups();
}
