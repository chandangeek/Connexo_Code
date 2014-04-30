Ext.define('Isu.view.administration.communicationtasks.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.communication-tasks-sort-menu',
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