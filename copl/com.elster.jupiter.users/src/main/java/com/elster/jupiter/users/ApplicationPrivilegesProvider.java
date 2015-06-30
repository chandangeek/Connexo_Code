package com.elster.jupiter.users;

import java.util.List;

public interface ApplicationPrivilegesProvider{
    List<String> getApplicationPrivileges();
    String getApplicationName();
}
