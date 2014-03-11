Ext.define('Isu.view.workspace.datacollection.issueassignmentrules.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.rule-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: 'Enable'
        },
        {
            text: 'Disable'
        },
        {
            text: 'Edit'
        },
        {
            text: 'Delete'
        },
        {
            text: 'Move up'
        },
        {
            text: 'Move down'
        }
    ]
});