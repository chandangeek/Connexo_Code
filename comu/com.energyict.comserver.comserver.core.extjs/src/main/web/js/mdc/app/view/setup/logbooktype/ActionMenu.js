Ext.define('Mdc.view.setup.logbooktype.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.logbook-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: 'Edit',
            action: 'edit'
        },
        {
            text: 'Delete',
            action: 'delete'
        }
    ]
});
