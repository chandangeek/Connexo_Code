Ext.define('Isu.view.creationrules.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.creation-rule-action-menu',
    plain: true,
    border: false,
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
