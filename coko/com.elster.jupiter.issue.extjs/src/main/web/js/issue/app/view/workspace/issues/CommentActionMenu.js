Ext.define('Isu.view.workspace.issues.CommentActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.comment-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: 'Edit'
        },
        {
            text: 'Delete'
        }
    ]
});