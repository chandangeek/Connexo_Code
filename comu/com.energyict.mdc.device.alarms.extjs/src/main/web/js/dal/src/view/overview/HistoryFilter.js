Ext.define('Dal.view.overview.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.view-history-filter',

    requires: [
        'Dal.store.AlarmReasons'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                itemId: 'cbo-history-filter-reasons',
                dataIndex: 'reason',
                emptyText: Uni.I18n.translate('general.reason', 'DAL', 'Reason'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Dal.store.AlarmReasons'
            }
        ];

        me.callParent(arguments);
    }
});