package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.EndDeviceControlTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceEventTypeCodeBuilder;
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
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.BufferedReaderIterable;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstallerImpl implements FullInstaller {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final int SLOT_COUNT = 8;
    private static final String IMPORT_FILE_NAME = "enddeviceeventtypes.csv";
    //TODO update the enddevicecontroltypes csv file
    private static final String IMPORT_CONTROL_TYPES = "enddevicecontroltypes.csv";
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
    private final Clock clock;
    private final DataModel dataModel;
    private final MetrologyConfigurationServiceImpl metrologyConfigurationService;

    @Inject
    public InstallerImpl(DataModel dataModel, MeteringServiceImpl meteringService, IdsService idsService, PartyService partyService, UserService userService, EventService eventService, Thesaurus thesaurus, MessageService messageService, Clock clock, MetrologyConfigurationServiceImpl metrologyConfigurationService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.idsService = idsService;
        this.partyService = partyService;
        this.userService = userService;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.messageService = messageService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.createAllReadingTypes = meteringService.isCreateAllReadingTypes();
        this.requiredReadingTypes = meteringService.getRequiredReadingTypes();
        this.clock = clock;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

        doTry(
                "Create Vaults for MTR",
                this::createVaults,
                logger
        );
        doTry(
                "Create Record Specs for MTR",
                this::createRecordSpecs,
                logger
        );
        doTry(
                "Create default Service Categories",
                this::createServiceCategories,
                logger
        );
        doTry(
                "Create default Multiplier Types",
                this::createMultiplierTypes,
                logger
        );
        doTry(
                "Create Reading Types",
                this::createReadingTypes,
                logger
        );
        doTry(
                "Create Party Roles",
                this::createPartyRoles,
                logger
        );
        doTry(
                "Create AMR Systems",
                this::createAmrSystems,
                logger
        );
        doTry(
                "Create default End Device Event Types",
                () -> createEndDeviceEventTypes(logger),
                logger
        );
        doTry(
                "Create event types for MTR",
                this::createEventTypes,
                logger
        );
        doTry(
                "Create End Device control types",
                () -> createEndDeviceControlTypes(logger),
                logger
        );
        doTry(
                "Create Queues for MTR",
                this::createQueues,
                logger
        );
        doTry(
                "Create SQL Aggregation Components",
                this::createSqlAggregationComponents,
                logger
        );
        metrologyConfigurationService.install(logger);

        doTry(
                "Create Location Member table",
                () -> new CreateLocationMemberTableOperation(dataModel, meteringService.getLocationTemplate()).execute(),
                logger
        );

        doTry(
                "Create GeoCoordinates Spatial Meta Data table",
                () -> new GeoCoordinatesSpatialMetaDataTableOperation(dataModel).execute(),
                logger
        );
        doTry(
                "Create location template",
                meteringService::createLocationTemplate,
                logger
        );
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

    private void createEndDeviceEventTypes(Logger logger) {
        try (InputStream resourceAsStream = getClass().getClassLoader()
                .getResourceAsStream(getClass().getPackage().getName().replace('.', '/') + '/' + IMPORT_FILE_NAME)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            for (String line : new BufferedReaderIterable(reader)) {
                String[] fields = line.split(",");

                for (EndDeviceType deviceType : endDeviceTypes(fields[0])) {
                    for (EndDeviceDomain domain : domains(fields[1])) {
                        for (EndDeviceSubDomain subDomain : subDomains(fields[2])) {
                            for (EndDeviceEventOrAction eventOrAction : eventOrActions(fields[3])) {
                                String code = EndDeviceEventTypeCodeBuilder
                                        .type(deviceType)
                                        .domain(domain)
                                        .subDomain(subDomain)
                                        .eventOrAction(eventOrAction)
                                        .toCode();
                                if (meteringService.getEndDeviceEventType(code).isPresent()) {
                                    logger.finer("Skipping code " + code + ": already exists");
                                } else {
                                    logger.finer("adding code " + code);
                                    meteringService.createEndDeviceEventType(code);
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

    private Iterable<EndDeviceEventOrAction> eventOrActions(String field) {
        return "*".equals(field) ? Arrays.asList(EndDeviceEventOrAction.values()) : NOT_APPLICABLE.equalsIgnoreCase(field) ? Arrays
                .asList(EndDeviceEventOrAction.NA) : Arrays.asList(EndDeviceEventOrAction
                .valueOf(sanitized(field)));
    }

    private String sanitized(String field) {
        return field.toUpperCase().replaceAll("[\\-%]", "");
    }

    private Iterable<EndDeviceSubDomain> subDomains(String field) {
        Iterable<EndDeviceSubDomain> result = NOT_APPLICABLE.equalsIgnoreCase(field) ? Arrays.asList(EndDeviceSubDomain.NA) : Arrays
                .asList(EndDeviceSubDomain.valueOf(sanitized(field)));
        return "*".equals(field) ? Arrays.asList(EndDeviceSubDomain.values()) : result;
    }

    private Iterable<EndDeviceDomain> domains(String field) {
        return "*".equals(field) ? Arrays.asList(EndDeviceDomain.values()) : NOT_APPLICABLE.equalsIgnoreCase(field) ? Arrays
                .asList(EndDeviceDomain.NA) : Arrays.asList(EndDeviceDomain.valueOf(sanitized(field)));
    }

    private Iterable<EndDeviceType> endDeviceTypes(String field) {
        return "*".equals(field) ? Arrays.asList(EndDeviceType.values()) : NOT_APPLICABLE.equalsIgnoreCase(field) ? Arrays
                .asList(EndDeviceType.NA) : Arrays.asList(EndDeviceType.valueOf(sanitized(field)));
    }

    private void createAmrSystems() {
        for (KnownAmrSystem amrSystem : KnownAmrSystem.values()) {
            meteringService.createAmrSystem(amrSystem.getId(), amrSystem.getName());
        }
    }

    private void createVaults() {
        Vault intervalVault = idsService.createVault(MeteringService.COMPONENTNAME, 1, "Interval Data Store", SLOT_COUNT, 0, true);
        createPartitions(intervalVault);
        Vault registerVault = idsService.createVault(MeteringService.COMPONENTNAME, 2, "Register Data Store", SLOT_COUNT, 1, false);
        createPartitions(registerVault);
        Vault dailyVault = idsService.createVault(MeteringService.COMPONENTNAME, 3, "Daily and Monthly Data Store", SLOT_COUNT, 0, true);
        createPartitions(dailyVault);
    }

    private void createPartitions(Vault vault) {
        Instant start = YearMonth.now(clock).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        vault.activate(start);
        vault.extendTo(start.plus(360, ChronoUnit.DAYS), Logger.getLogger(getClass().getPackage().getName()));
    }

    private void createRecordSpecs() {
        RecordSpecs.createAll(idsService);
    }

    private List<ServiceCategoryImpl> createServiceCategories() {
        List<ServiceCategoryImpl> list = new ArrayList<>();
        ServiceCategoryImpl serviceCategory = null;
        for (ServiceKind kind : ServiceKind.values()) {
            switch (kind) {
                case ELECTRICITY:
                case GAS:
                case WATER:
                case HEAT:
                    serviceCategory = meteringService.createServiceCategory(kind, true);
                    break;
                default:
                    serviceCategory = meteringService.createServiceCategory(kind, false);
                    break;
            }
            list.add(serviceCategory);
        }
        return list;
    }

    private List<MultiplierType> createMultiplierTypes() {
        return Stream
                .of(MultiplierType.StandardType.values())
                .map(meteringService::createMultiplierType)
                .collect(Collectors.toList());
    }

    private void createReadingTypes() {
        if (createAllReadingTypes) {
            List<Pair<String, String>> readingTypes = ReadingTypeGenerator.generate();
            this.meteringService.createAllReadingTypes(readingTypes);
        } else if (requiredReadingTypes.length > 0) {
            ReadingTypeGenerator.generateSelectedReadingTypes(meteringService, requiredReadingTypes);
        }
    }

    private void createPartyRoles() {
        for (MarketRoleKind role : MarketRoleKind.values()) {
            partyService.createRole(MeteringService.COMPONENTNAME, role.name(), role.getDisplayName(), null, null);
        }
    }

    private void createQueues() {
        this.createQueue(SwitchStateMachineEvent.DESTINATION, SwitchStateMachineEvent.SUBSCRIBER);
    }

    private void createQueue(String queueDestination, String queueSubscriber) {
        QueueTableSpec defaultQueueTableSpec = this.messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(queueDestination, DEFAULT_RETRY_DELAY_IN_SECONDS);
        destinationSpec.activate();
        destinationSpec.subscribe(queueSubscriber);
    }

    private void createSqlAggregationComponents() {
        if (!SqlDialect.H2.equals(this.meteringService.getDataModel().getSqlDialect())) {
            SqlExecuteFromResourceFile
                    .executeAll(
                            this.meteringService.getDataModel(),
                            "type.Flags_Array.sql",
                            "function.aggFlags.sql");
        }
    }

    private void createEndDeviceControlTypes(Logger logger) {
        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(getClass().getPackage().getName().replace('.', '/') + '/' + IMPORT_CONTROL_TYPES)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            for (String line : new BufferedReaderIterable(reader)) {
                String[] fields = line.split(",");

                for (EndDeviceType deviceType : endDeviceTypes(fields[0])) {
                    for (EndDeviceDomain domain : domains(fields[1])) {
                        for (EndDeviceSubDomain subDomain : subDomains(fields[2])) {
                            for (EndDeviceEventOrAction eventOrAction : eventOrActions(fields[3])) {
                                String code = EndDeviceControlTypeCodeBuilder
                                        .type(deviceType)
                                        .domain(domain)
                                        .subDomain(subDomain)
                                        .eventOrAction(eventOrAction)
                                        .toCode();
                                try {
                                    if (meteringService.getEndDeviceControlType(code).isPresent()) {
                                        logger.finer("Skipping code " + code + ": already exists");
                                    } else {
                                        logger.finer("adding code " + code);
                                        meteringService.createEndDeviceControlType(code);
                                    }
                                } catch (Exception e) {
                                    logger.log(Level.SEVERE, "Error creating EndDeviceType \'" + code + "\' : " + e.getMessage(), e);
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

}

