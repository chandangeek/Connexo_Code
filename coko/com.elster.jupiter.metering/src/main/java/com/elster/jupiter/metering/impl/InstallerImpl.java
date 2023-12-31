/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

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
import com.elster.jupiter.metering.impl.upgraders.GasDayOptionsCreator;
import com.elster.jupiter.metering.impl.upgraders.GasDayRelativePeriodCreator;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstallerImpl implements FullInstaller {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final int SLOT_COUNT = 8;


    private final ServerMeteringService meteringService;
    private final TimeService timeService;
    private final IdsService idsService;
    private final PartyService partyService;
    private final UserService userService;
    private final EventService eventService;
    private final MessageService messageService;
    private final boolean createAllReadingTypes;
    private final String[] requiredReadingTypes;
    private final Clock clock;
    private final DataModel dataModel;
    private final BundleContext bundleContext;
    private final InstallerV10_2Impl installerV10_2;
    private final InstallerV10_3Impl installerV10_3;
    private final PrivilegesProviderV10_3 privilegesProviderV10_3;
    private final DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller;

    @Inject
    public InstallerImpl(BundleContext bundleContext,
                         DataModel dataModel,
                         ServerMeteringService meteringService,
                         TimeService timeService,
                         IdsService idsService,
                         PartyService partyService,
                         UserService userService,
                         EventService eventService,
                         MessageService messageService,
                         Clock clock,
                         MeteringDataModelServiceImpl meteringDataModelService,
                         InstallerV10_2Impl installerV10_2,
                         InstallerV10_3Impl installerV10_3,
                         PrivilegesProviderV10_3 privilegesProviderV10_3,
                         DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller) {
        this.bundleContext = bundleContext;
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.timeService = timeService;
        this.idsService = idsService;
        this.partyService = partyService;
        this.userService = userService;
        this.eventService = eventService;
        this.messageService = messageService;
        this.installerV10_2 = installerV10_2;
        this.installerV10_3 = installerV10_3;
        this.privilegesProviderV10_3 = privilegesProviderV10_3;
        this.createAllReadingTypes = meteringDataModelService.isCreateAllReadingTypes();
        this.requiredReadingTypes = meteringDataModelService.getRequiredReadingTypes();
        this.clock = clock;
        this.defaultDeviceEventTypesInstaller = defaultDeviceEventTypesInstaller;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        execute(dataModel,
                "alter table MTR_READINGTYPE add constraint UK_MTR_READINGTYPE_ID unique (ID)",
                "alter table MTR_READINGQUALITY add constraint PK_MTR_READINGQUALITY primary key (CHANNELID, READINGTIMESTAMP, TYPE, READINGTYPEID)"
        );

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
                () -> defaultDeviceEventTypesInstaller.installIfNotPresent(logger),
                logger
        );
        doTry(
                "Create event types for MTR",
                this::createEventTypes,
                logger
        );

        doTry(
                "Create Queues for MTR",
                this::createQueues,
                logger
        );
        installerV10_2.install(dataModelUpgrader, logger);
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
        doTry(
                "Create Gas Day Options",
                () -> new GasDayOptionsCreator(this.meteringService).createIfMissing(this.bundleContext),
                logger
        );
        doTry(
                "Create Gas Day Relative periods",
                () -> GasDayRelativePeriodCreator.createAll(this.meteringService, this.timeService),
                logger
        );
        installerV10_3.install(dataModelUpgrader, logger);
        userService.addModulePrivileges(installerV10_2);
        userService.addModulePrivileges(privilegesProviderV10_3);
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
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
        ServiceCategoryImpl serviceCategory;
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
        this.createQueue(SwitchStateMachineEvent.DESTINATION, DefaultTranslationKey.SWITCH_STATE_MACHINE_SUBSCRIBER);
    }

    private void createQueue(String queueDestination, TranslationKey queueSubscriber) {
        QueueTableSpec defaultQueueTableSpec = this.messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(queueDestination, DEFAULT_RETRY_DELAY_IN_SECONDS);
        destinationSpec.activate();
        destinationSpec.subscribe(queueSubscriber, MeteringDataModelService.COMPONENT_NAME, Layer.DOMAIN);
    }

}
