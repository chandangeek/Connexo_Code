/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.energyict.mdc.common.device.data.Device;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class PowerFactorDomainExtensionTest {

    private BigDecimal setpointThreshold = BigDecimal.ONE;
    private BigDecimal hysteresisPercentage = new BigDecimal(0.5);
    private Boolean checkEnabled = true;

    private CustomPropertySetValues cpsValues;
    private PowerFactorDomainExtension domainExtension;

    @Mock
    private Device device;

    @Before
    public void setup() {
        cpsValues = CustomPropertySetValues.empty();
        domainExtension = new PowerFactorDomainExtension();
    }

    @Test
    public void testCopyFrom() {
        cpsValues.setProperty(PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName(), setpointThreshold);
        cpsValues.setProperty(PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName(), hysteresisPercentage);
        cpsValues.setProperty(PowerFactorDomainExtension.FieldNames.CHECK_ENABLED.javaName(), checkEnabled);

        domainExtension.copyFrom(device, cpsValues);

        assertThat(domainExtension.getSetpointThreshold()).isEqualTo(setpointThreshold);
        assertThat(domainExtension.getHysteresisPercentage()).isEqualTo(hysteresisPercentage);
        assertThat(domainExtension.isCheckEnabled()).isSameAs(checkEnabled);
    }

    @Test
    public void testCopyTo() {
        domainExtension.setSetpointThreshold(setpointThreshold);
        domainExtension.setHysteresisPercentage(hysteresisPercentage);
        domainExtension.setCheckEnabled(checkEnabled);

        domainExtension.copyTo(cpsValues);

        assertThat(cpsValues.getProperty(PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName()))
                .isSameAs(setpointThreshold);
        assertThat(cpsValues.getProperty(PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName()))
                .isSameAs(hysteresisPercentage);
        assertThat(cpsValues.getProperty(PowerFactorDomainExtension.FieldNames.CHECK_ENABLED.javaName()))
                .isSameAs(checkEnabled);
    }

    @Test
    public void testValidateDelete() {
        domainExtension.validateDelete();
    }
}