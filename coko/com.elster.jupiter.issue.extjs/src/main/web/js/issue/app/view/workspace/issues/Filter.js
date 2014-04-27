Ext.define('Isu.view.workspace.issues.Filter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Ext.form.Label',
        'Isu.view.workspace.issues.FilteringToolbar',
        'Isu.view.workspace.issues.SortingToolbar',
        'Isu.view.workspace.issues.GroupingToolbar',
        'Isu.view.workspace.issues.GroupGrid'
    ],
    alias: "widget.issues-filter",
    store: 'Isu.store.Issues',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        { xtype: 'filtering-toolbar' },
        { xtype: 'menuseparator' },
        { xtype: 'grouping-toolbar' },
//        { xtype: 'issue-group-grid' },
        { xtype: 'menuseparator' },
        { xtype: 'sorting-toolbar' }
    ]
});