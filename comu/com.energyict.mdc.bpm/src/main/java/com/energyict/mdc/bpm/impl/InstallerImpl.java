package com.energyict.mdc.bpm.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.util.logging.Logger;

public class InstallerImpl {
    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());
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
