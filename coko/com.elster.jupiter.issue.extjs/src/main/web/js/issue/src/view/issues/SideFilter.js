Ext.define('Isu.view.issues.SideFilter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issues-side-filter',
    cls: 'filter-form',
    width: 200,
    title: Uni.I18n.translate('general.title.filter', 'ISU', 'Filter'),
    ui: 'filter',
    requires: [
        'Isu.view.component.AssigneeCombo',
        'Uni.view.form.CheckboxGroup',
        'Uni.component.filter.view.Filter'
    ],
    defaults: {
        labelAlign: 'top'
    },

    items: [
        {
            itemId: 'filter-by-status',
            xtype: 'checkboxstore',
            store: 'Isu.store.IssueStatuses',
            name: 'status',
            fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
            columns: 1,
            vertical: true
        },
        {
            itemId: 'filter-by-assignee',
            xtype: 'issues-assignee-combo',
            name: 'assignee',
            fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
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

            displayField: 'name',
            valueField: 'id',
            forceSelection: true,
            store: 'Isu.store.IssueReasons',

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

            displayField: 'name',
            valueField: 'name',
            forceSelection: true,
            store: 'Isu.store.Devices',

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
    ],

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [
                {
                    itemId: 'issues-filter-apply',
                    ui: 'action',
                    text: Uni.I18n.translate('general.apply', 'ISU', 'Apply'),
                    action: 'applyFilter'
                },
                {
                    itemId: 'issues-filter-reset',
                    text: Uni.I18n.translate('general.clearAll', 'ISU', 'Clear all'),
                    action: 'resetFilter'
                }
            ]
        }
    ]
});