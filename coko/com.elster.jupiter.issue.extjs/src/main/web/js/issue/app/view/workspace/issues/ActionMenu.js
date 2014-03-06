Ext.define('Mtr.view.workspace.issues.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issue-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: 'Assign'
        },
        {
            text: 'Close'
        }
    ]
});