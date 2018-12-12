/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.ReadingTypeGeneratorForDataLogger;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Statement;

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
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ImmutableList.of(
                        "UPDATE MTR_ENDDEVICE SET NAME = MRID, MRID = regexp_replace(lower(sys_guid()), '(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})', '\\1-\\2-\\3-\\4-\\5')",
                        "UPDATE MTR_USAGEPOINT SET NAME = MRID, MRID = regexp_replace(lower(sys_guid()), '(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})', '\\1-\\2-\\3-\\4-\\5')",
                        "UPDATE MTR_ENDDEVICEJRNL edjrnl SET NAME = MRID, MRID = (SELECT MRID FROM MTR_ENDDEVICE WHERE id = edjrnl.id)",
                        "UPDATE MTR_USAGEPOINTJRNL upjrnl SET NAME = MRID, MRID = (SELECT MRID FROM MTR_USAGEPOINT WHERE id = upjrnl.id)"
                ).forEach(command -> execute(statement, command));
            }
        });

        dataModelUpgrader.upgrade(this.dataModel, VERSION);
        this.meteringService.createAllReadingTypes(new ReadingTypeGeneratorForDataLogger().generateReadingTypes());
    }
}
