Ext.define('Isu.view.administration.datacollection.issuecreationrules.ActionMenu', {
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
