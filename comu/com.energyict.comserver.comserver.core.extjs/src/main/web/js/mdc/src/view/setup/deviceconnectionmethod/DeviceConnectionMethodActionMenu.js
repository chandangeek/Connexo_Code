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
            itemId: 'editDeviceConnectionMethod',
            action: 'editDeviceConnectionMethod'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'deleteDeviceConnectionMethod',
            action: 'deleteDeviceConnectionMethod'
        },
        {
            text: Uni.I18n.translate('deviceconnectionmethod.setAsDefault', 'MDC', 'Set as default'),
            itemId: 'toggleDefaultMenuItem',
            action: 'toggleDefault'
        },
        {
            text: Uni.I18n.translate('deviceconnectionmethod.activate', 'MDC', 'Activate'),
            itemId: 'toggleActiveMenuItem',
            action: 'toggleActive'
        }
    ]
});

