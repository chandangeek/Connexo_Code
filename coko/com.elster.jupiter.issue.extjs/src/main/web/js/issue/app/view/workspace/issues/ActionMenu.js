Ext.define('Isu.view.workspace.issues.ActionMenu', {
    itemId: 'ActionMenu',
    extend: 'Ext.menu.Menu',
    alias: 'widget.issue-action-menu',
    plain: true,
    items: [
        {   itemId: 'assign',
            text: 'Assign',
            action: 'assign'
        },
        {
            itemId: 'close',
            text: 'Close',
            action: 'close'
        },
        {
            itemId: 'addcomment',
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