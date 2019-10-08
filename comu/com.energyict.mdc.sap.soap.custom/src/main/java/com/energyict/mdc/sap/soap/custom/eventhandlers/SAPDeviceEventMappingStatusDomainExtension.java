/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventhandlers;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public class SAPDeviceEventMappingStatusDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames implements TranslationKey {
        SERVICE_CALL("serviceCall", "Service call"),
        LOADED_ENTRIES_NUMBER("loadedEntriesNumber", "Number of successfully loaded event entries"),
        FAILED_ENTRIES_NUMBER("failedEntriesNumber", "Number of event entries failed to load"),
        SKIPPED_ENTRIES_NUMBER("skippedEntriesNumber", "Number of event entries not forwarded to SAP"),
        PATH("path", "Path to event mapping csv"),
        SEPARATOR("separator", "Csv separator");

        private final String javaName;
        private final String defaultFormat;

        FieldNames(String javaName, String defaultFormat) {
            this.javaName = javaName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return javaName;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return name();
        }
    }

    private Reference<ServiceCall> serviceCall = Reference.empty();

    private Integer loadedEntriesNumber;
    private Integer failedEntriesNumber;
    private Integer skippedEntriesNumber;
    @NotNull
    private String path;
    @NotNull
    private String separator;

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    public Optional<Integer> getFailedEntriesNumber() {
        return Optional.ofNullable(failedEntriesNumber);
    }

    public void setFailedEntriesNumber(Integer failedEntriesNumber) {
        this.failedEntriesNumber = failedEntriesNumber;
    }

    public Optional<Integer> getLoadedEntriesNumber() {
        return Optional.ofNullable(loadedEntriesNumber);
    }

    public void setLoadedEntriesNumber(Integer loadedEntriesNumber) {
        this.loadedEntriesNumber = loadedEntriesNumber;
    }

    public Optional<Integer> getSkippedEntriesNumber() {
        return Optional.ofNullable(skippedEntriesNumber);
    }

    public void setSkippedEntriesNumber(Integer skippedEntriesNumber) {
        this.skippedEntriesNumber = skippedEntriesNumber;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        serviceCall.set(domainInstance);
        setLoadedEntriesNumber(Math.toIntExact((Long) propertyValues.getProperty(FieldNames.LOADED_ENTRIES_NUMBER.javaName())));
        setFailedEntriesNumber(Math.toIntExact((Long) propertyValues.getProperty(FieldNames.FAILED_ENTRIES_NUMBER.javaName())));
        setSkippedEntriesNumber(Math.toIntExact((Long) propertyValues.getProperty(FieldNames.SKIPPED_ENTRIES_NUMBER.javaName())));
        setPath((String) propertyValues.getProperty(FieldNames.PATH.javaName()));
        setSeparator((String) propertyValues.getProperty(FieldNames.SEPARATOR.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.LOADED_ENTRIES_NUMBER.javaName(), getLoadedEntriesNumber().map(Integer::longValue).orElse(null));
        propertySetValues.setProperty(FieldNames.FAILED_ENTRIES_NUMBER.javaName(), getFailedEntriesNumber().map(Integer::longValue).orElse(null));
        propertySetValues.setProperty(FieldNames.SKIPPED_ENTRIES_NUMBER.javaName(), getSkippedEntriesNumber().map(Integer::longValue).orElse(null));
        propertySetValues.setProperty(FieldNames.PATH.javaName(), getPath());
        propertySetValues.setProperty(FieldNames.SEPARATOR.javaName(), getSeparator());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}
