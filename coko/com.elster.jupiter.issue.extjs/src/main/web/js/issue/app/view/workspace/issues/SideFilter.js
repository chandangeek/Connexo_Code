Ext.define('Isu.view.workspace.issues.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-side-filter',
    title: 'Filter',
    cls: 'filter-form',
    width: 180,

    requires: [
        'Isu.view.workspace.issues.component.AssigneeCombo',
        'Isu.util.FilterCheckboxgroup',
        'Isu.component.filter.view.Filter'
    ],

    items: [
        {
            xtype: 'filter-form',
            items: [
                {
                    xtype: 'filter-checkboxgroup',
                    store: 'Isu.store.IssueStatus',
                    name: 'status',
                    fieldLabel: 'Status',
                    labelAlign : 'top',
                    columns: 1,
                    vertical: true
                },
                {
                    xtype: 'issues-assignee-combo',
                    name: 'assignee',
                    fieldLabel: 'Assignee',
                    labelAlign : 'top'
                },
                {
                    xtype: 'combobox',
                    name: 'reason',
                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
                    anyMatch: true,
                    store: 'Isu.store.IssueReason',
                    queryMode: 'local',
                    fieldLabel: 'Reason',
                    labelAlign : 'top',
                    listConfig: {
                        cls: 'isu-combo-color-list'
                    }
                }
            ]
        }
    ],

    buttons: [
        {
            text: 'Apply',
            action: 'filter'
        },
        {
            text: 'Clear all',
            action: 'reset'
        }
    ]
});