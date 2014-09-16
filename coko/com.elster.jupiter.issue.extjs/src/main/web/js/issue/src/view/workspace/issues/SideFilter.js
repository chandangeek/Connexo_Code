Ext.define('Isu.view.workspace.issues.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-side-filter',
    cls: 'filter-form',
    width: 200,
    title: Uni.I18n.translate('general.title.filter', 'ISU', 'Filter'),
    ui: 'filter',
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
            itemId: 'filter-form',
            items: [
                {
                    itemId: 'filter-by-status',
                    xtype: 'filter-checkboxgroup',
                    store: 'Isu.store.IssueStatus',
                    name: 'status',
                    fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                    labelAlign: 'top',
                    columns: 1,
                    vertical: true
                },
                {
                    itemId: 'filter-by-assignee',
                    xtype: 'issues-assignee-combo',
                    name: 'assignee',
                    fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                    labelAlign: 'top',
                    forceSelection: true,
                    anyMatch: true,
                    emptyText: 'select an assignee',
                    tooltipText: 'Start typing for assignee'
                },
                {
                    itemId: 'filter-by-reason',
                    xtype: 'combobox',
                    name: 'reason',
                    fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
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
                    itemId: 'filter-by-meter',
                    xtype: 'combobox',
                    name: 'meter',
                    fieldLabel: Uni.I18n.translate('general.title.meter', 'ISU', 'Meter'),
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

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [
                {
                    itemId: 'fApply',
                    ui: 'action',
                    text: 'Apply',
                    action: 'filter'
                },
                {
                    itemId: 'fReset',
                    text: 'Clear all',
                    action: 'reset'
                }
            ]
        }
    ]
});