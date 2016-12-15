package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/*
 * represents a collection of privileges
 */
@ProviderType
public interface Resource extends HasName {
    String getComponentName();
    String getDescription();
    void delete();
    void createPrivilege(String name);
    void createPrivilege(String name, PrivilegeCategory category);
    GrantPrivilegeBuilder createGrantPrivilege(String name);
    List<Privilege> getPrivileges();

    interface GrantPrivilegeBuilder {
        GrantPrivilegeBuilder in(PrivilegeCategory category);
        GrantPrivilegeBuilder forCategory(PrivilegeCategory privilegeCategory);
        GrantPrivilege create();
    }
}
