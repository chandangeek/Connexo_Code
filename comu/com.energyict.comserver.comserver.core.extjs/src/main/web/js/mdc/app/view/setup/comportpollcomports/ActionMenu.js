Ext.define('Mdc.view.setup.comportpollcomports.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comPortPoolComPortsActionMenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'remove',
            text: 'Remove',
            action: 'remove'
        }
    ]
});
