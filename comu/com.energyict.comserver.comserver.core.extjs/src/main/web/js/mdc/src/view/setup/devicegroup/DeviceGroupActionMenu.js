Ext.define('Mdc.view.setup.devicegroup.DeviceGroupActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-group-action-menu',
    plain: true,
    border: false,
    itemId: 'device-group-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'remove-device-group',
            action: 'deleteDeviceGroup'
        },
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editDeviceGroup',
            itemId: 'edit-device-group'
        }
    ]
});
