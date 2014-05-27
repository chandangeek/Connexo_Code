Ext.define('Mdc.view.setup.comtasks.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.communication-tasks-action-menu',
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