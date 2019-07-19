/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.impl.EventType;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
public class TimeOfUseCampaignDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall>, TimeOfUseCampaign {

    public enum FieldNames {
        DOMAIN("serviceCall", "service_call"),
        NAME_OF_CAMPAIGN("name", "name"),
        DEVICE_TYPE("deviceType", "device_type"),
        DEVICE_GROUP("deviceGroup", "device_group"),
        UPLOAD_PERIOD_START("uploadPeriodStart", "activation_start"),
        UPLOAD_PERIOD_END("uploadPeriodEnd", "activation_end"),
        CALENDAR("calendar", "calendar"),
        ACTIVATION_OPTION("activationOption", "activation_option"),
        ACTIVATION_DATE("activationDate", "activation_date"),
        UPDATE_TYPE("updateType", "update_type"),
        VALIDATION_TIMEOUT("validationTimeout", "validation_timeout"),
        WITH_UNIQUE_CALENDAR_NAME("withUniqueCalendarName", "with_unique_calendar_name"),
        VALIDATION_COMTASK_ID("validationComTaskId", "VALIDATION_COMTASK_ID"),
        CALENDAR_UPLOAD_COMTASK_ID("calendarUploadComTaskId", "CALENDAR_UPLOAD_COMTASK_ID"),
        VALIDATION_CONNECTIONSTRATEGY("validationConnectionStrategy", "VALIDATION_CONSTRATEGY"),
        CALENDAR_UPLOAD_CONNECTIONSTRATEGY("calendarUploadConnectionStrategy", "CALENDAR_UPLOAD_CONSTRATEGY"),
        ;

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

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;
    private final EventService eventService;

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
    private Instant uploadPeriodStart;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant uploadPeriodEnd;
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
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private boolean withUniqueCalendarName;
    private long calendarUploadComTaskId;
    private ConnectionStrategy calendarUploadConnectionStrategy;
    private long validationComTaskId;
    private ConnectionStrategy validationConnectionStrategy;

    @Inject
    public TimeOfUseCampaignDomainExtension(TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        super();
        this.dataModel = timeOfUseCampaignService.getDataModel();
        thesaurus = dataModel.getInstance(Thesaurus.class);
        serviceCallService = dataModel.getInstance(ServiceCallService.class);
        eventService = dataModel.getInstance(EventService.class);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
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
    public Instant getUploadPeriodStart() {
        return uploadPeriodStart;
    }

    public void setUploadPeriodStart(Instant activationStart) {
        this.uploadPeriodStart = activationStart;
    }

    @Override
    public Instant getUploadPeriodEnd() {
        return uploadPeriodEnd;
    }

    public void setUploadPeriodEnd(Instant uploadPeriodEnd) {
        this.uploadPeriodEnd = uploadPeriodEnd;
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
    public ConnectionStrategy getCalendarUploadConnectionStrategy() {
        return calendarUploadConnectionStrategy;
    }

    @Override
    public Optional<ConnectionStrategy> getValidationConnectionStrategy() {
        return Optional.of(validationConnectionStrategy);
    }

    @Override
    public void setCalendarUploadConnectionStrategy(ConnectionStrategy calendarUploadConnectionStrategy) {
        this.calendarUploadConnectionStrategy = calendarUploadConnectionStrategy;
    }

    @Override
    public void setValidationConnectionStrategy(ConnectionStrategy validationConnectionStrategy) {
        this.validationConnectionStrategy = validationConnectionStrategy;
    }

    @Override
    public long getCalendarUploadComTaskId() {
        return calendarUploadComTaskId;
    }

    @Override
    public long getValidationComTaskId() {
        return validationComTaskId;
    }

    @Override
    public void setCalendarUploadComTaskId(long calendarUploadComTaskId) {
        this.calendarUploadComTaskId = calendarUploadComTaskId;
    }

    @Override
    public void setValidationComTaskId(long validationComTaskId) {
        this.validationComTaskId = validationComTaskId;
    }

    @Override
    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public Map<DefaultState, Long> getNumbersOfChildrenWithStatuses() {
        return dataModel.getInstance(ServiceCallService.class).getChildrenStatus(getServiceCall().getId());
    }

    @Override
    public void update() {
        getServiceCall().update(this);
        eventService.postEvent(EventType.TOU_CAMPAIGN_EDITED.topic(), this);
    }

    @Override
    public void cancel() {
        ServiceCall serviceCall = getServiceCall();
        if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
            serviceCall.requestTransition(DefaultState.CANCELLED);
            serviceCall.update(this);
            serviceCall.log(LogLevel.INFO, thesaurus.getSimpleFormat(MessageSeeds.CANCELED_BY_USER).format());
        }
    }

    @Override
    public void delete() {
        getServiceCall().delete();
    }

    @Override
    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    @Override
    public boolean isWithUniqueCalendarName() {
        return withUniqueCalendarName;
    }

    public void setWithUniqueCalendarName(boolean withUniqueCalendarName) {
        this.withUniqueCalendarName = withUniqueCalendarName;
    }

    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(domainInstance);
        this.setName((String) propertyValues.getProperty(FieldNames.NAME_OF_CAMPAIGN.javaName()));
        this.setDeviceType((DeviceType) propertyValues.getProperty(FieldNames.DEVICE_TYPE.javaName()));
        this.setDeviceGroup((String) propertyValues.getProperty(FieldNames.DEVICE_GROUP.javaName()));
        this.setUploadPeriodStart((Instant) propertyValues.getProperty(FieldNames.UPLOAD_PERIOD_START.javaName()));
        this.setUploadPeriodEnd((Instant) propertyValues.getProperty(FieldNames.UPLOAD_PERIOD_END.javaName()));
        this.setCalendar((Calendar) propertyValues.getProperty(FieldNames.CALENDAR.javaName()));
        this.setActivationOption((String) propertyValues.getProperty(FieldNames.ACTIVATION_OPTION.javaName()));
        this.setActivationDate((Instant) propertyValues.getProperty(FieldNames.ACTIVATION_DATE.javaName()));
        this.setUpdateType((String) propertyValues.getProperty(FieldNames.UPDATE_TYPE.javaName()));
        this.setValidationTimeout((long) propertyValues.getProperty(FieldNames.VALIDATION_TIMEOUT.javaName()));
        this.setWithUniqueCalendarName((boolean) propertyValues.getProperty(FieldNames.WITH_UNIQUE_CALENDAR_NAME.javaName()));
        this.setValidationComTaskId((long) propertyValues.getProperty(FieldNames.VALIDATION_COMTASK_ID.javaName()));
        this.setCalendarUploadComTaskId((long) propertyValues.getProperty(FieldNames.CALENDAR_UPLOAD_COMTASK_ID.javaName()));
        this.setValidationConnectionStrategy((ConnectionStrategy) propertyValues.getProperty(FieldNames.VALIDATION_CONNECTIONSTRATEGY.javaName()));
        this.setCalendarUploadConnectionStrategy((ConnectionStrategy) propertyValues.getProperty(FieldNames.CALENDAR_UPLOAD_CONNECTIONSTRATEGY.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.NAME_OF_CAMPAIGN.javaName(), this.getName());
        propertySetValues.setProperty(FieldNames.DEVICE_TYPE.javaName(), this.getDeviceType());
        propertySetValues.setProperty(FieldNames.DEVICE_GROUP.javaName(), this.getDeviceGroup());
        propertySetValues.setProperty(FieldNames.UPLOAD_PERIOD_START.javaName(), this.getUploadPeriodStart());
        propertySetValues.setProperty(FieldNames.UPLOAD_PERIOD_END.javaName(), this.getUploadPeriodEnd());
        propertySetValues.setProperty(FieldNames.CALENDAR.javaName(), this.getCalendar());
        propertySetValues.setProperty(FieldNames.ACTIVATION_OPTION.javaName(), this.getActivationOption());
        propertySetValues.setProperty(FieldNames.ACTIVATION_DATE.javaName(), this.getActivationDate());
        propertySetValues.setProperty(FieldNames.UPDATE_TYPE.javaName(), this.getUpdateType());
        propertySetValues.setProperty(FieldNames.VALIDATION_TIMEOUT.javaName(), this.getValidationTimeout());
        propertySetValues.setProperty(FieldNames.WITH_UNIQUE_CALENDAR_NAME.javaName(), this.isWithUniqueCalendarName());
        propertySetValues.setProperty(FieldNames.VALIDATION_COMTASK_ID.javaName(), this.getValidationComTaskId());
        propertySetValues.setProperty(FieldNames.CALENDAR_UPLOAD_COMTASK_ID.javaName(), this.getCalendarUploadComTaskId());
        propertySetValues.setProperty(FieldNames.VALIDATION_CONNECTIONSTRATEGY.javaName(), this.getValidationConnectionStrategy().get());
        propertySetValues.setProperty(FieldNames.CALENDAR_UPLOAD_CONNECTIONSTRATEGY.javaName(), this.getCalendarUploadConnectionStrategy());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}