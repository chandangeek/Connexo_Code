Ext.define('Isu.view.issues.IssueFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'isu-view-issues-issuefilter',

    // TODO Integrate the filter on screens.
    store: undefined,

    filters: [
        {
            type: 'combobox',
            dataIndex: 'status',
            emptyText: Uni.I18n.translate('view.issues.issueFilter.status', 'ISU', 'Status'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Isu.store.IssueStatuses'
        },
        {
            type: 'combobox',
            dataIndex: 'assignee',
            emptyText: Uni.I18n.translate('view.issues.issueFilter.assignee', 'ISU', 'Type for assignees'),
            store: 'Isu.store.IssueAssignees',
            displayField: 'name',
            valueField: 'idx',
            anyMatch: true,
            queryMode: 'remote',
            queryParam: 'like',
            queryCaching: false,
            minChars: 0
        },
        {
            type: 'combobox',
            dataIndex: 'reason',
            emptyText: Uni.I18n.translate('view.issues.issueFilter.reason', 'ISU', 'Reason'),
            displayField: 'name',
            valueField: 'id',
            store: 'Isu.store.IssueReasons',
            queryMode: 'remote',
            queryParam: 'like',
            queryCaching: false,
            minChars: 0
        },
        {
            type: 'combobox',
            dataIndex: 'meter',
            emptyText: Uni.I18n.translate('view.issues.issueFilter.meter', 'ISU', 'Type to search by MRID'),
            displayField: 'name',
            valueField: 'id',
            store: 'Isu.store.Devices',
            queryMode: 'remote',
            queryParam: 'like',
            queryCaching: false,
            minChars: 0
        }
    ]
});