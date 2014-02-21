package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.orm.DataModel;

public class InstallerImpl {
    private DataModel dataModel;

    public InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install() {
		dataModel.install(true, true);
		createMasterData();
	}

	private void createMasterData() {
	}
}
