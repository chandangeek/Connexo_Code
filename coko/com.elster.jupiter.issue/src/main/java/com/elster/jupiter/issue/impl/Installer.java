package com.elster.jupiter.issue.impl;

import com.elster.jupiter.orm.DataModel;

public class Installer {
    /*
    If you want to add EventService see @com.elster.jupiter.parties.impl.Installer class file
     */

    private final DataModel dataModel;

    Installer (DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install(boolean executeDDL, boolean store) {
        dataModel.install(executeDDL, store);
    }

}
