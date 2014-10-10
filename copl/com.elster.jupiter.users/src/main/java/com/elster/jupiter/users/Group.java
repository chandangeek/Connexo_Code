package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import java.time.Instant;
import java.util.List;

public interface Group extends HasName {

    long getId();

    boolean hasPrivilege(String privilegeCode);

    void grant(String privilegeCode);

    long getVersion();

    void save();

    void delete();

    List<Privilege> getPrivileges();

    boolean hasPrivilege(Privilege privilege);

    boolean grant(Privilege privilege);

    boolean revoke(Privilege privilege);

    Instant getCreationDate();

    Instant getModifiedDate();

    String getDescription();

    void setDescription(String description);
}
