Ext.define('Mdc.view.setup.comtasks.ComtaskActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comtaskActionMenu',
    plain: true,
    items: [
        {
            text: 'Edit',
            action: 'edit'
        },
        {
            text: 'Remove',
            action: 'delete'
        }
    ]
});