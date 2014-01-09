package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;

class InstallerImpl {

    private final DataModel dataModel;

    InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install() {
        dataModel.install(true, true);
    }

}
