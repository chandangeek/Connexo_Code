package com.elster.jupiter.orm.callback;

import java.util.List;

public interface InstallService {
    void install();

    List<String> getPrerequisiteModules();
}
