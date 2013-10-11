package com.elster.jupiter.ids.plumbing;

import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.ids.impl.RecordSpecImpl;
import com.elster.jupiter.ids.impl.VaultImpl;

import java.util.Calendar;

public class InstallerImpl {

    private static final int DEFAULT_SLOT_COUNT = 8;

    public void install(boolean executeDdl , boolean updateOrm , boolean createMasterData) {
		Bus.getOrmClient().install(executeDdl,updateOrm);
		if (createMasterData) {
            createMasterData();
        }
	}
	
	private void createMasterData() {
		createVaults();
		createRecordSpecs();
	}

	private void createVaults() {
		Vault newVault = new VaultImpl("IDS",1,"Regular TimeSeries Default ", DEFAULT_SLOT_COUNT, true);
		Bus.getOrmClient().getVaultFactory().persist(newVault);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH,1);
		cal.set(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND, 0);
		newVault.activate(cal.getTime());
		cal.add(Calendar.MONTH,1);
		newVault.addPartition(cal.getTime());		
	}
	
	private void createRecordSpecs() {
		RecordSpecImpl recordSpec = new RecordSpecImpl("IDS",1,"Simple");
		recordSpec.addFieldSpec("value" , FieldType.NUMBER);
		recordSpec.persist();
	}
	
}
