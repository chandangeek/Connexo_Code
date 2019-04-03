Ext.define('Itk.view.bulk.IssuesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'issues-selection-grid',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfIssuess.selected', count, 'ITK',
            'No issues selected', '{0} issue selected', '{0} issues selected'
        );
    },

    allLabel: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allLabel', 'ITK', 'All issues'),
    allDescription: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allDescription', 'ITK', 'Select all issues (related to filters on the issues screen)'),

    selectedLabel: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedLabel', 'ITK', 'Selected issues'),
    selectedDescription: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedDescription', 'ITK', 'Select issues in table'),

    cancelHref: '#/search',

    columns: {
        items: [
            {
                itemId: 'issues-grid-title',
                header: Uni.I18n.translate('general.title.issue', 'ITK', 'Issue'),
                dataIndex: 'title',
                flex: 2
            },
            {
                itemId: 'issues-grid-priority',
                header: Uni.I18n.translate('general.priority', 'ITK', 'Priority'),
                dataIndex: 'priority',
                flex: 1
            },
            {
                itemId: 'issues-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'ITK', 'Due date'),
                dataIndex: 'dueDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '';
                },
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.status', 'ITK', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'issues-grid-cleared',
                header: Uni.I18n.translate('general.cleared', 'ITK', 'Cleared'),
                dataIndex: 'cleared',
                flex: 1
            },
            {
                itemId: 'issues-grid-workgroup-assignee',
                header: Uni.I18n.translate('general.workgroup', 'ITK', 'Workgroup'),
                dataIndex: 'workGroupAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ITK', 'Unassigned');
                }
            },
            {
                itemId: 'issues-grid-user-assignee',
                header: Uni.I18n.translate('general.user', 'ITK', 'User'),
                dataIndex: 'userAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ITK', 'Unassigned');
                }
            }
        ]
    },


    initComponent: function () {
        this.callParent(arguments);
        this.getBottomToolbar().setVisible(false);
    }
});