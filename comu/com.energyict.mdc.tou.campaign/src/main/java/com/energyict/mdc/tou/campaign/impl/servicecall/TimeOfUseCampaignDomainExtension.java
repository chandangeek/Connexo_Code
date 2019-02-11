/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
public class TimeOfUseCampaignDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall>, TimeOfUseCampaign {

    public TimeOfUseCampaignDomainExtension() {
        super();
    }

    public enum FieldNames {
        DOMAIN("serviceCall", "service_call"),
        NAME_OF_CAMPAIGN("name", "name"),
        DEVICE_TYPE("deviceType", "device_type"),
        DEVICE_GROUP("deviceGroup", "device_group"),
        ACTIVATION_START("activationStart", "activation_start"),
        ACTIVATION_END("activationEnd", "activation_end"),
        CALENDAR("calendar", "calendar"),
        ACTIVATION_OPTION("activationOption", "activation_option"),
        ACTIVATION_DATE("activationDate", "activation_date"),
        UPDATE_TYPE("updateType", "update_type"),
        VALIDATION_TIMEOUT("validationTimeout", "validation_timeout");

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

    private Reference<ServiceCall> serviceCall = Reference.empty();

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent
    private Reference<DeviceType> deviceType = Reference.empty();
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceGroup;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant activationStart;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant activationEnd;
    @IsPresent
    private Reference<Calendar> calendar = Reference.empty();
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String updateType;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String activationOption;
    private Instant activationDate;
    private long validationTimeout;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType.set(deviceType);
    }

    @Override
    public String getDeviceGroup() {
        return deviceGroup;
    }

    public void setDeviceGroup(String deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    @Override
    public Instant getActivationStart() {
        return activationStart;
    }

    public void setActivationStart(Instant activationStart) {
        this.activationStart = activationStart;
    }

    @Override
    public Instant getActivationEnd() {
        return activationEnd;
    }

    public void setActivationEnd(Instant activationEnd) {
        this.activationEnd = activationEnd;
    }

    @Override
    public Calendar getCalendar() {
        return calendar.get();
    }

    public void setCalendar(Calendar calendar) {
        this.calendar.set(calendar);
    }

    @Override
    public String getActivationOption() {
        return activationOption;
    }

    public void setActivationOption(String activationOption) {
        this.activationOption = activationOption;
    }

    @Override
    public Instant getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Instant activationDate) {
        this.activationDate = activationDate;
    }

    @Override
    public long getValidationTimeout() {
        return validationTimeout;
    }

    @Override
    public long getId() {
        return serviceCall.get().getId();
    }

    public void setValidationTimeout(long validationTimeout) {
        this.validationTimeout = validationTimeout;
    }

    @Override
    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(domainInstance);
        this.setName((String) propertyValues.getProperty(FieldNames.NAME_OF_CAMPAIGN.javaName()));
        this.setDeviceType((DeviceType) propertyValues.getProperty(FieldNames.DEVICE_TYPE.javaName()));
        this.setDeviceGroup((String) propertyValues.getProperty(FieldNames.DEVICE_GROUP.javaName()));
        this.setActivationStart((Instant) propertyValues.getProperty(FieldNames.ACTIVATION_START.javaName()));
        this.setActivationEnd((Instant) propertyValues.getProperty(FieldNames.ACTIVATION_END.javaName()));
        this.setCalendar((Calendar) propertyValues.getProperty(FieldNames.CALENDAR.javaName()));
        this.setActivationOption((String) propertyValues.getProperty(FieldNames.ACTIVATION_OPTION.javaName()));
        this.setActivationDate((Instant) propertyValues.getProperty(FieldNames.ACTIVATION_DATE.javaName()));
        this.setUpdateType((String) propertyValues.getProperty(FieldNames.UPDATE_TYPE.javaName()));
        this.setValidationTimeout((long) propertyValues.getProperty(FieldNames.VALIDATION_TIMEOUT.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.NAME_OF_CAMPAIGN.javaName(), this.getName());
        propertySetValues.setProperty(FieldNames.DEVICE_TYPE.javaName(), this.getDeviceType());
        propertySetValues.setProperty(FieldNames.DEVICE_GROUP.javaName(), this.getDeviceGroup());
        propertySetValues.setProperty(FieldNames.ACTIVATION_START.javaName(), this.getActivationStart());
        propertySetValues.setProperty(FieldNames.ACTIVATION_END.javaName(), this.getActivationEnd());
        propertySetValues.setProperty(FieldNames.CALENDAR.javaName(), this.getCalendar());
        propertySetValues.setProperty(FieldNames.ACTIVATION_OPTION.javaName(), this.getActivationOption());
        propertySetValues.setProperty(FieldNames.ACTIVATION_DATE.javaName(), this.getActivationDate());
        propertySetValues.setProperty(FieldNames.UPDATE_TYPE.javaName(), this.getUpdateType());
        propertySetValues.setProperty(FieldNames.VALIDATION_TIMEOUT.javaName(), this.getValidationTimeout());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}