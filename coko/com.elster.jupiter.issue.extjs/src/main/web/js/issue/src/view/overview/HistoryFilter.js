Ext.define('Isu.view.overview.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.view-issue-history-filter',

    requires: [
        'Isu.store.IssueReasons'
    ],

    initComponent: function () {
        var me = this;
        me.filters = [
            {
                type: 'combobox',
                itemId: 'issue-type-filter',
                dataIndex: 'issueType',
                emptyText: Uni.I18n.translate('general.type', 'ISU', 'Type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'uid',
                store: 'Isu.store.IssueTypes'
            },
            {
                type: 'combobox',
                itemId: 'cbo-history-filter-reasons',
                dataIndex: 'reason',
                emptyText: Uni.I18n.translate('general.reason', 'ISU', 'Reason'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Isu.store.IssueReasons'
            }
        ];
        me.callParent(arguments);
    }
});