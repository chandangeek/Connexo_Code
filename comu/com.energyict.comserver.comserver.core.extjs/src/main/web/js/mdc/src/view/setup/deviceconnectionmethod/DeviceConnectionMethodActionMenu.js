Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-connection-method-action-menu',
    plain: true,
    border: false,
    itemId: 'device-connection-method-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'editDeviceConnectionMethod',
            action: 'editDeviceConnectionMethod',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'deleteDeviceConnectionMethod',
            action: 'deleteDeviceConnectionMethod',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions
        },
        {
            text: Uni.I18n.translate('deviceconnectionmethod.setAsDefault', 'MDC', 'Set as default'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'toggleDefaultMenuItem',
            action: 'toggleDefault',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions
        },
        {
            text: Uni.I18n.translate('deviceconnectionmethod.activate', 'MDC', 'Activate'),
            privileges: Mdc.privileges.Device.operateDeviceCommunication,
            itemId: 'toggleActiveMenuItem',
            action: 'toggleActive',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions
        },
        {
            text: Uni.I18n.translate('deviceconnectionmethod.viewHistory', 'MDC', 'View history'),
            itemId: 'viewConnectionHistoryItem',
            action: 'viewConnectionHistory'
        }
    ]
});

