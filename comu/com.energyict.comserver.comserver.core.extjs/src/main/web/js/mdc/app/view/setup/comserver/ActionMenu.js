Ext.define('Mdc.view.setup.comserver.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comserver-actionmenu',
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
            itemId: 'activate',
            text: 'Activate',
            action: 'activate'
        },
        {
            itemId: 'deactivate',
            text: 'Deactivate',
            action: 'deactivate'
        },
        {
            itemId: 'remove',
            text: 'Remove',
            action: 'remove'
        }
    ]
});
