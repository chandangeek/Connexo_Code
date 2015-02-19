Ext.define('Mdc.view.setup.comservercomports.AddMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comServerComPortsAddMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            text: Uni.I18n.translate('comServerComPorts.addMenu.inbound', 'MDC', 'Inbound'),
            action: 'addInbound',
            itemId: 'btn-add-inbound'
        },
        {
            text: Uni.I18n.translate('comServerComPorts.addMenu.outbound', 'MDC', 'Outbound'),
            action: 'addOutbound',
            itemId: 'bn-add-outbound'
        }
    ]
});
