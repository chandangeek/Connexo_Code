Ext.define('Est.estimationtasks.view.TaskHistoryTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'est-tasks-view-taskhistorytopfilter',
    store: 'Est.estimationtasks.store.EstimationTasksHistory',
    filters: [
        {
            type: 'interval',
            dataIndex: 'started',
            dataIndexFrom: 'startedOnFrom',
            dataIndexTo: 'startedOnTo',
            text: Uni.I18n.translate('estimationtasks.started', 'EST', 'Started between')
        },
        {
            type: 'interval',
            dataIndex: 'finished',
            dataIndexFrom: 'finishedOnFrom',
            dataIndexTo: 'finishedOnTo',
            text: Uni.I18n.translate('estimationtasks.finished', 'EST', 'Finished between')
        }
    ]
});
