package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.orm.DataModel;

import java.util.logging.Level;
import java.util.logging.Logger;

class InstallerImpl {

    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    private final DataModel dataModel;

    public InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install() {
        try {
            dataModel.install(true, true);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not install datamodel : " + ex.getMessage(), ex);
        }
    }
}
