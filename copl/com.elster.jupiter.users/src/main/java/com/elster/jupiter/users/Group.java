package com.elster.jupiter.users;

import com.elster.jupiter.users.impl.ApplicationPrivilege;
import com.elster.jupiter.util.HasName;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface Group extends HasName {

    long getId();

    boolean hasPrivilege(String applicationName, String privilegeCode);

    void grant(String applicationName, String privilegeCode);

    long getVersion();

    void save();

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

}
