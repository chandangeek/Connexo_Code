Ext.define('Isu.view.workspace.issues.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-side-filter',
    itemId: 'issues-side-filter',
    cls: 'filter-form',
    width: 200,
    title: "Filter",
    ui: "filter",
    requires: [
        'Isu.view.workspace.issues.component.AssigneeCombo',
        'Isu.util.FilterCheckboxgroup',
        'Uni.component.filter.view.Filter',
        'Isu.store.IssueStatus',
        'Isu.store.IssueReason'
    ],

    items: [
        {
            xtype: 'filter-form',

            items: [
                {
                    itemId: 'StatusFilter',
                    xtype: 'filter-checkboxgroup',
                    store: 'Isu.store.IssueStatus',
                    name: 'status',
                    fieldLabel: 'Status',
                    labelAlign: 'top',
                    columns: 1,
                    vertical: true
                },
                {
                    xtype: 'button',
                    ui: 'link',
                    text: 'Assigned to me',
                    action: 'assignedToMe'
                },
                {
                    itemId: 'AssigneeFilter',
                    xtype: 'issues-assignee-combo',
                    name: 'assignee',
                    fieldLabel: 'Assignee',
                    labelAlign: 'top',
                    forceSelection: true,
                    anyMatch: true,
                    emptyText: 'select an assignee',
                    tooltipText: 'Start typing for assignee'
                },
                {
                    itemId: 'ReasonFilter',
                    xtype: 'combobox',
                    name: 'reason',
                    fieldLabel: 'Reason',
                    labelAlign: 'top',

                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
                    store: 'Isu.store.IssueReason',

                    listConfig: {
                        cls: 'isu-combo-color-list',
                        emptyText: 'No reason found'
                    },

                    queryMode: 'remote',
                    queryParam: 'like',
                    queryDelay: 100,
                    queryCaching: false,
                    minChars: 1,

                    triggerAction: 'query',
                    anchor: '100%',
                    emptyText: 'select a reason',
                    tooltipText: 'Start typing for reason'
                },
                {
                    itemId: 'MeterFilter',
                    xtype: 'combobox',
                    name: 'meter',
                    fieldLabel: 'Meter',
                    labelAlign: 'top',

                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
                    store: 'Isu.store.IssueMeter',

                    listConfig: {
                        cls: 'isu-combo-color-list',
                        emptyText: 'No meter found'
                    },

                    queryMode: 'remote',
                    queryParam: 'like',
                    queryDelay: 100,
                    queryCaching: false,
                    minChars: 1,

                    triggerAction: 'query',
                    anchor: '100%',
                    emptyText: 'select a MRID of the meter',
                    tooltipText: 'Start typing for a MRID'
                }
            ]
        }
    ],

    buttons: [
        {
            itemId: 'fApply',
            text: 'Apply',
            action: 'filter'
        },
        {
            itemId: 'fReset',
            text: 'Clear all',
            action: 'reset'
        }
    ]
});