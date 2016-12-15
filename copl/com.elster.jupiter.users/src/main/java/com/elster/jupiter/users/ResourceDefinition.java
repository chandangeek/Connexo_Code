package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface ResourceDefinition extends Resource, HasName {
    List<String> getPrivilegeNames();
}