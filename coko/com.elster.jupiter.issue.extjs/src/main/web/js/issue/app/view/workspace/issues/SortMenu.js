Ext.define('Isu.view.workspace.issues.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issue-sort-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            text: 'Due date',
            action: 'dueDate'
        }
    ]
});