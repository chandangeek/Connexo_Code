package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModelUpgrader;

// Sometimes you can't upgrade without me.
// When some upgrader is added to an obsolete release, same version should be added to the later releases
public abstract class DummyUpgrader implements Upgrader {

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
    }
}
