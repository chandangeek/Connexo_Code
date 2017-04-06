/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MainCheckValidatorMiscTest extends MainCheckValidatorTest {

    private static ByteArrayOutputStream outContent;
    private static ByteArrayOutputStream errContent;

    private static PrintStream out;
    private static PrintStream err;

    private static String preparedOut;
    private static String preparedErr;

    @BeforeClass
    public static void configureConsole() {

        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();

        out = System.out;
        err = System.err;

        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterClass
    public static void configureConsoleBack() {
        System.setOut(out);
        System.setErr(err);

        System.out.println(preparedOut);
        System.err.println(preparedErr);
    }

    @Test
    public void testNoPuprose() {
        validateWithReadings(new MainCheckValidatorTest.MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withNotExistingCheckPurpose("Fake purpose")
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Fri, 01 Jan 2016 12:00 until Fri, 02 Jan 2016 12:00\" using method \"Main/check comparison\" on [Daily] Secondary Delta A+ (kWh) since the specified purpose doesnt exist on the Usage point name", false);


    }

    @Test
    public void testNoChannel() {
        validateWithReadings(new MainCheckValidatorTest.MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withNotExistingCheckChannel()
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Fri, 01 Jan 2016 12:00 until Fri, 02 Jan 2016 12:00\" using method \"Main/check comparison\" on [Daily] Secondary Delta A+ (kWh) since check output with matching reading type on the specified purpose doesnt exist on Usage point name", false);
    }

    @Test
    public void testChannelWithMissingDataPass() {
        validateWithReadings(new MainCheckValidatorTest.MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Fri, 01 Jan 2016 12:00 until Fri, 02 Jan 2016 12:00\" using method \"Main/check comparison\" on Usage point name/[Daily] Secondary Delta A+ (kWh) since data from check output is missing or not validated", true);
    }

    @Test
    public void testChannelWithMissingDataNotPass() {
        validateWithReadings(new MainCheckValidatorTest.MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(true)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Fri, 01 Jan 2016 12:00 until Fri, 02 Jan 2016 12:00\" using method \"Main/check comparison\" on Usage point name/[Daily] Secondary Delta A+ (kWh) since data from check output is missing or not validated", true);
    }

    private void validateWithReadings(MainCheckValidatorRule rule, String warning, boolean missingData) {
        ChannelReadings mainChannelReadings = new ChannelReadings(3);
        mainChannelReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"));
        mainChannelReadings.setReadingValue(1, bigDecimal(20D), instant("20160102000000"));
        mainChannelReadings.setReadingValue(2, bigDecimal(30D), instant("20160103000000"));

        // NOTE: check channel readings are not validated!
        ValidatedChannelReadings checkReadings = new ValidatedChannelReadings(3);
        checkReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"));
        if (!missingData) {
            checkReadings.setReadingValue(1, bigDecimal(20D), instant("20160102000000"));
        }
        checkReadings.setReadingValue(2, bigDecimal(30D), instant("20160103000000"));

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, mainChannelReadings, checkReadings);
        MainCheckValidator validator = initValidator(validationConfiguration);

        assertEquals(3, validationConfiguration.mainChannelReadings.readings.size());

        long validReadingsCount = (rule.notExistingCheckPurpose!=null || rule.noCheckChannel)?0:(missingData?(rule.passIfNoData?3L:1L):3L);

        assertEquals(validReadingsCount, validationConfiguration.mainChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> c.equals(ValidationResult.VALID))).count());

        assertEquals(0, validator.finish().size());

        preparedOut = outContent.toString();
        preparedErr = errContent.toString();

        assertFalse(preparedOut.contains(warning));
        assertTrue(preparedErr.contains(warning));
    }
}
