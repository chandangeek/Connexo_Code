package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModelUpgrader;

public interface Upgrader {

    void migrate(DataModelUpgrader dataModelUpgrader) throws Exception;
}
