/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeterActivationChannelsContainer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UpgraderV10_7 implements Upgrader {
    private final DataModel dataModel;
    private final MessageService messageService;
    private final InstallerImpl installer;

    @Inject
    UpgraderV10_7(DataModel dataModel, MessageService messageService, InstallerImpl installer) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.installer = installer;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        deleteOldDestinations();
        installer.createMessageHandlers();
        deleteInapplicableRuleSets();
    }

    private void deleteOldDestinations() {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ValidationServiceImpl.DESTINATION_NAME);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(ValidationServiceImpl.DESTINATION_NAME);
            destination.delete();
        });
    }

    private void deleteInapplicableRuleSets() {
        Set<ChannelValidation> channelValidationList = new HashSet<>();
        Set<ChannelsContainerValidation> channelsContainerValidations = new HashSet<>();
        dataModel.stream(ChannelsContainerValidation.class).filter(Where.where("ruleSet").isNotNull())
                .filter(channelsContainerValidation -> channelsContainerValidation.getChannelsContainer() instanceof MeterActivationChannelsContainer
                        && channelsContainerValidation.getRuleSet().getQualityCodeSystem() != QualityCodeSystem.MDC)
                .forEach(channelsContainerValidation -> {
                    channelsContainerValidations.add(channelsContainerValidation);
                    channelValidationList.addAll(channelsContainerValidation.getChannelValidations());
                });
        dataModel.mapper(ChannelsContainerValidation.class).remove(new ArrayList<>(channelsContainerValidations));
        dataModel.mapper(ChannelValidation.class).remove(new ArrayList<>(channelValidationList));
    }
}
