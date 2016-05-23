package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModelUpgrader;

import aQute.bnd.annotation.ConsumerType;

import java.util.logging.Logger;

@ConsumerType
public interface FullInstaller {

    void install(DataModelUpgrader dataModelUpgrader, Logger logger);

}
