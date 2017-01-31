/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Locale;

/**
 * TestRule that ensures tests run within a given Locale. When returning from the test the current Locale will be restored.
 * Tests or code under test that change the default Locale will have their effect, but the current Locale will still be reset after the test.
 * When setting this rule without a given Locale it will use new Locale("mt", "MT"), as we regard this Locale to be sufficiently rare to not be
 * the default Locale of one of the developers.
 */
public class LocaleNeutral implements TestRule {

    static final LocaleNeutral DEFAULT_SUBSTITUTE = new LocaleNeutral(new Locale("mt", "MT"));
    private final Locale substitute;

    LocaleNeutral(Locale substitute) {
        this.substitute = substitute;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new LocaleStatement(base);
    }

    private final class LocaleStatement extends Statement {

        private final Statement decorated;

        private LocaleStatement(Statement decorated) {
            this.decorated = decorated;
        }

        @Override
        public void evaluate() throws Throwable {
            Locale toRestore = Locale.getDefault();
            try {
                Locale.setDefault(substitute);

                decorated.evaluate();

            } finally {
                Locale.setDefault(toRestore);
            }
        }
    }
}
