package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.streams.BufferedReaderIterable;
import org.joda.time.MutableDateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.ids.FieldType.*;

public class InstallerImpl {

    private static final int SLOT_COUNT = 8;
    private static final int MONTHS_PER_YEAR = 12;
    private static final String IMPORT_FILE_NAME = "enddeviceeventtypes.csv";

    private final DataModel dataModel;
    private final IdsService idsService;
    private final PartyService partyService;
    private final UserService userService;
    private final EventService eventService;

    public InstallerImpl(DataModel dataModel, IdsService idsService, PartyService partyService, UserService userService, EventService eventService) {
        this.dataModel = dataModel;
        this.idsService = idsService;
        this.partyService = partyService;
        this.userService = userService;
        this.eventService = eventService;
    }

    public void install(boolean executeDdl, boolean updateOrm, boolean createMasterData) {
        try {
            dataModel.install(executeDdl, updateOrm);
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
            eventType.install(eventService);
        }
    }

    private void createMasterData() {
        createVaults(idsService);
        createRecordSpecs(idsService);
        createServiceCategories();
        createReadingTypes();
        createPartyRoles(partyService);
        createPrivileges(userService);
        createAmrSystems();
        createEndDeviceEventTypes();
    }

    private void createEndDeviceEventTypes() {
        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(getClass().getPackage().getName().replace('.', '/') + '/' + IMPORT_FILE_NAME)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            for (String line : new BufferedReaderIterable(reader)) {
                String[] fields = line.split(",");

                for (EndDeviceType deviceType : endDeviceTypes(fields[0])) {
                    for (EndDeviceDomain domain : domains(fields[1])) {
                        for (EndDeviceSubDomain subDomain : subDomains(fields[2])) {
                            for (EndDeviceEventorAction eventOrAction : eventOrActions(fields[3])) {
                                String code = EndDeviceEventTypeCodeBuilder
                                        .type(deviceType)
                                        .domain(domain)
                                        .subDomain(subDomain)
                                        .eventOrAction(eventOrAction)
                                        .toCode();
                                dataModel.mapper(EndDeviceEventType.class).persist(EndDeviceEventTypeImpl.from(dataModel, code));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Iterable<EndDeviceEventorAction> eventOrActions(String field) {
        return "*".equals(field) ? Arrays.asList(EndDeviceEventorAction.values()) : "n/a".equalsIgnoreCase(field) ? Arrays.asList(EndDeviceEventorAction.NA) : Arrays.asList(EndDeviceEventorAction.valueOf(sanitized(field)));
    }

    private String sanitized(String field) {
        return field.toUpperCase().replaceAll("[\\-%]", "");
    }

    private Iterable<EndDeviceSubDomain> subDomains(String field) {
        Iterable<EndDeviceSubDomain> result = "n/a".equalsIgnoreCase(field) ? Arrays.asList(EndDeviceSubDomain.NA) : Arrays.asList(EndDeviceSubDomain.valueOf(sanitized(field)));
        return "*".equals(field) ? Arrays.asList(EndDeviceSubDomain.values()) : result;
    }

    private Iterable<EndDeviceDomain> domains(String field) {
        return "*".equals(field) ? Arrays.asList(EndDeviceDomain.values()) : "n/a".equalsIgnoreCase(field) ? Arrays.asList(EndDeviceDomain.NA) : Arrays.asList(EndDeviceDomain.valueOf(sanitized(field)));
    }

    private Iterable<EndDeviceType> endDeviceTypes(String field) {
        return "*".equals(field) ? Arrays.asList(EndDeviceType.values()) : "n/a".equalsIgnoreCase(field) ? Arrays.asList(EndDeviceType.NA) : Arrays.asList(EndDeviceType.valueOf(sanitized(field)));
    }

    private void createAmrSystems() {
        AmrSystemImpl mdc = AmrSystemImpl.from(dataModel, 1, "MDC");
        mdc.save();
        AmrSystemImpl energyAxis = AmrSystemImpl.from(dataModel, 2, "EnergyAxis");
        energyAxis.save();
    }

    private void createVaults(IdsService idsService) {
        Vault intervalVault = idsService.newVault(MessageService.COMPONENTNAME, 1, "Interval Data Store", SLOT_COUNT, true);
        intervalVault.persist();
        createPartitions(intervalVault);
        Vault registerVault = idsService.newVault(MessageService.COMPONENTNAME, 2, "Register Data Store", SLOT_COUNT, false);
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
        RecordSpec singleIntervalRecordSpec = service.newRecordSpec(MessageService.COMPONENTNAME, ++id, "Single Interval Data");
        singleIntervalRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        singleIntervalRecordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
        singleIntervalRecordSpec.addFieldSpec("Value", NUMBER);
        singleIntervalRecordSpec.persist();
        RecordSpec dualIntervalRecordSpec = service.newRecordSpec(MessageService.COMPONENTNAME, ++id, "Dual Interval Data");
        dualIntervalRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        dualIntervalRecordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
        dualIntervalRecordSpec.addFieldSpec("Value", NUMBER);
        dualIntervalRecordSpec.addFieldSpec("Cumulative", NUMBER);
        dualIntervalRecordSpec.persist();
        RecordSpec multiIntervalRecordSpec = service.newRecordSpec(MessageService.COMPONENTNAME, ++id, "Multi Interval Data");
        multiIntervalRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        multiIntervalRecordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
        multiIntervalRecordSpec.addFieldSpec("Value1", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value2", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value3", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value4", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value5", NUMBER);
        multiIntervalRecordSpec.addFieldSpec("Value6", NUMBER);
        multiIntervalRecordSpec.persist();
        RecordSpec singleRegisterRecordSpec = service.newRecordSpec(MessageService.COMPONENTNAME, ++id, "Base Register");
        singleRegisterRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        singleRegisterRecordSpec.addFieldSpec("Value", NUMBER);
        singleRegisterRecordSpec.persist();
        RecordSpec billingPeriodRegisterRecordSpec = service.newRecordSpec(MessageService.COMPONENTNAME, ++id, "Billing Period Register");
        billingPeriodRegisterRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        billingPeriodRegisterRecordSpec.addFieldSpec("Value", NUMBER);
        billingPeriodRegisterRecordSpec.addFieldSpec("From Time", DATE);
        billingPeriodRegisterRecordSpec.persist();
        RecordSpec demandRegisterRecordSpec = service.newRecordSpec(MessageService.COMPONENTNAME, ++id, "Demand Register");
        demandRegisterRecordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
        demandRegisterRecordSpec.addFieldSpec("Value", NUMBER);
        demandRegisterRecordSpec.addFieldSpec("From Time", DATE);
        demandRegisterRecordSpec.addFieldSpec("Event Time", DATE);
        demandRegisterRecordSpec.persist();
    }

    private void createServiceCategories() {
        ServiceCategoryImpl.from(dataModel, ServiceKind.ELECTRICITY).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.GAS).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.WATER).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.TIME).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.HEAT).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.REFUSE).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.SEWERAGE).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.RATES).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.TVLICENSE).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.INTERNET).persist();
        ServiceCategoryImpl.from(dataModel, ServiceKind.OTHER).persist();
    }

    private void createReadingTypes() {
        for (ReadingTypeImpl readingType : ReadingTypeGenerator.generate(dataModel)) {
            readingType.persist();
        }
    }

    private void createPartyRoles(PartyService partyService) {
        for (MarketRoleKind role : MarketRoleKind.values()) {
            partyService.createRole(MessageService.COMPONENTNAME, role.name(), role.getDisplayName(), null, null);
        }
    }

    private void createPrivileges(UserService userService) {
        for (String each : getPrivileges()) {
            userService.createPrivilege(MessageService.COMPONENTNAME, each, "");
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
