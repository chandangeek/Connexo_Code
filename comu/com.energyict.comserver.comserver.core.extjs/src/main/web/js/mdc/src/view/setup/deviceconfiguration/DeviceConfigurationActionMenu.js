Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-configuration-action-menu',
    plain: true,
    border: false,
    itemId: 'device-configuration-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
            itemId: 'activateDeviceconfigurationMenuItem',
            action: 'activateDeactivateDeviceConfiguration'
        },
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editDeviceConfiguration'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'deleteDeviceConfiguration'
        }
    ]
});
