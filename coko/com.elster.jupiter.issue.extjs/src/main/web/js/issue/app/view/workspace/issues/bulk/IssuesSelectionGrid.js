Ext.define('Isu.view.workspace.issues.bulk.IssuesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'issues-selection-grid',

    store: 'Isu.store.Issues',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'setup.searchitems.bulk.SchedulesSelectionGrid.counterText',
            count,
            'MDC',
            '{0} communication schedules selected'
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
                itemId: 'Title',
                header: 'Title',
                xtype: 'templatecolumn',
                tpl: '{reason.name}<tpl if="device"> to {device.serialNumber}</tpl>',
                flex: 2
            },
            {
                itemId: 'dueDate',
                header: 'Due date',
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                itemId: 'status',
                header: 'Status',
                xtype: 'templatecolumn',
                tpl: '<tpl if="status">{status.name}</tpl>',
                width: 100
            },
            {
                itemId: 'assignee',
                header: 'Assignee',
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type} isu-assignee-type-icon"></span></tpl> {assignee.name}',
                flex: 1
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
        this.getBottomToolbar().setVisible(false);
    }
});