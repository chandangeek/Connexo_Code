Ext.define('Cfg.view.validationtask.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'cfg-view-validationtask-historyfilter',

    store: 'Cfg.store.ValidationTasksHistory',

    filters: [
        {
            type: 'interval',
            dataIndex: 'startedBetween',
            dataIndexFrom: 'startedOnFrom',
            dataIndexTo: 'startedOnTo',
            text: Uni.I18n.translate('validationtask.historyFilter.startedBetween', 'CFG', 'Started between')
        },
        {
            type: 'interval',
            dataIndex: 'finishedBetween',
            dataIndexFrom: 'finishedOnFrom',
            dataIndexTo: 'finishedOnTo',
            text: Uni.I18n.translate('validationtask.historyFilter.finishedBetween', 'CFG', 'Finished between')
        }
    ]
});