package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModelUpgrader;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface Upgrader {

    void migrate(DataModelUpgrader dataModelUpgrader);
}
