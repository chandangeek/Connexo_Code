Ext.define('Dxp.view.tasks.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dxp-view-tasks-historyfilter',

    store: 'Dxp.store.DataExportTasksHistory',

    filters: [
        {
            type: 'interval',
            dataIndex: 'startedBetween',
            dataIndexFrom: 'startedOnFrom',
            dataIndexTo: 'startedOnTo',
            text: Uni.I18n.translate('tasks.historyFilter.startedBetween', 'DES', 'Started between')
        },
        {
            type: 'interval',
            dataIndex: 'finishedBetween',
            dataIndexFrom: 'finishedOnFrom',
            dataIndexTo: 'finishedOnTo',
            text: Uni.I18n.translate('tasks.historyFilter.finishedBetween', 'DES', 'Finished between')
        },
        {
            type: 'combobox',
            dataIndex: 'status',
            emptyText: Uni.I18n.translate('general.status', 'DES', 'Status'),
            multiSelect: true,
            displayField: 'display',
            valueField: 'value',
            store: 'Dxp.store.Status'
        }
    ]
});