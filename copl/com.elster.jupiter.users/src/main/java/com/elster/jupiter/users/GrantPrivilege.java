package com.elster.jupiter.users;

import java.util.Set;

public interface GrantPrivilege extends Privilege {

    Set<PrivilegeCategory> grantableCategories();

    void addGrantableCategory(PrivilegeCategory category);

    default boolean canGrant(Privilege privilege) {
        return grantableCategories().contains(privilege.getCategory());
    }
}
