/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.impl.ami.eventhandler.MeterReadingEventHandlerFactory;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementChangeMessageHandler;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementConnectionMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementPriorityMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementStatusMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.scheduling.SchedulingService;

public enum SubscriberTranslationKeys implements TranslationKey {

    DATA_COLLECTION_KPI_CALCULATOR(DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME),
    CONNECTION_TASK_VALIDATOR_AFTER_PROPERTY_REMOVAL(ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_SUBSCRIBER, ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_SUBSCRIBER_DISPLAY_NAME),
    COMSCHEDULE_RECALCULATOR(Installer.COMSCHEDULE_RECALCULATOR_MESSAGING_NAME, Installer.COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME),
    COMSCHEDULE_BACKGROUND_OBSOLETION(Installer.COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME, Installer.COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME),
    COMTASK_ENABLEMENT_CONNECTION(ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME),
    COMTASK_ENABLEMENT_PRIORITY(ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME),
    COMTASK_ENABLEMENT_STATUS(ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME),
    COMMUNICATION_RESCHEDULER(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_SUBSCRIBER, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DISPLAYNAME),
    COMMUNICATION_FILTER_ITEMIZER(CommunicationTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DISPLAYNAME),
    CONNECTION_RESCHEDULER(ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_SUBSCRIBER, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DISPLAY_NAME),
    CONNECTION_FILTER_ITEMIZER(ConnectionTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DISPLAYNAME),
    CONNECTION_PROPERTY_UPDATER(ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_SUBSCRIBER, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DISPLAY_NAME),
    CONNECTION_PROPERTY_FILTER_ITEMIZER(ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_SUBSCRIBER, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DISPLAY_NAME),
    COMSCHEDULE_FILTER_ITEMIZER(SchedulingService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER, SchedulingService.FILTER_ITEMIZER_QUEUE_DISPLAYNAME),
    COMSCHEDULE(SchedulingService.COM_SCHEDULER_QUEUE_SUBSCRIBER, SchedulingService.COM_SCHEDULER_QUEUE_DISPLAYNAME),
    CHANGE_DEVICE_CONFIGURATION(ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SUBSCRIBER, ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SUBSCRIBER_DISPLAY_NAME),
    COMTASK_ENABLEMENT(ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_SUBSCRIBER, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_SUBSCRIBER_DISPLAY_NAME),
    METER_READING_EVENT(MeterReadingEventHandlerFactory.SUBSCRIBER_NAME, MeterReadingEventHandlerFactory.SUBSCRIBER_DISPLAYNAME);

    private String key;
    private String defaultFormat;

    SubscriberTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}