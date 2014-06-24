Ext.define('Mdc.view.setup.comportpool.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comportpool-actionmenu',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'edit',
            text: 'Edit',
            action: 'edit'
        },
        {
            itemId: 'remove',
            text: 'Remove',
            action: 'remove'
        }
    ]
});
