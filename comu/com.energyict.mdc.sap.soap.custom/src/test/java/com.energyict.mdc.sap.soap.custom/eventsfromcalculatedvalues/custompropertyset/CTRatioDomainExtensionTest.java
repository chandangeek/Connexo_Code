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

public class CTRatioDomainExtensionTest {

    private BigDecimal ctRatio = new BigDecimal("100");
    private Boolean flag = true;

    private CustomPropertySetValues cpsValues;
    private CTRatioDomainExtension domainExtension;

    @Mock
    private Device device;

    @Before
    public void setup() {
        cpsValues = CustomPropertySetValues.empty();
        domainExtension = new CTRatioDomainExtension();
    }

    @Test
    public void testCopyFrom() {
        cpsValues.setProperty(CTRatioDomainExtension.FieldNames.CT_RATIO.javaName(), ctRatio);
        cpsValues.setProperty(CTRatioDomainExtension.FieldNames.FLAG.javaName(), flag);

        domainExtension.copyFrom(device, cpsValues);

        assertThat(domainExtension.getCtRatio()).isEqualTo(ctRatio);
        assertThat(domainExtension.isFlag()).isSameAs(flag);
    }

    @Test
    public void testCopyTo() {
        domainExtension.setCTRatio(ctRatio);
        domainExtension.setFlag(flag);

        domainExtension.copyTo(cpsValues);

        assertThat(cpsValues.getProperty(CTRatioDomainExtension.FieldNames.CT_RATIO.javaName()))
                .isSameAs(ctRatio);
        assertThat(cpsValues.getProperty(CTRatioDomainExtension.FieldNames.FLAG.javaName()))
                .isSameAs(flag);
    }

    @Test
    public void testValidateDelete() {
        domainExtension.validateDelete();
    }
}