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
import com.energyict.mdc.tou.campaign.impl.TranslationKeys;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
public class TimeOfUseCampaignDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall>, TimeOfUseCampaign {

    private Thesaurus thesaurus;

    public TimeOfUseCampaignDomainExtension(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

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
        ACTIVATION_DATE("activationDate", "activation_date"),
        UPDATE_TYPE("updateType", "update_type"),
        TIME_VALIDATION("timeValidation", "time_validation");

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
    //    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @IsPresent
    private Reference<Calendar> calendar = Reference.empty();
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String updateType;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String activationDate;
    private long timeValidation;

    // private Instant processDate;
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
    public String getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(String activationDate) {
        this.activationDate = activationDate;
    }

    @Override
    public long getTimeValidation() {
        return timeValidation;
    }

    @Override
    public long getId() {
        return serviceCall.get().getId();
    }

    public void setTimeValidation(long timeValidation) {
        this.timeValidation = timeValidation;
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
        this.setActivationDate((String) propertyValues.getProperty(FieldNames.ACTIVATION_DATE.javaName()));
        this.setUpdateType((String) propertyValues.getProperty(FieldNames.UPDATE_TYPE.javaName()));
        this.setTimeValidation((long) propertyValues.getProperty(FieldNames.TIME_VALIDATION.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.NAME_OF_CAMPAIGN.javaName(), this.getName());
        propertySetValues.setProperty(FieldNames.DEVICE_TYPE.javaName(), this.getDeviceType());
        propertySetValues.setProperty(FieldNames.DEVICE_GROUP.javaName(), this.getDeviceGroup());
        propertySetValues.setProperty(FieldNames.ACTIVATION_START.javaName(), this.getActivationStart());
        propertySetValues.setProperty(FieldNames.ACTIVATION_END.javaName(), this.getActivationEnd());
        propertySetValues.setProperty(FieldNames.CALENDAR.javaName(), this.getCalendar());
        propertySetValues.setProperty(FieldNames.ACTIVATION_DATE.javaName(), this.getActivationDate());
        propertySetValues.setProperty(FieldNames.UPDATE_TYPE.javaName(), this.getUpdateType());
        propertySetValues.setProperty(FieldNames.TIME_VALIDATION.javaName(), this.getTimeValidation());
    }

    public String getFormattedActivationDate() {
        if (activationDate.equals(TranslationKeys.IMMEDIATELY.getKey())) {
            return thesaurus.getString(TranslationKeys.IMMEDIATELY.getKey(), TranslationKeys.IMMEDIATELY.getDefaultFormat());
        } else if (activationDate.equals(TranslationKeys.WITHOUT_ACTIVATION.getKey())) {
            return thesaurus.getString(TranslationKeys.WITHOUT_ACTIVATION.getKey(), TranslationKeys.WITHOUT_ACTIVATION.getDefaultFormat());
        } else {
            return activationDate;
        }

    }

    public String getFormattedUpdateType() {
        if (updateType.equals(TranslationKeys.FULL_CALENDAR.getKey())) {
            return thesaurus.getString(TranslationKeys.FULL_CALENDAR.getKey(), TranslationKeys.FULL_CALENDAR.getDefaultFormat());
        } else {
            return thesaurus.getString(TranslationKeys.SPECIAL_DAYS.getKey(), TranslationKeys.SPECIAL_DAYS.getDefaultFormat());
        }
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}