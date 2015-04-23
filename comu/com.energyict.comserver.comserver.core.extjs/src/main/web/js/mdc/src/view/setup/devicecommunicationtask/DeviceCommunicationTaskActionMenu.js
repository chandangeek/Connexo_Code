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
            hidden: Uni.Auth.hasNoPrivilege('privilege.operate.deviceCommunication'),
            itemId: 'runDeviceComTask',
            action: 'runDeviceComTask'

        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.runComTaskNow', 'MDC', 'Run now'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.operate.deviceCommunication'),
            itemId: 'runDeviceComTaskNow',
            action: 'runDeviceComTaskNow'

        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.changeConnectionMethod', 'MDC', 'Change connection method'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'changeConnectionMethodOfDeviceComTask',
            action: 'changeConnectionMethodOfDeviceComTask'

        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.changeProtocolDialect', 'MDC', 'Change protocol dialect'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'changeProtocolDialectOfDeviceComTask',
            action: 'changeProtocolDialectOfDeviceComTask'

        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.changeUrgency', 'MDC', 'Change urgency'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'changeUrgencyOfDeviceComTask',
            action: 'changeUrgencyOfDeviceComTask'

        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.viewHistory', 'MDC', 'View history'),
            itemId: 'viewHistoryOfDeviceComTask',
            action: 'viewHistoryOfDeviceComTask'

        }
    ]
});