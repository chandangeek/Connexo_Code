package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;
import org.joda.time.MutableDateTime;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.ids.FieldType.*;
import static com.elster.jupiter.metering.impl.Bus.COMPONENTNAME;

public class InstallerImpl {

    private static final int SLOT_COUNT = 8;
    private static final int MONTHS_PER_YEAR = 12;

    public void install(boolean executeDdl, boolean updateOrm, boolean createMasterData) {
        try {
            Bus.getOrmClient().install(executeDdl, updateOrm);
            if (createMasterData) {
                createMasterData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install();
        }
    }

    private void createMasterData() {
        IdsService idsService = Bus.getIdsService();
        createVaults(idsService);
        createRecordSpecs(idsService);
        createServiceCategories();
        createReadingTypes();
        createPartyRoles(Bus.getPartyService());
        createPrivileges(Bus.getUserService());
        createAmrSystems();
    }

    private void createAmrSystems() {
        AmrSystemImpl mdc = new AmrSystemImpl(1, "MDC");
        mdc.save();
        AmrSystemImpl energyAxis = new AmrSystemImpl(2, "EnergyAxis");
        energyAxis.save();
    }

    private void createVaults(IdsService idsService) {
        Vault intervalVault = idsService.newVault(COMPONENTNAME, 1, "Interval Data Store", SLOT_COUNT, true);
        intervalVault.persist();
        createPartitions(intervalVault);
        Vault registerVault = idsService.newVault(COMPONENTNAME, 2, "Register Data Store", SLOT_COUNT, false);
        registerVault.persist();
        createPartitions(registerVault);
    }

    private void createPartitions(Vault vault) {
        MutableDateTime startOfMonth = new MutableDateTime();
        startOfMonth.setMillisOfDay(0);
        startOfMonth.setMonthOfYear(1);
        startOfMonth.setDayOfMonth(1);
        vault.activate(startOfMonth.toDate());
        for (int i = 0; i < MONTHS_PER_YEAR; i++) {
            startOfMonth.addMonths(1);
            vault.addPartition(startOfMonth.toDate());
        }
    }

    private void createRecordSpecs(IdsService service) {
        int id = 0;
        RecordSpec singleIntervalRecordSpec = service.newRecordSpec(COMPONENTNAME, ++id, "Single Interval Data");
        singleIntervalRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        singleIntervalRecordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
        singleIntervalRecordSpec.addFieldSpec("Value", NUMBER);
        singleIntervalRecordSpec.persist();
        RecordSpec dualIntervalRecordSpec = service.newRecordSpec(COMPONENTNAME, ++id, "Dual Interval Data");
        dualIntervalRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        dualIntervalRecordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
        dualIntervalRecordSpec.addFieldSpec("Value", NUMBER);
        dualIntervalRecordSpec.addFieldSpec("Cumulative", NUMBER);
        dualIntervalRecordSpec.persist();
        RecordSpec multiIntervalRecordSpec = service.newRecordSpec(COMPONENTNAME, ++id, "Multi Interval Data");
        multiIntervalRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        multiIntervalRecordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
        multiIntervalRecordSpec.addFieldSpec("Value1", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value2", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value3", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value4", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value5", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value6", NUMBER);
        multiIntervalRecordSpec.persist();
        RecordSpec singleRegisterRecordSpec = service.newRecordSpec(COMPONENTNAME, ++id, "Base Register");
        singleRegisterRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        singleRegisterRecordSpec.addFieldSpec("Value", NUMBER);
        singleRegisterRecordSpec.persist();
        RecordSpec billingPeriodRegisterRecordSpec = service.newRecordSpec(COMPONENTNAME, ++id, "Billing Period Register");
        billingPeriodRegisterRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        billingPeriodRegisterRecordSpec.addFieldSpec("Value", NUMBER);
        billingPeriodRegisterRecordSpec.addFieldSpec("From Time", DATE);
        billingPeriodRegisterRecordSpec.persist();
        RecordSpec demandRegisterRecordSpec = service.newRecordSpec(COMPONENTNAME, ++id, "Demand Register");
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
        new ReadingTypeImpl("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "15m Active+ kWh").persist();
        new ReadingTypeImpl("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "15m Active- kWh").persist();
        new ReadingTypeImpl("0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "15m Active Net kWh").persist();
        new ReadingTypeImpl("0.0.2.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "15m Reactive+ kVArh").persist();
        new ReadingTypeImpl("0.0.2.4.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "15m Reactive- kVArh").persist();
        new ReadingTypeImpl("0.0.2.4.20.1.12.0.0.0.0.0.0.0.0.3.73.0", "15m Reactive Total kVArh").persist();
        new ReadingTypeImpl("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "Total Import kWh").persist();
    }

    private void createPartyRoles(PartyService partyService) {
        for (MarketRoleKind role : MarketRoleKind.values()) {
            partyService.createRole(Bus.COMPONENTNAME, role.name(), role.getDisplayName(), null, null);
        }
    }

    private void createPrivileges(UserService userService) {
        for (String each : getPrivileges()) {
            userService.createPrivilege(Bus.COMPONENTNAME, each, "");
        }
    }

    private List<String> getPrivileges() {
        Field[] fields = Privileges.class.getFields();
        List<String> result = new ArrayList<>(fields.length);
        for (Field each : fields) {
            try {
                result.add((String) each.get(null));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }


}
