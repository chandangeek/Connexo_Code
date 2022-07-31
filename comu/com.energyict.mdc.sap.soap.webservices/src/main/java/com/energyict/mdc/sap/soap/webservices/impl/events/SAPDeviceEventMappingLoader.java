/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.events;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.time.Clock;
import java.util.Optional;
import java.util.stream.Stream;

public class SAPDeviceEventMappingLoader {
    private static final String BATCH_EXECUTOR_USER_NAME = "batch executor";
    private static final String PROPERTY_CSV_PATH_NAME = "com.elster.jupiter.sap.eventmapping.csv.path";
    private static final String PROPERTY_CSV_PATH_DEFAULT = "./sap/event-mapping.csv";
    private static final String PROPERTY_CSV_SEPARATOR_NAME = "com.elster.jupiter.sap.eventmapping.csv.separator";
    private static final String PROPERTY_CSV_SEPARATOR_DEFAULT = ";";
    private static final String PROPERTY_COLUMN_VALUE_SEPARATOR = "com.elster.jupiter.sap.eventmapping.column.value.separator";
    private static final String PROPERTY_COLUMN_VALUE_SEPARATOR_DEFAULT = "/";
    private static final String PROPERTY_DISABLE_PROPERTY_TAG = "com.elster.jupiter.sap.eventpropertytag.disable";
    private static final String PROPERTY_DISABLE_PROPERTY_TAG_DEFAULT = "true";

    private static final String SERVICE_CALL_TYPE_NAME = TranslationKeys.SAP_EVENT_MAPPING_LOADING_SC_TYPE.getDefaultFormat();
    private static final String SERVICE_CALL_TYPE_VERSION = "1.0";

    private final BundleContext bundleContext;
    private final FileSystem fileSystem;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final DeviceService deviceService;
    private final MeteringGroupsService meteringGroupsService;
    private final Clock сlock;
    private final SAPDeviceEventMappingStatusCustomPropertySet eventMappingStatusCPS;
    private final UserService userService;
    private final ThreadPrincipalService threadPrincipalService;
    private final TransactionService transactionService;
    private final ForwardedDeviceEventTypesFormatter formatter;

    private ServiceCallType serviceCallType;

    @Inject
    SAPDeviceEventMappingLoader(BundleContext bundleContext,
                                FileSystem fileSystem,
                                ServiceCallService serviceCallService,
                                CustomPropertySetService customPropertySetService,
                                SAPCustomPropertySets sapCustomPropertySets,
                                UserService userService,
                                ThreadPrincipalService threadPrincipalService,
                                TransactionService transactionService,
                                SAPDeviceEventMappingStatusCustomPropertySet eventMappingStatusCPS,
                                DeviceService deviceService,
                                MeteringGroupsService meteringGroupsService,
                                Clock сlock) {
        this.bundleContext = bundleContext;
        this.fileSystem = fileSystem;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.userService = userService;
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.eventMappingStatusCPS = eventMappingStatusCPS;
        this.deviceService = deviceService;
        this.meteringGroupsService = meteringGroupsService;
        this.сlock = сlock;
        formatter = new ForwardedDeviceEventTypesFormatter(sapCustomPropertySets, deviceService, meteringGroupsService, сlock);
    }

    public ForwardedDeviceEventTypesFormatter loadMapping() {
        setSecurityContext();
        String path = getProperty(PROPERTY_CSV_PATH_NAME, PROPERTY_CSV_PATH_DEFAULT);
        String columnValueSeparator = getProperty(PROPERTY_COLUMN_VALUE_SEPARATOR, PROPERTY_COLUMN_VALUE_SEPARATOR_DEFAULT);
        String separator = getProperty(PROPERTY_CSV_SEPARATOR_NAME, PROPERTY_CSV_SEPARATOR_DEFAULT);
        formatter.setDisablePropertyTag(Boolean.valueOf(getProperty(PROPERTY_DISABLE_PROPERTY_TAG, PROPERTY_DISABLE_PROPERTY_TAG_DEFAULT)));
        ServiceCall serviceCall = createServiceCall(path, separator, columnValueSeparator);
        try (InputStream input = Files.newInputStream(fileSystem.getPath(path));
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")))) {
            if (separator.equalsIgnoreCase(columnValueSeparator)) {
                throw separatorsAreNotDifferentException();
            }
            LineCounter lineCounter = new LineCounter();
            reader.lines()
                    .peek(line -> lineCounter.newLine())
                    .skip(1) // header line
                    .flatMap(line -> {
                        try {
                            SAPDeviceEventType eventType = SAPDeviceEventType.parseFromCsvEntry(line, separator, columnValueSeparator);
                            if (eventType.isForwardedToSap() && formatter.contains(eventType)) {
                                throw duplicateEventCodeOrDeviceEventCodeException(eventType);
                            }
                            lineCounter.success(!eventType.isForwardedToSap());
                            return Stream.of(eventType);
                        } catch (Exception e) {
                            log(serviceCall, "Failed to load line " + lineCounter.getIndex() + ": " + e.getLocalizedMessage(), e);
                            lineCounter.failure();
                            return Stream.empty();
                        }
                    })
                    .filter(SAPDeviceEventType::isForwardedToSap)
                    .forEach(formatter::add);
            closeServiceCall(serviceCall, lineCounter.getFailed(), lineCounter.getLoaded(), lineCounter.getSkipped());
        } catch (Exception e) {
            log(serviceCall, e);
            closeServiceCall(serviceCall, 0, 0, 0);
        }
        return formatter;
    }

    private static Exception separatorsAreNotDifferentException() {
        return new Exception("CSV separator and separator in a column for a list of values must have different values.");
    }

    private static Exception duplicateEventCodeOrDeviceEventCodeException(SAPDeviceEventType eventType) {
        return new Exception("Non-unique event identification code(s) " + toString(eventType) +
                ". Combination of " + SAPDeviceEventType.CsvField.EVENT_CODE.name() + " & " + SAPDeviceEventType.CsvField.DEVICE_EVENT_CODE.name() +
                " on respective positions " + SAPDeviceEventType.CsvField.EVENT_CODE.position() + " & " + SAPDeviceEventType.CsvField.DEVICE_EVENT_CODE.position() + " must be unique.");
    }

    private static String toString(SAPDeviceEventType eventType) {
        return eventType.getEventCode()
                .orElseGet(() -> SAPDeviceEventType.NO_EVENT_CODE + eventType.getDeviceEventCode()
                        .map(code -> " (" + code + ')')
                        .orElse(""));
    }

    private void setSecurityContext() {
        if (threadPrincipalService.getPrincipal() == null) {
            userService.findUser(BATCH_EXECUTOR_USER_NAME, userService.getRealm())
                    .ifPresent(threadPrincipalService::set);
        }
    }

    private String getProperty(String name, String defaultValue) {
        String value = bundleContext.getProperty(name);
        return value == null ? defaultValue : value;
    }

    private ServiceCall createServiceCall(String path, String separator, String columnValueSeparator) {
        try (TransactionContext context = transactionService.getContext()) {
            SAPDeviceEventMappingStatusDomainExtension properties = new SAPDeviceEventMappingStatusDomainExtension();
            properties.setPath(path);
            properties.setSeparator(separator);
            properties.setColumnValueSeparator(columnValueSeparator);
            ServiceCall serviceCall = getServiceCallType().newServiceCall()
                    .origin(WebServiceActivator.APPLICATION_NAME)
                    .extendedWith(properties)
                    .create();
            serviceCall.requestTransition(DefaultState.PENDING);
            serviceCall.requestTransition(DefaultState.ONGOING);
            context.commit();
            return serviceCall;
        }
    }

    private void log(ServiceCall serviceCall, String message, Exception e) {
        try (TransactionContext context = transactionService.getContext()) {
            serviceCall.log(message, e);
            context.commit();
        }
    }

    private void log(ServiceCall serviceCall, Exception e) {
        log(serviceCall, e.getLocalizedMessage(), e);
    }

    private void closeServiceCall(ServiceCall serviceCall, int failed, int loaded, int skipped) {
        try (TransactionContext context = transactionService.getContext()) {
            SAPDeviceEventMappingStatusDomainExtension properties = serviceCall.getExtensionFor(eventMappingStatusCPS)
                    .orElseThrow(() -> new IllegalStateException("Couldn't find extension for service call " + serviceCall.getNumber() + '.'));
            properties.setFailedEntriesNumber(failed);
            properties.setLoadedEntriesNumber(loaded);
            properties.setSkippedEntriesNumber(skipped);
            serviceCall.update(properties);
            if (loaded == 0) {
                serviceCall.log(LogLevel.SEVERE, "No event entry has been loaded from mapping file.");
                serviceCall.requestTransition(DefaultState.FAILED);
            } else {
                if (loaded == skipped) {
                    serviceCall.log(LogLevel.WARNING, "None of the event entries loaded from mapping file is forwarded to SAP.");
                }
                if (failed == 0) {
                    serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                } else {
                    serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
                }
            }
            context.commit();
        }
    }

    private ServiceCallType getServiceCallType() {
        if (serviceCallType == null) {
            serviceCallService.addServiceCallHandler(ServiceCallHandler.DUMMY, ImmutableMap.of("name", SERVICE_CALL_TYPE_NAME));
            serviceCallType = findServiceCallType()
                    .orElseGet(this::createServiceCallType);
        }
        return serviceCallType;
    }

    private Optional<ServiceCallType> findServiceCallType() {
        return serviceCallService.findServiceCallType(SERVICE_CALL_TYPE_NAME, SERVICE_CALL_TYPE_VERSION);
    }

    private ServiceCallType createServiceCallType() {
        return serviceCallService.createServiceCallType(SERVICE_CALL_TYPE_NAME, SERVICE_CALL_TYPE_VERSION, "MDC")
                .customPropertySet(getCustomPropertySet())
                .handler(SERVICE_CALL_TYPE_NAME)
                .create();
    }

    private RegisteredCustomPropertySet getCustomPropertySet() {
        return customPropertySetService.findActiveCustomPropertySet(eventMappingStatusCPS.getId())
                .orElseThrow(() -> new IllegalStateException("Couldn't find custom property set '" + SAPDeviceEventMappingStatusCustomPropertySet.CUSTOM_PROPERTY_SET_ID + "'."));
    }

    private static class LineCounter {
        private int index, loaded, failed, skipped;

        void newLine() {
            ++index;
        }

        void success(boolean skipped) {
            ++loaded;
            if (skipped) {
                ++this.skipped;
            }
        }

        void failure() {
            ++failed;
        }

        int getIndex() {
            return index;
        }

        int getFailed() {
            return failed;
        }

        int getLoaded() {
            return loaded;
        }

        int getSkipped() {
            return skipped;
        }
    }
}
