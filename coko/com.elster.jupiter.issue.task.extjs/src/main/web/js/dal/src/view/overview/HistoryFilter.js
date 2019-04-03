Ext.define('Itk.view.overview.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.view-history-filter',

    requires: [
        'Itk.store.IssueReasons'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                itemId: 'cbo-history-filter-reasons',
                dataIndex: 'reason',
                emptyText: Uni.I18n.translate('general.reason', 'ITK', 'Reason'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Itk.store.IssueReasons'
            }
        ];

        me.callParent(arguments);
    }
});