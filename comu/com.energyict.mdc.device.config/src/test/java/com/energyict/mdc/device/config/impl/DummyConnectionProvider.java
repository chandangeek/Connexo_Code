/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-16 (10:20)
 */
public final class DummyConnectionProvider implements ConnectionProvider {

    @SuppressWarnings("unused")
    private long id;

    public static void install(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel("Dummy ConnectionProvider", "for testing purposes only");
        Table<ConnectionProvider> table = dataModel.addTable("DummyConnectionProvider", ConnectionProvider.class);
        table.map(DummyConnectionProvider.class);
        Column idColumn = table.addAutoIdColumn();
        table.primaryKey("PK_DUMMYCP").on(idColumn).add();
        dataModel.register();
        dataModel.install(true, false);
    }

    @Override
    public ConnectionType getType() {
        return null;
    }

}