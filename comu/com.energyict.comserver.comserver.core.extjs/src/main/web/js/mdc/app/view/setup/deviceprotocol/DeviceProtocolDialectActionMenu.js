Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-protocol-dialect-action-menu',
    plain: true,
    border: false,
    itemId: 'device-protocol-dialect-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'editDeviceProtocolDialect',
            action: 'editDeviceProtocolDialect'

        }
    ]
});
