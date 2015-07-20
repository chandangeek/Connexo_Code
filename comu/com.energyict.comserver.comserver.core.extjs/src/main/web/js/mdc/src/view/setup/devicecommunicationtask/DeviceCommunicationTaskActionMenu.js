Ext.define('Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-communication-task-action-menu',
    plain: true,
    border: false,
    itemId: 'device-communication-task-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('deviceCommunicationTask.runComTask', 'MDC', 'Run'),
            privileges: Mdc.privileges.Device.operateDeviceCommunication,
            itemId: 'runDeviceComTask',
            action: 'runDeviceComTask',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions
        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.runComTaskNow', 'MDC', 'Run now'),
            privileges: Mdc.privileges.Device.operateDeviceCommunication,
            itemId: 'runDeviceComTaskNow',
            action: 'runDeviceComTaskNow',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions
        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.changeConnectionMethod', 'MDC', 'Change connection method'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'changeConnectionMethodOfDeviceComTask',
            action: 'changeConnectionMethodOfDeviceComTask',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions
        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.changeProtocolDialect', 'MDC', 'Change protocol dialect'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'changeProtocolDialectOfDeviceComTask',
            action: 'changeProtocolDialectOfDeviceComTask',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions
        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.changeUrgency', 'MDC', 'Change urgency'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'changeUrgencyOfDeviceComTask',
            action: 'changeUrgencyOfDeviceComTask',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationTasksActions
        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.viewHistory', 'MDC', 'View history'),
            itemId: 'viewHistoryOfDeviceComTask',
            action: 'viewHistoryOfDeviceComTask'

        }
    ]
});