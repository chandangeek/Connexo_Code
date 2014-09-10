Ext.define('Isu.view.workspace.issues.TopPanel', {
    extend: 'Ext.container.Container',
    requires: [
        'Isu.view.workspace.issues.FilteringToolbar',
        'Isu.view.workspace.issues.GroupingToolbar',
        'Isu.view.workspace.issues.SortingToolbar',
        'Isu.view.workspace.issues.IssueGroup',
        'Ext.menu.Separator'
    ],
    alias: 'widget.issues-top-panel',
    items: [
        {
            xtype: 'filtering-toolbar',
            itemId: 'filtering-toolbar'
        },
        {
            xtype: 'menuseparator'
        },
        {
            xtype: 'grouping-toolbar',
            itemId: 'grouping-toolbar'
        },
        {
            xtype: 'menuseparator'
        },
        {
            xtype: 'sorting-toolbar',
            itemId: 'sorting-toolbar'
        },
        {
            xtype: 'issue-group',
            itemId: 'issue-group'
        }
    ]
});