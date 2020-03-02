package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModelUpgrader;

//sometimes you can't upgrade without me
public abstract class DummyUpgrader implements Upgrader {

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
    }
}
