/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.mockito.Mock;

import static org.fest.reflect.core.Reflection.field;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

public class MainCheckValidatorMiscTest extends MainCheckValidatorTest {

    @Mock
    private Logger logger;

    private StringBuffer logs;

    @Test
    public void testNoPuprose() {
        validateWithReadings(new MainCheckValidatorTest.MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withNotExistingCheckPurpose("Fake purpose")
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Fri, 01 Jan 2016 12:00 until Thu, 07 Jan 2016 12:00\" using method \"Main/check comparison\" on [Daily] Secondary Delta A+ (kWh) since the specified purpose doesnt exist on the Usage point name", false);


    }

    @Test
    public void testNoChannel() {
        validateWithReadings(new MainCheckValidatorTest.MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withNotExistingCheckChannel()
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Fri, 01 Jan 2016 12:00 until Thu, 07 Jan 2016 12:00\" using method \"Main/check comparison\" on [Daily] Secondary Delta A+ (kWh) since check output with matching reading type on the specified purpose doesnt exist on Usage point name", false);
    }

    @Test
    public void testChannelWithMissingDataPass() {
        validateWithReadings(new MainCheckValidatorTest.MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Fri, 01 Jan 2016 12:00 until Thu, 07 Jan 2016 12:00\" using method \"Main/check comparison\" on Usage point name/[Daily] Secondary Delta A+ (kWh) since data from check output is missing or not validated", true);
    }

    @Test
    public void testChannelWithMissingDataNotPass() {
        validateWithReadings(new MainCheckValidatorTest.MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(true)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Fri, 01 Jan 2016 12:00 until Thu, 07 Jan 2016 12:00\" using method \"Main/check comparison\" on Usage point name/[Daily] Secondary Delta A+ (kWh) since data from check output is missing or not validated", true);
    }

    private void mockLogger(MainCheckValidator validator){

        logs = new StringBuffer();
        doAnswer(invocationOnMock -> {
            Level level = (Level)(invocationOnMock.getArguments()[0]);
            logs.append(level).append(":").append(" ");
            logs.append((String)(invocationOnMock.getArguments()[1]));
            return null;
        }).when(logger).log(any(Level.class),anyString(),any(Throwable.class));

        field("logger").ofType(Logger.class).in(validator).set(logger);
    }



    @Override
    MainCheckValidator initValidator(ValidationConfiguration validationConfiguration) {
        MainCheckValidator validator = new MainCheckValidator(thesaurus, propertySpecService, validationConfiguration.rule
                .createProperties(), validationConfiguration.metrologyConfigurationService, validationConfiguration.validationService);
        mockLogger(validator);
        validator.init(validationConfiguration.checkChannel, validationConfiguration.readingType, range);
        return validator;
    }

    private void validateWithReadings(MainCheckValidatorRule rule, String warning, boolean missingData) {
        ChannelReadings mainChannelReadings = new ChannelReadings(3);
        mainChannelReadings.setReadingValue(0, bigDecimal(10D), instant("20160201000000"));
        mainChannelReadings.setReadingValue(1, bigDecimal(20D), instant("20160202000000"));
        mainChannelReadings.setReadingValue(2, bigDecimal(30D), instant("20160203000000"));

        // NOTE: check channel readings are not validated!
        ValidatedChannelReadings checkReadings = new ValidatedChannelReadings(3);
        checkReadings.setReadingValue(0, bigDecimal(10D), instant("20160201000000"));
        if (!missingData) {
            checkReadings.setReadingValue(1, bigDecimal(20D), instant("20160202000000"));
        }
        checkReadings.setReadingValue(2, bigDecimal(30D), instant("20160203000000"));

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, mainChannelReadings, checkReadings);
        MainCheckValidator validator = initValidator(validationConfiguration);

        assertEquals(3, validationConfiguration.mainChannelReadings.readings.size());

        long validReadingsCount = (rule.notExistingCheckPurpose!=null || rule.noCheckChannel)?0:(missingData?(rule.passIfNoData?3L:1L):3L);

        assertEquals(validReadingsCount, validationConfiguration.mainChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> c.equals(ValidationResult.VALID))).count());

        assertEquals(0, validator.finish().size());

        assertTrue(logs.toString().contains(warning));
    }
}
