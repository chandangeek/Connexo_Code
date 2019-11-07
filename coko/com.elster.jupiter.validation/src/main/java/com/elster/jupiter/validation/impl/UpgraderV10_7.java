/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeterActivationChannelsContainer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.validation.EventType;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UpgraderV10_7 implements Upgrader {
    private final DataModel dataModel;
    private final MessageService messageService;
    private final InstallerImpl installer;
    private final EventService eventService;

    @Inject
    UpgraderV10_7(DataModel dataModel, MessageService messageService, InstallerImpl installer, EventService eventService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.installer = installer;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        deleteOldDestinations();
        installer.createMessageHandlers();
        deleteInapplicableRuleSets();
        addNewEventsForMessageService();
    }

    private void deleteOldDestinations() {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ValidationServiceImpl.DESTINATION_NAME);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(ValidationServiceImpl.DESTINATION_NAME);
            destination.delete();
        });
    }

    private void deleteInapplicableRuleSets() {
        Set<ChannelsContainerValidation> channelsContainerValidations = new HashSet<>();
        dataModel.stream(ChannelsContainerValidation.class).filter(Where.where("ruleSet").isNotNull())
                .filter(channelsContainerValidation -> channelsContainerValidation.getChannelsContainer() instanceof MeterActivationChannelsContainer
                        && channelsContainerValidation.getRuleSet().getQualityCodeSystem() != QualityCodeSystem.MDC)
                .forEach(channelsContainerValidations::add);
        dataModel.mapper(ChannelsContainerValidation.class).remove(new ArrayList<>(channelsContainerValidations));
    }

    private void addNewEventsForMessageService() {
        EventType.SUSPECT_VALUE_CREATED.install(eventService);
    }
}
