/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NlsKeyImplTest extends EqualsContractTest {

    private static final String KEY = "key";
    private static final String COMPONENT_NAME = "CMP";
    private NlsKeyImpl instanceA;

    @Mock
    private DataModel dataModel;

    @Override
    protected NlsKeyImpl getInstanceA() {
        if (instanceA == null) {
            instanceA = new NlsKeyImpl(dataModel).init(COMPONENT_NAME, Layer.REST, KEY);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new NlsKeyImpl(dataModel).init(COMPONENT_NAME, Layer.REST, KEY);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new NlsKeyImpl(dataModel).init("OTHER", Layer.REST, KEY),
                new NlsKeyImpl(dataModel).init(COMPONENT_NAME, Layer.DOMAIN, KEY),
                new NlsKeyImpl(dataModel).init(COMPONENT_NAME, Layer.REST, "OTHER")
        );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void testTranslateSpecificNotAvailable() {
        NlsKeyImpl nlsKey = getInstanceA();
        nlsKey.add(Locale.CANADA_FRENCH, "CANADA_FRENCH");
        nlsKey.add(Locale.FRENCH, "FRENCH");

        assertThat(nlsKey.translate(new Locale("fr", "BE")).get()).isEqualTo("FRENCH");
    }

    @Test
    public void testTranslateSpecificAvailable() {
        NlsKeyImpl nlsKey = getInstanceA();
        nlsKey.add(Locale.CANADA_FRENCH, "CANADA_FRENCH");
        nlsKey.add(Locale.FRENCH, "FRENCH");

        assertThat(nlsKey.translate(Locale.CANADA_FRENCH).get()).isEqualTo("CANADA_FRENCH");
    }

    @Test
    public void testTranslateNonSpecific() {
        NlsKeyImpl nlsKey = getInstanceA();
        nlsKey.add(Locale.CANADA_FRENCH, "CANADA_FRENCH");
        nlsKey.add(Locale.FRENCH, "FRENCH");

        assertThat(nlsKey.translate(Locale.FRENCH).get()).isEqualTo("FRENCH");
    }

}
