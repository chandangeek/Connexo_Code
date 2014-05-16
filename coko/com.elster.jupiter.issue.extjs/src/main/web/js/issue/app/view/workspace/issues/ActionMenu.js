Ext.define('Isu.view.workspace.issues.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issue-action-menu',
    plain: true,
    items: [
        {
            text: 'Assign',
            action: 'assign'
        },
        {
            text: 'Close',
            action: 'close'
        },
        {
            text: 'Add comment',
            action: 'addcomment'
        }
    ]
});