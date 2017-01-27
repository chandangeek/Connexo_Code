Ext.define('Isu.view.issues.bulk.IssuesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'issues-selection-grid',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfIssues.selected', count, 'ISU',
            'No issues selected', '{0} issue selected', '{0} issues selected'
        );
    },

    allLabel: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allLabel', 'ISU', 'All issues'),
    allDescription: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allDescription', 'ISU', 'Select all issues (related to filters and grouping on the issues screen)'),

    selectedLabel: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedLabel', 'ISU', 'Selected issues'),
    selectedDescription: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedDescription', 'ISU', 'Select issues in table'),

    cancelHref: '#/search',

    columns: {
        items: [
            {
                itemId: 'issues-grid-id',
                header: Uni.I18n.translate('general.title.issueId', 'ISU', 'Id'),
                dataIndex: 'issueId'
            },
            {
                itemId: 'issues-grid-title',
                header: Uni.I18n.translate('general.title.issue', 'ISU', 'Issue'),
                dataIndex: 'title',
                flex: 2
            },
            {
                itemId: 'issues-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                dataIndex: 'dueDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '';
                },
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.status', 'ISU', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'issues-grid-workgroup-assignee',
                header: Uni.I18n.translate('general.workgroup', 'ISU', 'Workgroup'),
                dataIndex: 'workGroupAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ISU', 'Unassigned');
                }
            },
            {
                itemId: 'issues-grid-user-assignee',
                header: Uni.I18n.translate('general.user', 'ISU', 'User'),
                dataIndex: 'userAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ISU', 'Unassigned');
                }
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
        this.getBottomToolbar().setVisible(false);
    }
});