Ext.define('Isu.view.workspace.issues.GroupingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Isu.store.IssueGrouping'
    ],
    alias: 'widget.grouping-toolbar',
    title: 'Group',
    name: 'group',
    showClearButton: false,
    content: {
        itemId: 'group_combobox',
        xtype: 'combobox',
        name: 'groupingcombo',
        store: 'Isu.store.IssueGrouping',
        editable: false,
        emptyText: 'None',
        queryMode: 'local',
        displayField: 'display',
        valueField: 'value',
        labelAlign: 'left'
    }
});
