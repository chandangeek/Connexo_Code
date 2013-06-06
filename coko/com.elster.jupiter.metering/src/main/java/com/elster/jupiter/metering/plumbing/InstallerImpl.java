package com.elster.jupiter.metering.plumbing;

import java.lang.reflect.Field;
import java.util.*;

import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.ids.*;

import static com.elster.jupiter.ids.FieldType.*;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.impl.ReadingTypeImpl;
import com.elster.jupiter.metering.impl.ServiceCategoryImpl;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;

import static com.elster.jupiter.metering.plumbing.Bus.*;

public class InstallerImpl {	
	
	public void install(boolean executeDdl , boolean updateOrm , boolean createMasterData) {
		Bus.getOrmClient().install(executeDdl,updateOrm);
		if (createMasterData)
			createMasterData();
	}
	
	private void createMasterData() {
		IdsService idsService = Bus.getIdsService();
		createVaults(idsService);
		createRecordSpecs(idsService);
		createServiceCategories();
		createReadingTypes();		
		createPartyRoles(Bus.getPartyService());
		createPrivileges(Bus.getUserService());
	}
	
	private void createVaults(IdsService idsService) {
		Vault intervalVault = idsService.newVault(COMPONENTNAME,1,"Interval Data Store",8,true);
		intervalVault.persist();
		createPartitions(intervalVault);
		Vault registerVault = idsService.newVault(COMPONENTNAME,2,"Register Data Store",8,false);
		registerVault.persist();
		createPartitions(registerVault);
	}
	
	private void createPartitions(Vault vault) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH,1);
		cal.set(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND, 0);
		vault.activate(cal.getTime());
		for (int i = 0 ; i < 12 ;i++) {
			cal.add(Calendar.MONTH,1);
			vault.addPartition(cal.getTime());
		}
	}
	
	private void createRecordSpecs(IdsService service) {
		RecordSpec singleIntervalRecordSpec = service.newRecordSpec(COMPONENTNAME, 1, "Single Interval Data");
		singleIntervalRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
		singleIntervalRecordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
		singleIntervalRecordSpec.addFieldSpec("Value", NUMBER);
		singleIntervalRecordSpec.persist();
		RecordSpec dualIntervalRecordSpec = service.newRecordSpec(COMPONENTNAME, 2, "Dual Interval Data");
		dualIntervalRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
		dualIntervalRecordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
		dualIntervalRecordSpec.addFieldSpec("Value", NUMBER);
		dualIntervalRecordSpec.addFieldSpec("Cumulative", NUMBER);
		dualIntervalRecordSpec.persist();
		RecordSpec multiIntervalRecordSpec = service.newRecordSpec(COMPONENTNAME, 3, "Multi Interval Data");
		multiIntervalRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
		multiIntervalRecordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
		multiIntervalRecordSpec.addFieldSpec("Value1", NUMBER);
		multiIntervalRecordSpec.addFieldSpec("Value2", NUMBER);
		multiIntervalRecordSpec.addFieldSpec("Value3", NUMBER);
		multiIntervalRecordSpec.addFieldSpec("Value4", NUMBER);
		multiIntervalRecordSpec.addFieldSpec("Value5", NUMBER);
		multiIntervalRecordSpec.addFieldSpec("Value6", NUMBER);
		multiIntervalRecordSpec.persist();
		RecordSpec singleRegisterRecordSpec = service.newRecordSpec(COMPONENTNAME, 4, "Base Register");
		singleRegisterRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
		singleRegisterRecordSpec.addFieldSpec("Value", NUMBER);
		singleRegisterRecordSpec.persist();
		RecordSpec billingPeriodRegisterRecordSpec = service.newRecordSpec(COMPONENTNAME, 5, "Billing Period Register");
		billingPeriodRegisterRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
		billingPeriodRegisterRecordSpec.addFieldSpec("Value", NUMBER);
		billingPeriodRegisterRecordSpec.addFieldSpec("From Time", DATE);
		billingPeriodRegisterRecordSpec.persist();
		RecordSpec demandRegisterRecordSpec = service.newRecordSpec(COMPONENTNAME, 6, "Demand Register");
		demandRegisterRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
		demandRegisterRecordSpec.addFieldSpec("Value", NUMBER);
		demandRegisterRecordSpec.addFieldSpec("From Time", DATE);
		demandRegisterRecordSpec.addFieldSpec("Event Time", DATE);
		demandRegisterRecordSpec.persist();
	}

	private void createServiceCategories() {
		new ServiceCategoryImpl(ServiceKind.ELECTRICITY).persist();
		new ServiceCategoryImpl(ServiceKind.GAS).persist();
		new ServiceCategoryImpl(ServiceKind.WATER).persist();
		new ServiceCategoryImpl(ServiceKind.TIME).persist();
		new ServiceCategoryImpl(ServiceKind.HEAT).persist();
		new ServiceCategoryImpl(ServiceKind.REFUSE).persist();
		new ServiceCategoryImpl(ServiceKind.SEWERAGE).persist();
		new ServiceCategoryImpl(ServiceKind.RATES).persist();
		new ServiceCategoryImpl(ServiceKind.TVLICENSE).persist();
		new ServiceCategoryImpl(ServiceKind.INTERNET).persist();
		new ServiceCategoryImpl(ServiceKind.OTHER).persist();
	}
	
	private void createReadingTypes() {
		new ReadingTypeImpl("2.6.7.1.0.12.0.0.0.3.72", "15m Active+ kWh").persist();
		new ReadingTypeImpl("2.6.7.19.0.12.0.0.0.3.72", "15m Active- kWh").persist();
		new ReadingTypeImpl("2.6.7.4.0.12.0.0.0.3.72", "15m Active Net kWh").persist();
		new ReadingTypeImpl("2.6.7.5.0.12.0.0.0.3.73", "15m Reactive+ kVArh").persist();
		new ReadingTypeImpl("2.6.7.13.0.12.0.0.0.3.73", "15m Reactive- kVArh").persist();
		new ReadingTypeImpl("2.6.7.6.0.12.0.0.0.3.73", "15m Reactive Total kVArh").persist();
	}
	
	private void createPartyRoles(PartyService partyService) {
		for (MarketRoleKind role : MarketRoleKind.values()) {
			partyService.createRole(Bus.COMPONENTNAME, role.name(), role.getDisplayName() , null , null);
		}
	}
	private void createPrivileges(UserService userService) {
		for (String each : getPrivileges()) {
			userService.createPrivilege(Bus.COMPONENTNAME, each , "");
		}
	}
	
	private List<String> getPrivileges() {
		Field[] fields = Privileges.class.getFields();
		List<String> result = new ArrayList<>(fields.length);
		for (Field each : fields) {
			try {
				result.add((String) each.get(null));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	
}
