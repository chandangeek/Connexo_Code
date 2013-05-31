package com.elster.jupiter.parties.impl;

public class InstallerImpl {	
	
	public void install(boolean executeDdl , boolean updateOrm , boolean createMasterData) {
		Bus.getOrmClient().install(executeDdl, updateOrm);
		if (createMasterData) {
			createMasterData();
		}
	}
	
	private void createMasterData() {
	}

	
}
