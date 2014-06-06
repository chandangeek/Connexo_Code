Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-communication-protocol-action-menu',
    plain: true,
    border: false,
    itemId: 'device-communication-protocol-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'editProtocol',
            action: 'editProtocol'
        }
    ]
});
