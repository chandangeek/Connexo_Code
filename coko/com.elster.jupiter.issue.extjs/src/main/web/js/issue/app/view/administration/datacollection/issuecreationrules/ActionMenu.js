Ext.define('Isu.view.administration.datacollection.issuecreationrules.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.creation-rule-action-menu',
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
