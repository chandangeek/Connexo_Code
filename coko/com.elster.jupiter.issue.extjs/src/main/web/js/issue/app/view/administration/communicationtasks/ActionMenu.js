Ext.define('Isu.view.administration.communicationtasks.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.communication-tasks-action-menu',
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