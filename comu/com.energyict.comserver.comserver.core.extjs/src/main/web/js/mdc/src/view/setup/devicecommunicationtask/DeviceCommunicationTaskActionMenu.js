/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-communication-task-action-menu',
    itemId: 'device-communication-task-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('deviceCommunicationTask.runComTask', 'MDC', 'Run'),
                privileges: Mdc.privileges.Device.operateDeviceCommunication,
                itemId: 'runDeviceComTask',
                action: 'runDeviceComTask',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions,
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('deviceCommunicationTask.runComTaskNow', 'MDC', 'Run now'),
                privileges: Mdc.privileges.Device.operateDeviceCommunication,
                itemId: 'runDeviceComTaskNow',
                action: 'runDeviceComTaskNow',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions,
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('deviceCommunicationTask.runComTaskNowWithPriority', 'MDC', 'Run with priority'),
                privileges: Mdc.privileges.Device.runWithPriority,
                itemId: 'runDeviceComTaskNowWithPriority',
                action: 'runDeviceComTaskNowWithPriority',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions,
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('deviceCommunicationTask.changeConnectionMethod', 'MDC', 'Change connection method'),
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                itemId: 'changeConnectionMethodOfDeviceComTask',
                action: 'changeConnectionMethodOfDeviceComTask',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('deviceCommunicationTask.changeUrgency', 'MDC', 'Change urgency'),
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                itemId: 'changeUrgencyOfDeviceComTask',
                action: 'changeUrgencyOfDeviceComTask',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('deviceCommunicationTask.activateTracing', 'MDC', 'Activate tracing'),
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                itemId: 'activateTracingOfDeviceComTask',
                action: 'activateTracing',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions,
                order: -1,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('deviceCommunicationTask.deactivateTracing', 'MDC', 'Deactivate tracing'),
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                itemId: 'deactivateTracingOfDeviceComTask',
                action: 'deactivateTracing',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions,
                order: -1,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('deviceCommunicationTask.activateComTask', 'MDC', 'Activate'),
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                itemId: 'activateDeviceComTask',
                action: 'activateDeviceComTask',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions,
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('deviceCommunicationTask.deactivateComTask', 'MDC', 'Deactivate'),
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                itemId: 'deactivateDeviceComTask',
                action: 'deactivateDeviceComTask',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions,
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('deviceCommunicationTask.viewHistory', 'MDC', 'View history'),
                itemId: 'viewHistoryOfDeviceComTask',
                action: 'viewHistoryOfDeviceComTask',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});