package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface Privilege extends HasName {
    void delete();

    PrivilegeCategory getCategory();
}
