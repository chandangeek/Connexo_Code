Ext.define('Mdc.view.setup.device.DeviceActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-action-menu',
    plain: true,
    border: false,
    itemId: 'deviceActionMenu',
    shadow: false,
    items: [
        {
            itemId: 'activate',
            text: Uni.I18n.translate('validation.activate', 'CFG', 'Activate')
        },
        {
            itemId: 'deactivate',
            text: Uni.I18n.translate('validation.deactivate', 'CFG', 'Deactivate')
        }
    ]
});


