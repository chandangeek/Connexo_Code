package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModelUpgrader;

public interface FullInstaller {

    void install(DataModelUpgrader dataModelUpgrader);

}
