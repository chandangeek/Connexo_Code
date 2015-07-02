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
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.KnownAmrSystem;
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
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.BufferedReaderIterable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerImpl {

    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final int SLOT_COUNT = 8;
    private static final String IMPORT_FILE_NAME = "enddeviceeventtypes.csv";
    private static final String NOT_APPLICABLE = "n/a";

    private final MeteringServiceImpl meteringService;
    private final IdsService idsService;
    private final PartyService partyService;
    private final UserService userService;
    private final EventService eventService;
    private final Thesaurus thesaurus;
    private final MessageService messageService;
    private final boolean createAllReadingTypes;
    private final String[] requiredReadingTypes;

    public InstallerImpl(MeteringServiceImpl meteringService, IdsService idsService, PartyService partyService, UserService userService, EventService eventService, Thesaurus thesaurus, MessageService messageService, boolean createAllReadingTypes, String[] requiredReadingTypes) {
        this.meteringService = meteringService;
        this.idsService = idsService;
        this.partyService = partyService;
        this.userService = userService;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.messageService = messageService;
        this.createAllReadingTypes = createAllReadingTypes;
        this.requiredReadingTypes = requiredReadingTypes;
    }

    public void install() {
        createVaults();
        createRecordSpecs();
        List<ServiceCategoryImpl> serviceCategories = createServiceCategories();
        createReadingTypes();
        createPartyRoles();
        createAmrSystems();
        createEndDeviceEventTypes();
        createEventTypes();
        createTranslations(serviceCategories);
        createQueueTranslations();
        createQueues();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not create event type : " + eventType.name(), e);
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
                                try {
                                    if (meteringService.getEndDeviceEventType(code).isPresent()) {
                                        LOGGER.finer("Skipping code "+code+": already exists");
                                    } else {
                                        LOGGER.finer("adding code "+code);
                                        meteringService.createEndDeviceEventType(code);
                                    }
                                } catch (Exception e) {
                                    LOGGER.log(Level.SEVERE, "Error creating EndDeviceType \'" + code + "\' : " + e.getMessage(), e);
                                }
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
        return "*".equals(field) ? Arrays.asList(EndDeviceEventorAction.values()) : NOT_APPLICABLE.equalsIgnoreCase(field) ? Arrays.asList(EndDeviceEventorAction.NA) : Arrays.asList(EndDeviceEventorAction.valueOf(sanitized(field)));
    }

    private String sanitized(String field) {
        return field.toUpperCase().replaceAll("[\\-%]", "");
    }

    private Iterable<EndDeviceSubDomain> subDomains(String field) {
        Iterable<EndDeviceSubDomain> result = NOT_APPLICABLE.equalsIgnoreCase(field) ? Arrays.asList(EndDeviceSubDomain.NA) : Arrays.asList(EndDeviceSubDomain.valueOf(sanitized(field)));
        return "*".equals(field) ? Arrays.asList(EndDeviceSubDomain.values()) : result;
    }

    private Iterable<EndDeviceDomain> domains(String field) {
        return "*".equals(field) ? Arrays.asList(EndDeviceDomain.values()) : NOT_APPLICABLE.equalsIgnoreCase(field) ? Arrays.asList(EndDeviceDomain.NA) : Arrays.asList(EndDeviceDomain.valueOf(sanitized(field)));
    }

    private Iterable<EndDeviceType> endDeviceTypes(String field) {
        return "*".equals(field) ? Arrays.asList(EndDeviceType.values()) : NOT_APPLICABLE.equalsIgnoreCase(field) ? Arrays.asList(EndDeviceType.NA) : Arrays.asList(EndDeviceType.valueOf(sanitized(field)));
    }

    private void createAmrSystems() {
        try {
            for (KnownAmrSystem amrSystem : KnownAmrSystem.values()) {
                meteringService.createAmrSystem(amrSystem.getId(), amrSystem.getName());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating AMR System : " + e.getMessage(), e);
        }
    }

    private void createVaults() {
        try {
            Vault intervalVault = idsService.newVault(MeteringService.COMPONENTNAME, 1, "Interval Data Store", SLOT_COUNT, 0, true);
            intervalVault.persist();
            createPartitions(intervalVault);
            Vault registerVault = idsService.newVault(MeteringService.COMPONENTNAME, 2, "Register Data Store", SLOT_COUNT, 1, false);
            registerVault.persist();
            createPartitions(registerVault);
            Vault dailyVault = idsService.newVault(MeteringService.COMPONENTNAME, 3, "Daily and Monthly Data Store", SLOT_COUNT, 0, true);
            dailyVault.persist();
            createPartitions(dailyVault);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating vaults : " + e.getMessage(), e);
        }
    }

    private void createPartitions(Vault vault) {
    	Instant start = YearMonth.now().atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    	vault.activate(start);
    	vault.extendTo(start.plus(360, ChronoUnit.DAYS), Logger.getLogger(getClass().getPackage().getName()));
    }

    private void createRecordSpecs() {
        try {
            RecordSpecs.createAll(idsService);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating recordspecs : " + e.getMessage(), e);
        }
    }

    private List<ServiceCategoryImpl> createServiceCategories() {
        List<ServiceCategoryImpl> list = new ArrayList<>();
        ServiceCategoryImpl serviceCategory = null;
        for (ServiceKind kind : ServiceKind.values()) {
            try {
                serviceCategory = meteringService.createServiceCategory(kind);
                list.add(serviceCategory);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error creating serviceCategory \'" + kind.name() + "\' : " + e.getMessage(), e);
            }
        }
        return list;
    }

    private void createReadingTypes() {
        try {
            if(createAllReadingTypes){
                List<Pair<String, String>> readingTypes = ReadingTypeGenerator.generate();
                this.meteringService.createAllReadingTypes(readingTypes);
            } else if(requiredReadingTypes.length > 0){
                ReadingTypeGenerator.generateSelectedReadingTypes(meteringService, requiredReadingTypes);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating readingtypes : " + e.getMessage(), e);
        }
    }

    private void createPartyRoles() {
        for (MarketRoleKind role : MarketRoleKind.values()) {
            try {
                partyService.createRole(MeteringService.COMPONENTNAME, role.name(), role.getDisplayName(), null, null);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error creating PartyRole : \'" + role.name() + "\': " + e.getMessage(), e);
            }
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

    private void createTranslations(List<ServiceCategoryImpl> serviceCategories) {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(MeteringService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        for (ServiceCategoryImpl serviceCategory : serviceCategories) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(MeteringService.COMPONENTNAME, Layer.DOMAIN, serviceCategory.getTranslationKey()).defaultMessage(serviceCategory.getKind().getDisplayName());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, serviceCategory.getKind().getDisplayName()));
        }
        thesaurus.addTranslations(translations);
    }

    private void createQueueTranslations() {
        this.thesaurus.addTranslations(
                Arrays.asList(
                        this.toTranslation(
                                SimpleNlsKey.key(
                                        MeteringService.COMPONENTNAME,
                                        Layer.DOMAIN,
                                        SwitchStateMachineEvent.SUBSCRIBER).defaultMessage(SwitchStateMachineEvent.SUBSCRIBER_TRANSLATION),
                                Locale.ENGLISH, SwitchStateMachineEvent.SUBSCRIBER_TRANSLATION)));
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

    private void createQueues() {
        this.createQueue(SwitchStateMachineEvent.DESTINATION, SwitchStateMachineEvent.SUBSCRIBER);
    }

    private void createQueue(String queueDestination, String queueSubscriber) {
        try {
            QueueTableSpec defaultQueueTableSpec = this.messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(queueDestination, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
            destinationSpec.subscribe(queueSubscriber);
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

}
