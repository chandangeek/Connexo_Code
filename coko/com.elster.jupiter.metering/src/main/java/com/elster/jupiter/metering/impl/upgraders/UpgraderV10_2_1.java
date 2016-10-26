package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.ReadingTypeGeneratorForDataLogger;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_2_1 implements Upgrader {

    private static final Version VERSION = version(10, 2, 1);

    private final DataModel dataModel;
    private final ServerMeteringService meteringService;

    @Inject
    public UpgraderV10_2_1(DataModel dataModel, ServerMeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(this.dataModel, VERSION);
        this.meteringService.createAllReadingTypes(new ReadingTypeGeneratorForDataLogger().generateReadingTypes());
    }
}

