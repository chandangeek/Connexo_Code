package com.elster.jupiter.cps.impl;

import com.elster.jupiter.orm.DataModel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs the Custom Property Set bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (14:36)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;

    public Installer(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}