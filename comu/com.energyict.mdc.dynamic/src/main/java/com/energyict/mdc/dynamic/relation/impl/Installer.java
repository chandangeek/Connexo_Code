package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.orm.DataModel;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (09:40)
 */
public class Installer {

    private final DataModel dataModel;

    public Installer(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public void install(boolean executeDdl, boolean updateOrm, boolean createMasterData) {
        try {
            this.dataModel.install(executeDdl, updateOrm);
            if (createMasterData) {
                this.createMasterData();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        createEventTypes();
    }

    private void createMasterData() {
    }

    private void createEventTypes() {
    }

}