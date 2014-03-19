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
                    labelAlign: 'top',
                    columns: 1,
                    vertical: true
                },
                {
                    xtype: 'issues-assignee-combo',
                    name: 'assignee',
                    fieldLabel: 'Assignee',
                    labelAlign: 'top',
                    forceSelection: true,
                    anyMatch: true
                },
                {
                    xtype: 'combobox',
                    name: 'reason',
                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
//                    anyMatch: true,
                    store: 'Isu.store.IssueReason',

                    fieldLabel: 'Reason',
                    labelAlign: 'top',
                    listConfig: {
                        cls: 'isu-combo-color-list'
                    },

                    queryMode: 'remote',
                    queryParam: 'like',
                    queryDelay: 100,
                    minChars: 1,

                    hideTrigger:true,
                    anchor: '100%',
                    emptyText: 'type something'
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