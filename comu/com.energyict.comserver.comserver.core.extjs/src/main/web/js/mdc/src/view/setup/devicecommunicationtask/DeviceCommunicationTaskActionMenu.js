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
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceCommunication'),
            itemId: 'changeConnectionMethodOfDeviceComTask',
            action: 'changeConnectionMethodOfDeviceComTask'

        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.changeProtocolDialect', 'MDC', 'Change protocol dialect'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceCommunication'),
            itemId: 'changeProtocolDialectOfDeviceComTask',
            action: 'changeProtocolDialectOfDeviceComTask'

        },
        {
            text: Uni.I18n.translate('deviceCommunicationTask.changeUrgency', 'MDC', 'Change urgency'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceCommunication'),
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