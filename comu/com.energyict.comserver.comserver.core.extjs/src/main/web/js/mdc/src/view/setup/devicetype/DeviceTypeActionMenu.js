Ext.define('Mdc.view.setup.devicetype.DeviceTypeActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-type-action-menu',
    plain: true,
    border: false,
    itemId: 'device-type-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editDeviceType',
            itemId: 'editDeviceType'
        },
        {
            text: Uni.I18n.translate('deviceLifeCycle.change', 'MDC', 'Change device life cycle'),
            action: 'changeDeviceLifeCycle',
            itemId: 'change-device-life-cycle'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'deleteDeviceType',
            itemId: 'deleteDeviceType'
        }
    ]
});
