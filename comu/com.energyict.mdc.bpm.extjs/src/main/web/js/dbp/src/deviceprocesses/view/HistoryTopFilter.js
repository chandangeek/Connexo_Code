Ext.define('Dbp.deviceprocesses.view.HistoryTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dbp-view-history-processes-topfilter',
    store: 'Dbp.deviceprocesses.store.HistoryProcesses',
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'process',
                emptyText: Uni.I18n.translate('dbp.process.filter.process', 'DBP', 'Process'),
                multiSelect: true,
                displayField: 'id',
                valueField: 'id',
                width: 240,
                store: 'Dbp.deviceprocesses.store.HistoryProcessesFilterProcesses'
            },
            {
                type: 'interval',
                dataIndex: 'startedBetween',
                dataIndexFrom: 'startedOnFrom',
                dataIndexTo: 'startedOnTo',
                text: Uni.I18n.translate('dbp.process.startedBetween', 'DBP', 'Started between')
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('dbp.process.filter.status', 'DBP', 'Status'),
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Dbp.deviceprocesses.store.HistoryProcessesFilterStatuses'
            },
            {
                type: 'combobox',
                dataIndex: 'user',
                emptyText: Uni.I18n.translate('dbp.process.filter.startedBy', 'DBP', 'Started by'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'name',
                store: 'Dbp.deviceprocesses.store.HistoryProcessesFilterUsers'
            }
        ]
        me.callParent(arguments);
    }
});