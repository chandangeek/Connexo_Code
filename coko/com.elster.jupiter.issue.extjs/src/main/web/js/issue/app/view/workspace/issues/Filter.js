Ext.define('Isu.view.workspace.issues.Filter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.menu.Separator',
        'Isu.view.workspace.issues.FilteringToolbar',
        'Isu.view.workspace.issues.SortingToolbar',
        'Isu.view.workspace.issues.GroupingToolbar',
        'Isu.view.workspace.issues.IssueGroup'
    ],
    alias: "widget.issues-filter",
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'filtering-toolbar'
        },
        {
            itemId: 'menuseparator',
            xtype: 'menuseparator'
        },
        {
            xtype: 'grouping-toolbar'
        },
        {
            itemId: 'menuseparator',
            xtype: 'menuseparator'
        },
        {
            xtype: 'sorting-toolbar'
        },
        {
            xtype: 'issue-group'
        }
    ]
});