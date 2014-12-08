package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.orm.DataModel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Installer for the Device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (10:48)
 */
public class Installer {

    private final DataModel dataModel;
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    public Installer(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}