/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_8 implements Upgrader {

    private final DataModel dataModel;
    private final BasicAuthentication basicAuthentication;

    @Inject
    UpgraderV10_8(DataModel dataModel, BasicAuthentication basicAuthentication) {
        this.dataModel = dataModel;
        this.basicAuthentication = basicAuthentication;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 8));
        updateExistingTokenKey();
    }

    /**
     * Due to change of JWT id format, we need to renew JWT token for Flow
     */
    private void updateExistingTokenKey(){
        basicAuthentication.updateExistingTokenKeyWithoutTransaction();
    }

}

