package com.elster.jupiter.users;

import java.util.List;

public interface PrivilegesProvider{
    String getModuleName();
    List<Resource> getModulePrivileges();
}
