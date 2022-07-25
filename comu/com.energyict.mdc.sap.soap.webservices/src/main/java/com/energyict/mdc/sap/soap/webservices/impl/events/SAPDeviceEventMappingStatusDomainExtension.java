/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.events;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

    private int loadedEntriesNumber;
    private int failedEntriesNumber;
    private int skippedEntriesNumber;
    @NotNull
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class})
    private String path;
    @NotNull
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class})
    private String separator;

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    public int getFailedEntriesNumber() {
        return failedEntriesNumber;
    }

    public void setFailedEntriesNumber(int failedEntriesNumber) {
        this.failedEntriesNumber = failedEntriesNumber;
    }

    public int getLoadedEntriesNumber() {
        return loadedEntriesNumber;
    }

    public void setLoadedEntriesNumber(int loadedEntriesNumber) {
        this.loadedEntriesNumber = loadedEntriesNumber;
    }

    public int getSkippedEntriesNumber() {
        return skippedEntriesNumber;
    }

    public void setSkippedEntriesNumber(int skippedEntriesNumber) {
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
        setLoadedEntriesNumber(getLongPropertyAndMapToInt(propertyValues, FieldNames.LOADED_ENTRIES_NUMBER));
        setFailedEntriesNumber(getLongPropertyAndMapToInt(propertyValues, FieldNames.FAILED_ENTRIES_NUMBER));
        setSkippedEntriesNumber(getLongPropertyAndMapToInt(propertyValues, FieldNames.SKIPPED_ENTRIES_NUMBER));
        setPath((String) propertyValues.getProperty(FieldNames.PATH.javaName()));
        setSeparator((String) propertyValues.getProperty(FieldNames.SEPARATOR.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.LOADED_ENTRIES_NUMBER.javaName(), (long) getLoadedEntriesNumber());
        propertySetValues.setProperty(FieldNames.FAILED_ENTRIES_NUMBER.javaName(), (long) getFailedEntriesNumber());
        propertySetValues.setProperty(FieldNames.SKIPPED_ENTRIES_NUMBER.javaName(), (long) getSkippedEntriesNumber());
        propertySetValues.setProperty(FieldNames.PATH.javaName(), getPath());
        propertySetValues.setProperty(FieldNames.SEPARATOR.javaName(), getSeparator());
    }

    private static int getLongPropertyAndMapToInt(CustomPropertySetValues values, FieldNames name) {
        Object value = values.getProperty(name.javaName());
        return value == null ? 0 : Math.toIntExact((Long) value);
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}
