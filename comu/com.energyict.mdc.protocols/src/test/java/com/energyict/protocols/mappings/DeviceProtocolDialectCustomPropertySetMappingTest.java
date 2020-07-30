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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
        List<Class<? extends AbstractDeviceProtocolDialect>> dialectClasses = searchDialectClasses("com.energyict");
        dialectClasses.addAll(searchDialectClasses("com.elster"));
        dialectClasses.addAll(searchDialectClasses("test.com.energyict"));

        List<Class<? extends AbstractDialectCustomPropertySet>> dialectCustomPropertySetClasses = searchDialectCustomPropertySetClasses("com.energyict");
        dialectCustomPropertySetClasses.addAll(searchDialectCustomPropertySetClasses("com.elster"));
        dialectCustomPropertySetClasses.addAll(searchDialectCustomPropertySetClasses("test.com.energyict"));

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

    private List<Class<? extends AbstractDeviceProtocolDialect>> searchDialectClasses(String prefix) {
        return new Reflections(prefix).getSubTypesOf(AbstractDeviceProtocolDialect.class)
                .stream()
                .filter(aClass -> !aClass.getSimpleName().equals("NoParamsDeviceProtocolDialect"))
                .filter(aClass -> (!Modifier.isAbstract( aClass.getModifiers())))
                .collect(Collectors.toList());
    }

    private List<Class<? extends AbstractDialectCustomPropertySet>> searchDialectCustomPropertySetClasses(String prefix) {
        return new ArrayList<>(new Reflections(prefix).getSubTypesOf(AbstractDialectCustomPropertySet.class));
    }

    private String asString(List<Class<? extends AbstractDeviceProtocolDialect>> dialectClasses) {
        StringBuilder builder = new StringBuilder();
        dialectClasses.forEach(dClass -> builder.append("\t").append(dClass.getName()).append("\r\n"));
        return builder.toString();
    }
}