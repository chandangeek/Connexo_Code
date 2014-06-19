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
            text: 'default',
            itemId: 'toggleDefaultMenuItem',
            action: 'toggleDefault'
        }
    ]
});

