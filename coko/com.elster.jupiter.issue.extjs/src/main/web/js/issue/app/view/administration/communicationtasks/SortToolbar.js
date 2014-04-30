Ext.define('Isu.view.administration.communicationtasks.SortToolbar', {
    extend: 'Skyline.panel.FilterToolbar',
    requires: [
        'Skyline.button.SortItemButton',
        'Isu.view.administration.communicationtasks.SortMenu'
    ],
    alias: 'widget.communication-tasks-sort-toolbar',
    title: 'Sort',
    name: 'sortitemspanel',
    emptyText: 'None',
    tools: [
        {
            xtype: 'button',
            action: 'addSort',
            text: 'Add sort',
            menu: {
                xtype: 'communication-tasks-sort-menu'
            }
        }
    ]
});