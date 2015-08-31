Ext.define('Isu.view.issues.SideFilter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issues-side-filter',
    cls: 'filter-form',
    width: 200,
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
            fieldLabel: Uni.I18n.translate('general.status', 'ISU', 'Status'),
            columns: 1,
            vertical: true
        },
        {
            itemId: 'filter-by-assignee',
            xtype: 'issues-assignee-combo',
            name: 'assignee',
            fieldLabel: Uni.I18n.translate('general.assignee', 'ISU', 'Assignee'),
            forceSelection: true,
            anyMatch: true,
            emptyText: Uni.I18n.translate('issues.selectAssignee','ISU','Start typing to select an assignee')

        },
        {
            itemId: 'filter-by-reason',
            xtype: 'combobox',
            name: 'reason',
            fieldLabel: Uni.I18n.translate('general.reason', 'ISU', 'Reason'),

            displayField: 'name',
            valueField: 'id',
            forceSelection: true,
            store: 'Isu.store.IssueReasons',

            listConfig: {
                cls: 'isu-combo-color-list',
                emptyText: Uni.I18n.translate('issues.noReasonFound','ISU','No reason found')
            },

            queryMode: 'remote',
            queryParam: 'like',
            queryDelay: 100,
            queryCaching: false,
            minChars: 0,

            anchor: '100%',
            emptyText: Uni.I18n.translate('issues.selectReason','ISU','Select a reason')
        },
        {
            itemId: 'filter-by-meter',
            xtype: 'combobox',
            name: 'meter',
            fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),

            displayField: 'name',
            valueField: 'name',
            forceSelection: true,
            store: 'Isu.store.Devices',

            listConfig: {
                cls: 'isu-combo-color-list',
                emptyText: Uni.I18n.translate('general.tooltip.meter', 'ISU', 'Start typing for a MRID')
            },

            queryMode: 'remote',
            queryParam: 'like',
            queryDelay: 100,
            queryCaching: false,
            minChars: 0,

            anchor: '100%',
            emptyText: Uni.I18n.translate('general.device.selectmrid', 'ISU', 'Select MRID of the device ...')
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