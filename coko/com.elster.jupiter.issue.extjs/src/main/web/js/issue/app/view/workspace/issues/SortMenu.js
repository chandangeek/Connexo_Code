Ext.define('Isu.view.workspace.issues.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issue-sort-menu',
    itemId: 'SortMenu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'sortEl',
            text: 'Due date',
            action: 'dueDate'
        }
    ]
});