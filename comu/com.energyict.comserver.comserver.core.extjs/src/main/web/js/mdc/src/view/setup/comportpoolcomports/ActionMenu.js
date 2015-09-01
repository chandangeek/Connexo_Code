Ext.define('Mdc.view.setup.comportpoolcomports.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comPortPoolComPortsActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'remove',
            text: Uni.I18n.translate('general.remove','MDC','Remove'),
            action: 'remove'
        }
    ]
});
