package com.elster.jupiter.system.app;

import java.util.List;

public interface SysAppService {

    String COMPONENTNAME = "SYSAPP";
    String APPLICATION_KEY = "SYS";

    List<String> getAvailablePrivileges();
}
