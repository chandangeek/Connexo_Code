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
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceCommunication'),
            itemId: 'editDeviceConnectionMethod',
            action: 'editDeviceConnectionMethod'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceCommunication'),
            itemId: 'deleteDeviceConnectionMethod',
            action: 'deleteDeviceConnectionMethod'
        },
        {
            text: Uni.I18n.translate('deviceconnectionmethod.setAsDefault', 'MDC', 'Set as default'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.operate.deviceCommunication'),
            itemId: 'toggleDefaultMenuItem',
            action: 'toggleDefault'
        },
        {
            text: Uni.I18n.translate('deviceconnectionmethod.activate', 'MDC', 'Activate'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.operate.deviceCommunication'),
            itemId: 'toggleActiveMenuItem',
            action: 'toggleActive'
        },
        {
            text: Uni.I18n.translate('deviceconnectionmethod.viewHistory', 'MDC', 'View history'),
            itemId: 'viewConnectionHistoryItem',
            action: 'viewConnectionHistory'
        }
    ]
});

