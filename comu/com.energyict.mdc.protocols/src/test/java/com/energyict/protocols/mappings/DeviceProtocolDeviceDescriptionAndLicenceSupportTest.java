/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mappings;

import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.upl.DeviceDescriptionSupport;
import com.energyict.mdc.upl.DeviceProtocol;

import com.energyict.license.LicensedProtocolRule;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that every {@link DeviceProtocol} has a unique protocol description and license ID assigned.
 *
 * @author Stijn Vanhoorelbeke
 * @since 26.09.17 - 11:16
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolDeviceDescriptionAndLicenceSupportTest {

    @Test
    public void testEveryDeviceProtocolHasUniqueProtocolDescription() throws Exception {
        Reflections reflections = new Reflections("com.energyict");
        List<Class<? extends DeviceDescriptionSupport>> classes = reflections.getSubTypesOf(DeviceDescriptionSupport.class)
                .stream()
                .filter(aClass -> !aClass.getSimpleName().equals("MeterProtocolAdapterImpl")
                        && !aClass.getSimpleName().equals("SmartMeterProtocolAdapterImpl")
                        && !aClass.getSimpleName().equals("UPLDeviceProtocolAdapter")
                        && !aClass.getSimpleName().equals("UPLMeterProtocolAdapter")
                        && !aClass.getSimpleName().equals("UPLSmartMeterProtocolAdapter"))
                .collect(Collectors.toList());

        reflections = new Reflections("com.elster");
        classes.addAll(reflections.getSubTypesOf(DeviceDescriptionSupport.class));

        classes = classes
                .stream()
                .filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())) // Filter out abstract classes
                .collect(Collectors.toList());

        Map<String, Class> protocolDescriptionMapping = new HashMap<>();
        Map<String, List<Class>> violations = new HashMap<>();
        for (Class<? extends DeviceDescriptionSupport> protocolClass : classes) {
            DeviceDescriptionSupport deviceDescriptionSupport = mock(protocolClass);        // Little trick to get to the protocol description without creating a new instance of the class
            when(deviceDescriptionSupport.getProtocolDescription()).thenCallRealMethod();   // (which we cannot do, as the protocols don't have a no argument constructor)

            LicensedProtocol licensedProtocol = LicensedProtocolRule.fromClassName(protocolClass.getName());
            if (licensedProtocol == null ||licensedProtocol.getCode() < 10000) { // Filter out the deprecated protocols
                String protocolDescription = deviceDescriptionSupport.getProtocolDescription();
                if (protocolDescriptionMapping.containsKey(protocolDescription)) {
                    List<Class> tempClasses = new ArrayList<>();
                    if (violations.containsKey(protocolDescription)) {
                        tempClasses = violations.get(protocolDescription);
                        tempClasses.add(protocolClass);
                    } else {
                        tempClasses.add(protocolDescriptionMapping.get(protocolDescription));
                        tempClasses.add(protocolClass);
                    }
                    violations.put(protocolDescription, tempClasses);
                }
                protocolDescriptionMapping.put(protocolDescription, protocolClass);
            }
        }

        if (!violations.isEmpty()) {
            throw new AssertionError(" One or more protocol descriptions are used multiple times, whilst the protocol description should be unique per protocol! \r\n" +
                    "Issues:\r\n" +
                    asString(violations));
        }
    }

    @Test
    public void testEveryDeviceProtocolHasLicenseId() throws Exception {
        Reflections reflections = new Reflections("com.energyict");
        List<Class<? extends DeviceDescriptionSupport>> classes = reflections.getSubTypesOf(DeviceDescriptionSupport.class)
                .stream()
                .filter(aClass -> !aClass.getSimpleName().equals("MeterProtocolAdapterImpl")
                        && !aClass.getSimpleName().equals("SmartMeterProtocolAdapterImpl")
                        && !aClass.getSimpleName().equals("UPLDeviceProtocolAdapter")
                        && !aClass.getSimpleName().equals("UPLMeterProtocolAdapter")
                        && !aClass.getSimpleName().equals("UPLSmartMeterProtocolAdapter"))
                .collect(Collectors.toList());

        reflections = new Reflections("com.elster");
        classes.addAll(reflections.getSubTypesOf(DeviceDescriptionSupport.class));

        classes = classes
                .stream()
                .filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())) // Filter out abstract classes
                .collect(Collectors.toList());

        List<Class> licenseViolations = new ArrayList<>();
        for (Class<? extends DeviceDescriptionSupport> protocolClass : classes) {
            DeviceDescriptionSupport deviceDescriptionSupport = mock(protocolClass);        // Little trick to get to the protocol description without creating a new instance of the class
            when(deviceDescriptionSupport.getProtocolDescription()).thenCallRealMethod();   // (which we cannot do, as the protocols don't have a no argument constructor)

            LicensedProtocol licensedProtocol = LicensedProtocolRule.fromClassName(protocolClass.getName());
            if (licensedProtocol == null) {
                licenseViolations.add(protocolClass);
            }
        }

        if (!licenseViolations.isEmpty()) {
            throw new AssertionError(" One or more device protocols do not have a proper license ID defined. " +
                    "Please add license to com.energyict.license.LicensedProtocolRule!\r\n" + asNewLineString(licenseViolations));
        }
    }

    private String asString(Map<String, List<Class>> violations) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<Class>> entry : violations.entrySet()) {
            builder.append("\t");
            builder.append(entry.getKey()).append(": ");
            builder.append(asString(entry.getValue()));
            builder.append("\r\n");
        }
        return builder.toString();
    }

    private String asString(List<Class> elements) {
        StringBuilder builder = new StringBuilder();
        elements.forEach(element -> builder.append(element.getName()).append(", "));
        return builder.substring(0, builder.lastIndexOf(", ") > -1 ? builder.lastIndexOf(", ") : 0);
    }

    private String asNewLineString(List<Class> elements) {
        StringBuilder builder = new StringBuilder();
        elements.forEach(dClass -> builder.append("\t").append(dClass.getName()).append("\r\n"));
        return builder.toString();
    }
}