package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.ExceptionCatcher;

public class Installer {
    private final DataModel dataModel;

    Installer(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
    }

    void install() {
        ExceptionCatcher.executing(
                this::installDataModel
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void installDataModel() {
        dataModel.install(true, true);
    }


}
