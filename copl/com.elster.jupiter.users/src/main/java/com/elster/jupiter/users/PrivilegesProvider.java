package com.elster.jupiter.users;

import java.util.List;

public interface PrivilegesProvider{
    List<Resource> getModulePrivileges();
}
