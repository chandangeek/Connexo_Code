Ext.define('Isu.view.workspace.issues.GroupingToolbar', {
    extend: 'Skyline.panel.FilterToolbar',
    alias: 'widget.grouping-toolbar',
    title: 'Group',
    name: 'group',
    showClearButton: false,
    content: {
        xtype: 'combobox',
        name: 'groupnames',
        editable: false,
        emptyText: 'None',
        queryMode: 'local',
        displayField: 'display',
        valueField: 'value',
        labelAlign: 'left'
    }
});
