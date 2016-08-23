/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import javax.inject.Provider;
import javax.validation.ConstraintValidatorContext;
import java.time.Clock;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link MeterRolePartOfMetrologyConfigurationIfAnyValidator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-23 (08:37)
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterRolePartOfMetrologyConfigurationIfAnyValidatorTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private ConstraintValidatorContext validatorContext;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContext;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private Provider<ChannelBuilder> channelBuilder;
    @Mock
    private Meter meter;
    @Mock
    private MeterRole meterRole;

    @Mock
    private UsagePoint usagePoint;
    private Clock clock = Clock.systemDefaultZone();

    @Before
    public void initializeMocks() {
        when(this.messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(this.messageFormat);
        when(this.dataModel.getInstance(MeterActivationChannelsContainerImpl.class))
                .thenReturn(new MeterActivationChannelsContainerImpl(this.meteringService, this.eventService, this.channelBuilder));
        when(this.validatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(this.violationBuilder);
        when(this.violationBuilder.addPropertyNode(anyString())).thenReturn(this.nodeBuilderCustomizableContext);
        when(this.meter.getHeadEndInterface()).thenReturn(Optional.empty());
        when(this.meterRole.getDisplayName()).thenReturn(MeterRolePartOfMetrologyConfigurationIfAnyValidatorTest.class.getSimpleName());
    }

    @Test
    public void meterActivationWithMeterButWithoutMeterRoleIsValid() {
        MeterActivationImpl meterActivation = new MeterActivationImpl(this.dataModel, this.eventService, this.clock, this.thesaurus);
        meterActivation.init(this.meter, this.clock.instant());

        // Business method
        boolean valid = this.getTestInstance().isValid(meterActivation, this.validatorContext);

        // Asserts
        assertThat(valid).isTrue();
    }

    @Test
    public void meterActivationWithoutUsagePointIsValid() {
        MeterActivationImpl meterActivation = new MeterActivationImpl(this.dataModel, this.eventService, this.clock, this.thesaurus);
        meterActivation.init(this.meter, this.clock.instant());

        // Business method
        boolean valid = this.getTestInstance().isValid(meterActivation, this.validatorContext);

        // Asserts
        assertThat(valid).isTrue();
    }

    @Test
    public void meterActivationWithoutMeterIsValid() {
        MeterActivationImpl meterActivation = new MeterActivationImpl(this.dataModel, this.eventService, this.clock, this.thesaurus);
        meterActivation.init(this.usagePoint, this.clock.instant());

        // Business method
        boolean valid = this.getTestInstance().isValid(meterActivation, this.validatorContext);

        // Asserts
        assertThat(valid).isTrue();
    }

    @Test
    public void meterActivationWithoutMetrologyConfigurationIsNotValid() {
        when(this.usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Collections.emptyList());
        when(this.usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenReturn(Collections.emptyList());
        MeterActivationImpl meterActivation = new MeterActivationImpl(this.dataModel, this.eventService, this.clock, this.thesaurus);
        meterActivation.init(this.meter, this.meterRole, this.usagePoint, Range.atLeast(this.clock.instant()));

        // Business method
        boolean valid = this.getTestInstance().isValid(meterActivation, this.validatorContext);

        // Asserts
        assertThat(valid).isFalse();
        this.verifyBuildingOfConstraintViolation();
    }

    private void verifyBuildingOfConstraintViolation() {
        verify(this.thesaurus).getFormat(MessageSeeds.METER_ROLE_NOT_IN_CONFIGURATION);
        verify(this.messageFormat).format(MeterRolePartOfMetrologyConfigurationIfAnyValidatorTest.class.getSimpleName());
        verify(this.validatorContext).buildConstraintViolationWithTemplate(anyString());
        verify(this.violationBuilder).addPropertyNode("meterRole");
        verify(this.nodeBuilderCustomizableContext).addConstraintViolation();
    }

    private MeterRolePartOfMetrologyConfigurationIfAnyValidator getTestInstance() {
        return new MeterRolePartOfMetrologyConfigurationIfAnyValidator(this.thesaurus);
    }

}