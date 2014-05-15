Ext.define('Isu.view.workspace.issues.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issue-action-menu',
    plain: true,
    border: false,
    shadow: false,
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
        },
        {
            text: 'Notify user',
            action: 'notify'
        },
        {
            text: 'Send to inspect',
            action: 'send'
        }
    ]
});