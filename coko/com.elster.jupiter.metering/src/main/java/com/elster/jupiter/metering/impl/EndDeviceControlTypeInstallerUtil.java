/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.EndDeviceControlTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.streams.BufferedReaderIterable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EndDeviceControlTypeInstallerUtil {


    private static final String IMPORT_CONTROL_TYPES = "enddevicecontroltypes.csv";
    private static final String NOT_APPLICABLE = "n/a";
    private final MeteringService meteringService;

    public EndDeviceControlTypeInstallerUtil(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public void createEndDeviceControlTypes(Logger logger) {
        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(getClass().getPackage().getName().replace('.', '/') + '/' + IMPORT_CONTROL_TYPES)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            for (String line : new BufferedReaderIterable(reader)) {
                String[] fields = line.split(",");

                for (EndDeviceType deviceType : endDeviceTypes(fields[0])) {
                    for (EndDeviceDomain domain : domains(fields[1])) {
                        for (EndDeviceSubDomain subDomain : subDomains(fields[2])) {
                            for (EndDeviceEventOrAction eventOrAction : eventOrActions(fields[3])) {
                                String code = EndDeviceControlTypeCodeBuilder
                                        .type(deviceType)
                                        .domain(domain)
                                        .subDomain(subDomain)
                                        .eventOrAction(eventOrAction)
                                        .toCode();
                                try {
                                    if (meteringService.getEndDeviceControlType(code).isPresent()) {
                                        logger.finer("Skipping code " + code + ": already exists");
                                    } else {
                                        logger.finer("adding code " + code);
                                        meteringService.createEndDeviceControlType(code);
                                    }
                                } catch (Exception e) {
                                    logger.log(Level.SEVERE, "Error creating EndDeviceType \'" + code + "\' : " + e.getMessage(), e);
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

    private Iterable<EndDeviceEventOrAction> eventOrActions(String field) {
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

    private String sanitized(String field) {
        return field.toUpperCase().replaceAll("[\\-%]", "");
    }

    private Iterable<EndDeviceSubDomain> subDomains(String field) {
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

    private Iterable<EndDeviceDomain> domains(String field) {
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

    private Iterable<EndDeviceType> endDeviceTypes(String field) {
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
}
