package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import java.util.List;

public interface ResourceDefinition extends Resource, HasName {
    List<String> getPrivilegeNames();
}