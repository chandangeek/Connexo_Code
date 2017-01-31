/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Tests the {@link BaudrateValue} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (10:29)
 */
public class BaudrateValueTest {

    @Test
    public void testGetTypedValues () {
        assertThat(BaudrateValue.getTypedValues().length).isEqualTo(BaudrateValue.values().length);
    }

    @Test
    public void testValueForBigDecimalValue () {
        for (BaudrateValue baudrateValue : BaudrateValue.values()) {
            BigDecimal baudrate = baudrateValue.value();
            assertThat(BaudrateValue.valueFor(baudrate)).
                    as("BaudrateValue::valueFor(BigDecimal) fails for " + baudrate + " returned by " + baudrateValue).
                    isEqualTo(baudrateValue);
        }
    }

    @Test
    public void testGetSioBaudrateForSupportedValues () {
        Set<BaudrateValue> sioSupported =
            EnumSet.of(
                BaudrateValue.BAUDRATE_150,
                BaudrateValue.BAUDRATE_300,
                BaudrateValue.BAUDRATE_600,
                BaudrateValue.BAUDRATE_1200,
                BaudrateValue.BAUDRATE_2400,
                BaudrateValue.BAUDRATE_4800,
                BaudrateValue.BAUDRATE_9600,
                BaudrateValue.BAUDRATE_19200,
                BaudrateValue.BAUDRATE_38400,
                BaudrateValue.BAUDRATE_57600,
                BaudrateValue.BAUDRATE_115200,
                BaudrateValue.BAUDRATE_230400,
                BaudrateValue.BAUDRATE_460800);
        for (BaudrateValue baudrateValue : sioSupported) {
            // Assert that no exception is thrown
            baudrateValue.sioBaudRate();
        }
    }

    @Test
    public void testGetSioBaudrateForUnsupportedValues () {
        Set<BaudrateValue> sioUnsupported =
            EnumSet.of(
                BaudrateValue.BAUDRATE_1800,
                BaudrateValue.BAUDRATE_7200,
                BaudrateValue.BAUDRATE_14400,
                BaudrateValue.BAUDRATE_28800,
                BaudrateValue.BAUDRATE_56000,
                BaudrateValue.BAUDRATE_76800);
        // Assert that every call to sioBaudrate throws an exception
        for (BaudrateValue baudrateValue : sioUnsupported) {
            try {
                baudrateValue.sioBaudRate();
                fail("Expected an SerialPortException to be thrown because " + baudrateValue + " is not supported by the Sio library");
            }
            catch (Exception e) {
                // Expected
            }
        }
    }

}