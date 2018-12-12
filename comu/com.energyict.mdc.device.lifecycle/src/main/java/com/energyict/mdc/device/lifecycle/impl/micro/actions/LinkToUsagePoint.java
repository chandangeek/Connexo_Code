/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroActionTranslationKey;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will link the Device to the Usage Point.
 *
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#LINK_TO_USAGE_POINT}
 */
public class LinkToUsagePoint extends TranslatableServerMicroAction {

    private final MetrologyConfigurationService metrologyConfigurationService;

    public LinkToUsagePoint(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService) {
        super(thesaurus);
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Collections.singletonList(usagePointPropertySpec(propertySpecService));
    }

    @Override
    protected final MicroAction getMicroAction() {
        return MicroAction.LINK_TO_USAGE_POINT;
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        Optional<UsagePoint> usagePoint = getUsagePointValue(properties);
        if (usagePoint.isPresent()) {
            try {
                device.activate(effectiveTimestamp, usagePoint.get(), this.metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT));
            } catch (LocalizedException e) {
                throw new DeviceLifeCycleActionViolationException() {
                    @Override
                    public String getLocalizedMessage() {
                        return e.getLocalizedMessage();
                    }
                };
            }
        }
    }

    private PropertySpec usagePointPropertySpec(PropertySpecService service) {
        return service.referenceSpec(UsagePoint.class)//supported by com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType#USAGEPOINT
                .named(MicroActionTranslationKey.MICRO_ACTION_NAME_LINK_TO_USAGE_POINT)
                .describedAs(MicroActionTranslationKey.MICRO_ACTION_DESCRIPTION_LINK_TO_USAGE_POINT)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
    }

    private Optional<UsagePoint> getUsagePointValue(List<ExecutableActionProperty> properties) {
        return properties.stream()
                .filter(executableActionProperty -> executableActionProperty.getPropertySpec().getName()
                        .equals(MicroActionTranslationKey.MICRO_ACTION_NAME_LINK_TO_USAGE_POINT.getKey()))
                .findFirst()
                .map(ExecutableActionProperty::getValue)
                .map(UsagePoint.class::cast);
    }
}
