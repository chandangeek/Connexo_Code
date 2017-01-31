/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools;

import com.elster.jupiter.devtools.tests.rules.Using;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class LocaleNeutralTestWithGivenLocale {

    @Rule
    public TestRule rule = Using.locale("et", "EE");

    @Test
    public void testLanguage() {
        assertThat(Locale.getDefault().getLanguage()).isEqualTo("et");
    }

    @Test
    public void testCountry() {
        assertThat(Locale.getDefault().getCountry()).isEqualTo("EE");
    }

}
