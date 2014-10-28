Ext.define('Idc.view.workspace.issues.bulk.IssuesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'issues-selection-grid',

    store: 'Idc.store.Issues',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'workspace.issues.bulk.IssuesSelectionGrid.counterText',
            count,
            'MDC',
            '{0} issues selected'
        );
    },

    allLabel: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allLabel', 'MDC', 'All issues'),
    allDescription: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.allDescription', 'MDC', 'Select all issues (related to filters and grouping on the issues screen)'),

    selectedLabel: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedLabel', 'MDC', 'Selected issues'),
    selectedDescription: Uni.I18n.translate('workspace.issues.bulk.IssuesSelectionGrid.selectedDescription', 'MDC', 'Select issues in table'),

    cancelHref: '#/search',

    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'issues-grid-title',
                header: Uni.I18n.translate('general.title.title', 'ISU', 'Title'),
                dataIndex: 'title',
                flex: 2
            },
            {
                itemId: 'issues-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'issues-grid-assignee',
                header: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee_type"><span class="isu-icon-{assignee_type} isu-assignee-type-icon"></span></tpl> {assignee_name}',
                flex: 1
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
        this.getBottomToolbar().setVisible(false);
    }
});