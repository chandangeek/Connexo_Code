Ext.define('Mdc.view.setup.logbooktype.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.logbook-action-menu',
    plain: true,
    border: false,
    itemId: 'actionMenu',
    shadow: false,
    items: [
        {
            itemId: 'edit',
            text: 'Edit',
            action: 'edit'
        },
        {
            itemId: 'delete',
            text: 'Delete',
            action: 'delete'
        }
    ]
});
