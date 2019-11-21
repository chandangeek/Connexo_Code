package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceEventTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.util.streams.BufferedReaderIterable;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

public class DefaultDeviceEventTypesInstaller {

    private final ServerMeteringService meteringService;
    private static final String IMPORT_FILE_NAME = "enddeviceeventtypes.csv";
    private static final String NOT_APPLICABLE = "n/a";

    @Inject
    DefaultDeviceEventTypesInstaller(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public void installIfNotPresent(Logger logger) {
        createEndDeviceEventTypes(meteringService, getClass(), logger);
    }

    static void createEndDeviceEventTypes(ServerMeteringService meteringService, Class clazz, Logger logger) {
        try (InputStream resourceAsStream = clazz.getClassLoader()
                .getResourceAsStream(clazz.getPackage().getName().replace('.', '/') + '/' + IMPORT_FILE_NAME)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            for (String line : new BufferedReaderIterable(reader)) {
                String[] fields = line.split(",");

                for (EndDeviceType deviceType : endDeviceTypes(fields[0])) {
                    for (EndDeviceDomain domain : domains(fields[1])) {
                        for (EndDeviceSubDomain subDomain : subDomains(fields[2])) {
                            for (EndDeviceEventOrAction eventOrAction : eventOrActions(fields[3])) {
                                String code = EndDeviceEventTypeCodeBuilder
                                        .type(deviceType)
                                        .domain(domain)
                                        .subDomain(subDomain)
                                        .eventOrAction(eventOrAction)
                                        .toCode();
                                if (meteringService.getEndDeviceEventType(code).isPresent()) {
                                    logger.finer("Skipping code " + code + ": already exists");
                                } else {
                                    logger.finer("adding code " + code);
                                    meteringService.createEndDeviceEventType(code);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static Iterable<EndDeviceEventOrAction> eventOrActions(String field) {
        if ("*".equals(field)) {
            return Arrays.asList(EndDeviceEventOrAction.values());
        } else {
            if (NOT_APPLICABLE.equalsIgnoreCase(field)) {
                return Collections.singletonList(EndDeviceEventOrAction.NA);
            } else {
                return Collections.singletonList(EndDeviceEventOrAction.valueOf(sanitized(field)));
            }
        }
    }

    static Iterable<EndDeviceSubDomain> subDomains(String field) {
        if ("*".equals(field)) {
            return Arrays.asList(EndDeviceSubDomain.values());
        } else {
            if (NOT_APPLICABLE.equalsIgnoreCase(field)) {
                return Collections.singletonList(EndDeviceSubDomain.NA);
            } else {
                return Collections.singletonList(EndDeviceSubDomain.valueOf(sanitized(field)));
            }
        }
    }

    static Iterable<EndDeviceDomain> domains(String field) {
        if ("*".equals(field)) {
            return Arrays.asList(EndDeviceDomain.values());
        } else {
            if (NOT_APPLICABLE.equalsIgnoreCase(field)) {
                return Collections.singletonList(EndDeviceDomain.NA);
            } else {
                return Collections.singletonList(EndDeviceDomain.valueOf(sanitized(field)));
            }
        }
    }

    static Iterable<EndDeviceType> endDeviceTypes(String field) {
        if ("*".equals(field)) {
            return Arrays.asList(EndDeviceType.values());
        } else {
            if (NOT_APPLICABLE.equalsIgnoreCase(field)) {
                return Collections.singletonList(EndDeviceType.NA);
            } else {
                return Collections.singletonList(EndDeviceType.valueOf(sanitized(field)));
            }
        }
    }


    private static String sanitized(String field) {
        return field.toUpperCase().replaceAll("[\\-%]", "");
    }

}