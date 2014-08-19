package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import java.util.List;

public interface Resource extends HasName {
    String getComponentName();
    String getName();
    String getDescription();

    void delete();

    Privilege createPrivilege(String name);
    List<Privilege> getPrivileges();
}
