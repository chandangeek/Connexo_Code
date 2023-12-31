/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignException;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;
import com.energyict.mdc.tou.campaign.impl.TranslationKeys;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.energyict.mdc.common.protocol.DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND;
import static com.energyict.mdc.common.protocol.DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE;
import static com.energyict.mdc.common.protocol.DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_SEND;
import static com.energyict.mdc.common.protocol.DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATE;
import static com.energyict.mdc.common.protocol.DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME;
import static com.energyict.mdc.common.protocol.DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_CONTRACT;
import static com.energyict.mdc.common.protocol.DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE;
import static com.energyict.mdc.common.protocol.DeviceMessageId.ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME;

public class TimeOfUseSendHelper {

    private final Thesaurus thesaurus;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final TimeOfUseCampaignServiceImpl timeOfUseCampaignService;

    @Inject
    public TimeOfUseSendHelper(Thesaurus thesaurus, DeviceConfigurationService deviceConfigurationService,
                               DeviceMessageSpecificationService deviceMessageSpecificationService,
                               TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        this.thesaurus = thesaurus;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    public void setCalendarOnDevice(TimeOfUseItemDomainExtension timeOfUseItemDomainExtension, ServiceCall serviceCall) {
        Device device = timeOfUseItemDomainExtension.getDevice();
        timeOfUseCampaignService.revokeCalendarsCommands(device);
        TimeOfUseCampaign timeOfUseCampaign =
                serviceCall.getParent()
                        .orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.SERVICE_CALL_PARENT_NOT_FOUND))
                        .getExtension(TimeOfUseCampaignDomainExtension.class)
                        .orElse(null);
        if (timeOfUseCampaignService.isWithVerification(timeOfUseCampaign)) {
            Optional<ComTaskExecution> verificationTaskOptional = timeOfUseItemDomainExtension.findOrCreateVerificationComTaskExecution();
            if (verificationTaskOptional.isPresent()) {
                ComTaskExecution verificationTask = verificationTaskOptional.get();
                if (verificationTask.getConnectionTask().isPresent()) {
                    ConnectionTask connectionTask = verificationTask.getConnectionTask().get();
                    ConnectionStrategy connectionStrategy = ((ScheduledConnectionTask) connectionTask).getConnectionStrategy();
                    if (!(connectionTask.isActive() && (!timeOfUseCampaign.getValidationConnectionStrategy().isPresent()
                            || connectionStrategy == timeOfUseCampaign.getValidationConnectionStrategy().get()))) {
                        serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT)
                                .format(thesaurus.getFormat(TranslationKeys.valueOf(timeOfUseCampaign.getValidationConnectionStrategy().get().name())).format(), verificationTask.getComTask()
                                        .getName()));
                        if (serviceCall.canTransitionTo(DefaultState.REJECTED)) {
                            serviceCall.requestTransition(DefaultState.REJECTED);
                        }
                        return;
                    }
                } else {
                    serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_MISSING_ON_COMTASK).format(verificationTask.getComTask().getName()));
                    if (serviceCall.canTransitionTo(DefaultState.REJECTED)) {
                        serviceCall.requestTransition(DefaultState.REJECTED);
                    }
                    return;
                }
            } else {
                serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.TASK_FOR_VALIDATION_IS_MISSING).format());
                if (serviceCall.canTransitionTo(DefaultState.REJECTED)) {
                    serviceCall.requestTransition(DefaultState.REJECTED);
                }
                return;
            }
        }
        Optional<ComTaskExecution> comTaskExecutionOptional = timeOfUseItemDomainExtension.findOrCreateUploadComTaskExecution();
        if (comTaskExecutionOptional.isPresent()) {
            SendCalendarInfo sendCalendarInfo = new SendCalendarInfo();
            sendCalendarInfo.allowedCalendarId = timeOfUseCampaign.getCalendar().getId();
            sendCalendarInfo.activationDate = timeOfUseCampaign.getActivationOption()
                    .equals(TranslationKeys.IMMEDIATELY.getKey()) ? serviceCall.getCreationTime() : timeOfUseCampaign.getActivationDate();
            sendCalendarInfo.calendarUpdateOption = timeOfUseCampaign.getUpdateType();
            sendCalendarInfo.releaseDate = timeOfUseCampaign.getUploadPeriodStart();
            sendCalendarInfo.contract = null;//bigdec
            sendCalendarInfo.type = null;//string
            Set<ProtocolSupportedCalendarOptions> allowedOptions = getAllowedTimeOfUseOptions(device, deviceConfigurationService);
            AllowedCalendar calendar = device.getDeviceType().getAllowedCalendars().stream()
                    .filter(allowedCalendar -> allowedCalendar.getCalendar().isPresent())
                    .filter(allowedCalendar -> !allowedCalendar.isGhost() && allowedCalendar.getCalendar().get().getId() == sendCalendarInfo.allowedCalendarId)
                    .findFirst().orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.UNABLE_TO_FIND_CALENDAR));
            DeviceMessageId deviceMessageId = getDeviceMessageId(sendCalendarInfo, allowedOptions)
                    .orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.NO_ALLOWED_CALENDAR_DEVICE_MESSAGE));
            DeviceMessage deviceMessage = sendNewMessage(device, deviceMessageId, sendCalendarInfo, calendar, deviceMessageSpecificationService);
            device.calendars().setPassive(calendar, sendCalendarInfo.activationDate, deviceMessage);
            ConnectionStrategy connectionStrategy;
            ComTaskExecution cteFromEnablement = comTaskExecutionOptional.get();
            if (cteFromEnablement.getConnectionTask().isPresent()) {
                connectionStrategy = ((ScheduledConnectionTask) cteFromEnablement.getConnectionTask().get()).getConnectionStrategy();
                if (cteFromEnablement.getConnectionTask().get().isActive() && (!timeOfUseCampaign.getCalendarUploadConnectionStrategy()
                        .isPresent() || connectionStrategy == timeOfUseCampaign.getCalendarUploadConnectionStrategy().get())) {
                    cteFromEnablement.schedule(timeOfUseCampaign.getUploadPeriodStart());
                    timeOfUseItemDomainExtension.setDeviceMessage(deviceMessage);
                    serviceCall.update(timeOfUseItemDomainExtension);
                    if (serviceCall.canTransitionTo(DefaultState.PENDING)) {
                        serviceCall.requestTransition(DefaultState.PENDING);
                    }
                } else {
                    serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT)
                            .format(thesaurus.getFormat(TranslationKeys.valueOf(timeOfUseCampaign.getCalendarUploadConnectionStrategy().get().name())).format(), cteFromEnablement.getComTask()
                                    .getName()));
                    if (serviceCall.canTransitionTo(DefaultState.REJECTED)) {
                        serviceCall.requestTransition(DefaultState.REJECTED);
                    }
                }
            } else {
                serviceCall.log(LogLevel.WARNING, thesaurus.getSimpleFormat(MessageSeeds.CONNECTION_METHOD_MISSING_ON_COMTASK)
                        .format(cteFromEnablement.getComTask().getName()));
                if (serviceCall.canTransitionTo(DefaultState.REJECTED)) {
                    serviceCall.requestTransition(DefaultState.REJECTED);
                }
            }
        } else {
            serviceCall.log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.TASK_FOR_SENDING_CALENDAR_IS_MISSING).format());
            if (serviceCall.canTransitionTo(DefaultState.REJECTED)) {
                serviceCall.requestTransition(DefaultState.REJECTED);
            }
        }
    }

    private Set<ProtocolSupportedCalendarOptions> getAllowedTimeOfUseOptions(Device device, DeviceConfigurationService deviceConfigurationService) {
        Optional<TimeOfUseOptions> timeOfUseOptions = deviceConfigurationService.findTimeOfUseOptions(device.getDeviceConfiguration().getDeviceType());
        return timeOfUseOptions.map(TimeOfUseOptions::getOptions).orElse(Collections.emptySet());
    }

    private Optional<DeviceMessageId> getDeviceMessageId(SendCalendarInfo sendCalendarInfo, Set<ProtocolSupportedCalendarOptions> allowedOptions) {
        CalendarUpdateOption calendarUpdateOption = CalendarUpdateOption.find(sendCalendarInfo.calendarUpdateOption);
        DeviceMessageId deviceMessageId;
        // Special days does not support activation date
        boolean hasActivationDate = !CalendarUpdateOption.SPECIAL_DAYS.equals(calendarUpdateOption) && sendCalendarInfo.activationDate != null;
        boolean hasType = sendCalendarInfo.type != null;
        boolean hasContract = sendCalendarInfo.contract != null;
        // The UI only lets choose between CalendarUpdateOption.FULL_CALENDAR &&  CalendarUpdateOption.SPECIAL_DAYS
        boolean hasActivityCalendarOption = CalendarUpdateOption.FULL_CALENDAR.equals(calendarUpdateOption);
        boolean hasSpecialDaysCalendarOption = CalendarUpdateOption.SPECIAL_DAYS.equals(calendarUpdateOption);
        boolean sendCalendarWithDateAllowed = allowedOptions.contains(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE);
        boolean sendCalendarWithDateTimeAllowed = allowedOptions.contains(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);

        int index = 0;

        for (boolean flag : new boolean[]{hasActivationDate, hasType, hasContract}) {
            index <<= 1;
            index |= flag ? 1 : 0;
        }

        Supplier<DeviceMessageId>[] options = new Supplier[8];
        options[0b000] = () -> hasActivityCalendarOption ? ACTIVITY_CALENDER_FULL_CALENDAR_SEND : (hasSpecialDaysCalendarOption ? ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND : null);
        options[0b001] = () -> hasActivityCalendarOption ? ACTIVITY_CALENDER_FULL_CALENDAR_SEND : (hasSpecialDaysCalendarOption ? ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND : null);
        options[0b010] = () -> ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE;
        options[0b011] = () -> ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE;
        options[0b100] = () -> sendCalendarWithDateAllowed ? ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATE : (sendCalendarWithDateTimeAllowed ? ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME : null);
        options[0b101] = () -> hasActivityCalendarOption ? ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_CONTRACT : (hasSpecialDaysCalendarOption ? ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME : null);
        options[0b110] = () -> ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE;
        options[0b111] = () -> ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE;

        deviceMessageId = options[index].get();

        return checkIfDeviceMessageIsAllowed(deviceMessageId, allowedOptions) ? Optional.of(deviceMessageId) : Optional.empty();
    }

    private boolean checkIfDeviceMessageIsAllowed(DeviceMessageId deviceMessageId, Set<ProtocolSupportedCalendarOptions> allowedOptions) {
        Map<DeviceMessageId, ProtocolSupportedCalendarOptions> messageId2Option = new HashMap<>();
        messageId2Option.put(ACTIVITY_CALENDER_FULL_CALENDAR_SEND, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        messageId2Option.put(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATE, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE);
        messageId2Option.put(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE);
        messageId2Option.put(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_CONTRACT, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT);
        messageId2Option.put(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        messageId2Option.put(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND, ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR);
        messageId2Option.put(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE, ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_TYPE);
        messageId2Option.put(ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME, ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_CONTRACT_AND_DATE);

        return allowedOptions.contains(messageId2Option.get(deviceMessageId));

    }

    private DeviceMessage sendNewMessage(Device device, DeviceMessageId deviceMessageId, SendCalendarInfo sendCalendarInfo, AllowedCalendar calendar,
                                         DeviceMessageSpecificationService deviceMessageSpecificationService) {
        Device.DeviceMessageBuilder messageBuilder = device.newDeviceMessage(deviceMessageId);
        DeviceMessageSpec deviceMessageSpec = deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue()).get();

        //Find the message attribute of type 'reference' (to a DeviceMessageFile or Calendar). This is the 'activityCalendar' attribute.
        Optional<PropertySpec> calendarPropertySpec = deviceMessageSpec
                .getPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getValueFactory().isReference())
                .findAny();

        calendarPropertySpec.ifPresent(propertySpec -> messageBuilder.addProperty(propertySpec.getName(), calendar.getCalendar().get()));

        if (!isSpecialDays(deviceMessageId)) {
            //Find the message attribute of type 'String' without any possible values. This is the 'activityCalendarName' attribute.
            Optional<PropertySpec> activityCalendarNamePropertySpec = deviceMessageSpec
                    .getPropertySpecs()
                    .stream()
                    .filter(propertySpec -> (propertySpec.getValueFactory().getValueType().equals(String.class) && ((propertySpec.getPossibleValues() == null) || propertySpec.getPossibleValues()
                            .getAllValues()
                            .isEmpty())))
                    .findAny();

            activityCalendarNamePropertySpec.ifPresent(propertySpec -> messageBuilder.addProperty(propertySpec.getName(), calendar.getName()));
        }

        if (sendCalendarInfo.releaseDate != null) {
            messageBuilder.setReleaseDate(sendCalendarInfo.releaseDate);
        }
        if (needsActivationDate(deviceMessageId)) {
            //Find the message attribute of type 'Date'. This is the 'activityCalendarActivationDate' attribute.
            Optional<PropertySpec> activityCalendarActivationDatePropertySpec = deviceMessageSpec
                    .getPropertySpecs()
                    .stream()
                    .filter(propertySpec -> (propertySpec.getValueFactory().getValueType().equals(Date.class)))
                    .findAny();


            if (activityCalendarActivationDatePropertySpec.isPresent()) {
                Date date = Date.from(sendCalendarInfo.activationDate);
                messageBuilder.addProperty(activityCalendarActivationDatePropertySpec.get().getName(), date);
            }
        }

        if (needsType(deviceMessageId)) {
            //Find the message attribute of type 'String' with possible values. This is the 'activityCalendarType' attribute.
            Optional<PropertySpec> activityCalendarTypePropertySpec = deviceMessageSpec
                    .getPropertySpecs()
                    .stream()
                    .filter(propertySpec -> (propertySpec.getValueFactory().getValueType().equals(String.class) && ((propertySpec.getPossibleValues() != null) && !propertySpec.getPossibleValues()
                            .getAllValues()
                            .isEmpty())))
                    .findAny();

            activityCalendarTypePropertySpec.ifPresent(propertySpec -> messageBuilder.addProperty(propertySpec.getName(), sendCalendarInfo.type));
        }

        if (needsContract(deviceMessageId)) {

            //Find the message attribute of type 'BigDecimal'. This is the 'contract' attribute.
            Optional<PropertySpec> contractPropertySpec = deviceMessageSpec
                    .getPropertySpecs()
                    .stream()
                    .filter(propertySpec -> (propertySpec.getValueFactory().getValueType().equals(BigDecimal.class)))
                    .findAny();

            contractPropertySpec.ifPresent(propertySpec -> messageBuilder.addProperty(propertySpec.getName(), sendCalendarInfo.contract));
        }

        return messageBuilder.add();
    }

    private boolean needsActivationDate(DeviceMessageId deviceMessageId) {
        return deviceMessageId.equals(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATE)
                || deviceMessageId.equals(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME)
                || deviceMessageId.equals(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_CONTRACT)
                || deviceMessageId.equals(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME);
    }

    private boolean needsType(DeviceMessageId deviceMessageId) {
        return deviceMessageId.equals(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE)
                || deviceMessageId.equals(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE);
    }

    private boolean needsContract(DeviceMessageId deviceMessageId) {
        return deviceMessageId.equals(ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_CONTRACT)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME);
    }

    private boolean isSpecialDays(DeviceMessageId deviceMessageId) {
        return deviceMessageId.equals(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND)
                || deviceMessageId.equals(ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME)
                || deviceMessageId.equals(ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE);
    }

    public enum CalendarUpdateOption {
        FULL_CALENDAR("fullCalendar"),
        SPECIAL_DAYS("specialDays"),
        ACTIVITY_CALENDAR("activityCalendar");

        private String key;

        CalendarUpdateOption(String key) {
            this.key = key;
        }

        public static CalendarUpdateOption find(String key) {
            return Arrays.stream(values()).filter(o -> o.key.equals(key)).findFirst().orElse(null);
        }
    }

    private class SendCalendarInfo {
        public long allowedCalendarId;
        public Instant releaseDate;
        public Instant activationDate;
        public String type;
        public BigDecimal contract;
        public String calendarUpdateOption;
    }
}
