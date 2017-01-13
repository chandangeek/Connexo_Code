Ext.define('Mdc.view.setup.comservercomports.AddMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.comServerComPortsAddMenu',
    initComponent: function () {
        this.items = [
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
        ];
        this.callParent(arguments);
    }
});
