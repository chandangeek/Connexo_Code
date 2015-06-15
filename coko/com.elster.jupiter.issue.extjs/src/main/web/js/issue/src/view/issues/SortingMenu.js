Ext.define('Isu.view.issues.SortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issues-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'issues-sorting-menu-item-by-due-date',
            text: 'Due date',
            action: 'dueDate'
        },
        {
            itemId: 'issues-sorting-menu-item-by-modification-date',
            text: 'Modification date',
            action: 'modTime'
        }
    ]
});