/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.util.HasExternalId;
import com.elster.jupiter.util.HasName;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Group extends HasName, HasExternalId {

    long getId();

    boolean hasPrivilege(String applicationName, String privilegeCode);

    void grant(String applicationName, String privilegeCode);

    long getVersion();

    void update();

    void delete();

    Map<String, List<Privilege>> getPrivileges();

    List<Privilege> getPrivileges(String applicationName);

    boolean hasPrivilege(String applicationName, Privilege privilege);

    boolean grant(String applicationName, Privilege privilege);

    boolean revoke(String applicationName, Privilege privilege);

    Instant getCreationDate();

    Instant getModifiedDate();

    String getDescription();

    void setDescription(String description);

    /**
     * @return the group with specified name
     */
    Optional<Group> getGroup(String name);

    Query<Group> getGroupsQuery();

    /**
     * @return group name
     */
    String getName();

}
