/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mappings;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.pluggable.adapters.upl.cps.DialectCustomPropertySetNameDetective;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;
import org.reflections.Reflections;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests that every {@link DeviceProtocolDialect} has a corresponding {@link AbstractDialectCustomPropertySet DialectCustomPropertySet} defined
 * and that the mapping between both is registered in dialect-custom-property-set-mapping.properties.
 *
 * @author Stijn Vanhoorelbeke
 * @since 25.09.17 - 11:16
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolDialectCustomPropertySetMappingTest {

    @Test
    public void testDeviceProtocolDialectCustomPropertySetExistence() throws Exception {
        testForPrefix("com.energyict");
        testForPrefix("com.elster");
//        testForPrefix("test.com.energyict");  // TODO: re-enable once the dialects of the SDK protocols have their proper DialectCustomPropertySet defined
    }

    private void testForPrefix(String prefix) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
        Reflections reflections = new Reflections(prefix);
        List<Class<? extends AbstractDeviceProtocolDialect>> dialectClasses = reflections.getSubTypesOf(AbstractDeviceProtocolDialect.class)
                .stream()
                .filter(aClass -> !aClass.getSimpleName().equals("NoParamsDeviceProtocolDialect"))
                .collect(Collectors.toList());

        List<Class<? extends AbstractDialectCustomPropertySet>> dialectCustomPropertySetClasses = reflections.getSubTypesOf(AbstractDialectCustomPropertySet.class)
                .stream()
                .filter(aClass -> !aClass.getName().contains("SDK"))
                .collect(Collectors.toList());

        for (Class<? extends AbstractDialectCustomPropertySet> customPropertySetClass : dialectCustomPropertySetClasses) {
            AbstractDialectCustomPropertySet customPropertySet = customPropertySetClass.getDeclaredConstructor(Thesaurus.class, PropertySpecService.class)
                    .newInstance(mock(Thesaurus.class), mock(PropertySpecService.class));
            Class<? extends DeviceProtocolDialect> dialectClass = customPropertySet.getDeviceProtocolDialect().getClass();

            assertThat(dialectClasses.contains(dialectClass))
                    .as("Encountered an dialect custom property set (" + dialectClass.getName() + ") not related to a known device protocol dialect").isTrue();

            assertThat(new DialectCustomPropertySetNameDetective().customPropertySetClassNameFor(dialectClass))
                    .as("No or invalid mapping for device protocol dialect " + dialectClass.getName()+" found in dialect-custom-property-set-mapping.properties")
                    .isEqualTo(customPropertySetClass.getName());
            dialectClasses.remove(dialectClass);
        }

        if (!dialectClasses.isEmpty()) {
            throw new AssertionError("Encountered device protocol dialect(s) without proper custom property support:\r\n" + asString(dialectClasses));
        }
    }

    private String asString(List<Class<? extends AbstractDeviceProtocolDialect>> dialectClasses) {
        StringBuilder builder = new StringBuilder();
        dialectClasses.forEach(dClass -> builder.append("\t").append(dClass.getName()).append("\r\n"));
        return builder.toString();
    }
}