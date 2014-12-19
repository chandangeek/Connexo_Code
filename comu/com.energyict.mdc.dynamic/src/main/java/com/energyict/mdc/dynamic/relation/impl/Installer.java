package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.orm.DataModel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (09:40)
 */
public class Installer {

    private final DataModel dataModel;
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    public Installer(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public void install(boolean executeDdl, boolean createMasterData) {
        try {
            this.dataModel.install(executeDdl, true);
            if (createMasterData) {
                this.createMasterData();
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.createEventTypes();
    }

    private void createMasterData() {
    }

    private void createEventTypes() {
    }

}