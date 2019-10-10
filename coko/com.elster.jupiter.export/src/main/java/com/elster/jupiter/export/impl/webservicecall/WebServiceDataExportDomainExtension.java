/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.impl.MessageSeeds;
import com.elster.jupiter.export.impl.TranslationKeys;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class WebServiceDataExportDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        UUID("uuid", "UUID"),
        TIMEOUT("timeout", "TIMEOUT"),
        ERROR_MESSAGE("errorMessage", "MESSAGE");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private volatile Thesaurus thesaurus;
    private final char SPACE = ' ';
    private final int MILLISECONDS_IN_SECOND = 1000;
    private final int MILLISECONDS_IN_MINUTE = 60000;

    private Reference<ServiceCall> serviceCall = Reference.empty();

    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + "}")
    private String uuid;
    private long timeout;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + "}")
    private String errorMessage;

    @Inject
    public WebServiceDataExportDomainExtension(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @org.osgi.service.component.annotations.Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(DataExportService.COMPONENTNAME, Layer.DOMAIN);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getTimeout() {
        return timeout;
    }

    public String getDisplayTimeout() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("###.###", symbols);
        double timeout = getTimeout();
        return timeout < MILLISECONDS_IN_MINUTE ?
                format.format(timeout / MILLISECONDS_IN_SECOND) + SPACE + thesaurus.getFormat(TranslationKeys.SECONDS).format() :
                format.format(timeout / MILLISECONDS_IN_MINUTE) + SPACE + thesaurus.getFormat(TranslationKeys.MINUTES).format();
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    private void setTimeout(String timeout) {
        int index = timeout.indexOf(SPACE);
        double timeoutValue = Double.valueOf(timeout.substring(0, index));
        this.timeout = timeout.substring(index + 1).equals(thesaurus.getFormat(TranslationKeys.SECONDS).format()) ?
                new Double(timeoutValue * MILLISECONDS_IN_SECOND).longValue() : new Double(timeoutValue * MILLISECONDS_IN_MINUTE).longValue();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        setUuid((String) propertyValues.getProperty(FieldNames.UUID.javaName()));
        setTimeout((String) propertyValues.getProperty(FieldNames.TIMEOUT.javaName()));
        setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.UUID.javaName(), getUuid());
        propertySetValues.setProperty(FieldNames.TIMEOUT.javaName(), getDisplayTimeout());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), getErrorMessage());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}
