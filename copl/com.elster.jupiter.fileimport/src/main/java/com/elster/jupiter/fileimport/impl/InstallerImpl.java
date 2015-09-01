package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.ExceptionCatcher;

class InstallerImpl {

    private final DataModel dataModel;

    InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install() {

        ExceptionCatcher.executing(
                this::installDataModel
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void installDataModel() {
        dataModel.install(true, true);
    }
}
