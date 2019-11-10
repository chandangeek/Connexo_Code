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

public class MaxDemandDomainExtensionTest {

    private BigDecimal connectedLoad = new BigDecimal("100");
    private Boolean flag = true;
    private Unit unit = Unit.kW;

    private CustomPropertySetValues cpsValues;
    private MaxDemandDomainExtension domainExtension;

    @Mock
    private Device device;

    @Before
    public void setup() {
        cpsValues = CustomPropertySetValues.empty();
        domainExtension = new MaxDemandDomainExtension();
    }

    @Test
    public void testCopyFrom() {
        cpsValues.setProperty(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName(), connectedLoad);
        cpsValues.setProperty(MaxDemandDomainExtension.FieldNames.UNIT.javaName(), unit);
        cpsValues.setProperty(MaxDemandDomainExtension.FieldNames.FLAG.javaName(), flag);

        domainExtension.copyFrom(device, cpsValues);

        assertThat(domainExtension.getConnectedLoad()).isEqualTo(connectedLoad);
        assertThat(domainExtension.getUnit()).isEqualTo(unit);
        assertThat(domainExtension.isFlag()).isSameAs(flag);
    }

    @Test
    public void testCopyTo() {
        domainExtension.setConnectedLoad(connectedLoad);
        domainExtension.setUnit(unit);
        domainExtension.setFlag(flag);

        domainExtension.copyTo(cpsValues);

        assertThat(cpsValues.getProperty(MaxDemandDomainExtension.FieldNames.UNIT.javaName()))
                .isSameAs(unit);
        assertThat(cpsValues.getProperty(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName()))
                .isSameAs(connectedLoad);
        assertThat(cpsValues.getProperty(MaxDemandDomainExtension.FieldNames.FLAG.javaName()))
                .isSameAs(flag);
    }

    @Test
    public void testValidateDelete() {
        domainExtension.validateDelete();
    }
}