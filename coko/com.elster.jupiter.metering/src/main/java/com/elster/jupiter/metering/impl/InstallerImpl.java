package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
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
import java.util.Locale;

public class InstallerImpl {

    private static final int SLOT_COUNT = 8;
    private static final int MONTHS_PER_YEAR = 12;
    private static final String IMPORT_FILE_NAME = "enddeviceeventtypes.csv";

    private final MeteringServiceImpl meteringService;
    private final IdsService idsService;
    private final PartyService partyService;
    private final UserService userService;
    private final EventService eventService;
    private final Thesaurus thesaurus;

    public InstallerImpl(MeteringServiceImpl meteringService, IdsService idsService, PartyService partyService, UserService userService, EventService eventService, Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.idsService = idsService;
        this.partyService = partyService;
        this.userService = userService;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    public void install() {
    	createVaults();
        createRecordSpecs();
        createServiceCategories();
        createReadingTypes();
        createPartyRoles();
        createPrivileges();
        createAmrSystems();
        createEndDeviceEventTypes();
        createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception e) {
                System.out.println("Could not create event type : " + eventType.name());
            }
        }
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
                                meteringService.createEndDeviceEventType(code);
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
    	meteringService.createAmrSystem(1, "MDC");
    	meteringService.createAmrSystem(2, "EnergyAxis");
    }

    private void createVaults() {
        Vault intervalVault = idsService.newVault(MeteringService.COMPONENTNAME, 1, "Interval Data Store", SLOT_COUNT, true);
        intervalVault.persist();
        createPartitions(intervalVault);
        Vault registerVault = idsService.newVault(MeteringService.COMPONENTNAME, 2, "Register Data Store", SLOT_COUNT, false);
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

    private void createRecordSpecs() {
    	RecordSpecs.createAll(idsService);
    }

    private void createServiceCategories() {
    	for (ServiceKind kind : ServiceKind.values()) {
    		meteringService.createServiceCategory(kind);
    	}
    }

    private void createReadingTypes() {
        ReadingTypeGenerator.generate(meteringService);
    }

    private void createPartyRoles() {
        for (MarketRoleKind role : MarketRoleKind.values()) {
            partyService.createRole(MeteringService.COMPONENTNAME, role.name(), role.getDisplayName(), null, null);
        }
    }

    private void createPrivileges() {
        for (String each : getPrivileges()) {
            userService.createPrivilege(MeteringService.COMPONENTNAME, each, "");
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

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(MeteringService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }

    private Translation toTranslation(final SimpleNlsKey nlsKey, final Locale locale, final String translation) {
        return new Translation() {
            @Override
            public NlsKey getNlsKey() {
                return nlsKey;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getTranslation() {
                return translation;
            }
        };
    }

}
