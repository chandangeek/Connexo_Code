/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests.rules;

import org.joda.time.DateTimeZone;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * TestRule that ensures tests run within a given TimeZone. When returning from the test the current TimeZone will be restored.
 * Changes will be done both for java.util and joda.time.
 * Tests or code under test that change the default TimeZone will have their effect, but the current TimeZone will still be reset after the test.
 * When setting this rule without a given TimeZone it will use "Antarctica/McMurdo", as we regard this TimeZone to be sufficiently rare to not be
 * the default Locale of one of the developers. In addition, it is a TimeZone that uses DST, making it challenging enough to tests that test code affected by DST changes.
 * Offset for winter time is 13 hours, offset for summer time is 12 hours.
 */
public class TimeZoneNeutral implements TestRule {

    private static final String DEFAULT_SUBSTITUTE = "Antarctica/McMurdo";

    static final TimeZoneNeutral DEFAULT_TIMEZONE_NEUTRAL = new TimeZoneNeutral(DEFAULT_SUBSTITUTE);
    private final String substitute;

    TimeZoneNeutral(String substitute) {
        this.substitute = substitute;
    }

    TimeZoneNeutral(TimeZone substitute) {
        this.substitute = substitute.getID();
    }

    TimeZoneNeutral(DateTimeZone substitute) {
        this.substitute = substitute.getID();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new TimeZoneStatement(base, TimeZone.getTimeZone(substitute), DateTimeZone.forID(substitute));
    }

    private static final class TimeZoneStatement extends Statement {

        private final Statement decorated;
        private final TimeZone substitute;
        private final DateTimeZone jodaSubstitute;

        private TimeZoneStatement(Statement decorated, TimeZone substitute, DateTimeZone jodaSubstitute) {
            this.decorated = decorated;
            this.substitute = substitute;
            this.jodaSubstitute = jodaSubstitute;
        }

        @Override
        public void evaluate() throws Throwable {
            TimeZone toRestore = TimeZone.getDefault();
            DateTimeZone jodaToRestore = DateTimeZone.getDefault();
            try {
                TimeZone.setDefault(substitute);
                DateTimeZone.setDefault(jodaSubstitute);

                decorated.evaluate();

            } finally {
                TimeZone.setDefault(toRestore);
                DateTimeZone.setDefault(jodaToRestore);
            }
        }
    }

    public static ZoneId getMcMurdo() {
        return ZoneId.of(DEFAULT_SUBSTITUTE);
    }

    public String getSubstitute() {
        return substitute;
    }
}
