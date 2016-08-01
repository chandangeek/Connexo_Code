Ext.define('Dsh.view.widget.CommunicationsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dsh-view-widget-communicationstopfilter',

    store: 'Dsh.store.CommunicationTasks',

    filters: [
        {
            type: 'combobox',
            dataIndex: 'deviceGroups',
            emptyText: Uni.I18n.translate('general.deviceGroup', 'DSH', 'Device group'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.DeviceGroup',
            itemId: 'device-group-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'currentStates',
            emptyText: Uni.I18n.translate('general.Status', 'DSH', 'Status'),
            multiSelect: true,
            displayField: 'localizedValue',
            valueField: 'taskStatus',
            store: 'Dsh.store.filter.CurrentState',
            itemId: 'current-state-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'latestResults',
            emptyText: Uni.I18n.translate('general.lastResult', 'DSH', 'Last result'),
            multiSelect: true,
            displayField: 'localizedValue',
            valueField: 'completionCode',
            store: 'Dsh.store.filter.CompletionCodes',
            itemId: 'latest-result-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'comTasks',
            emptyText: Uni.I18n.translate('general.commTask', 'DSH', 'Communication task'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.CommunicationTask',
            itemId: 'com-task-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'comSchedules',
            emptyText: Uni.I18n.translate('connection.widget.filter.communicationSchedule', 'DSH', 'Communication schedule'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.CommunicationSchedule',
            itemId: 'com-schedule-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'deviceTypes',
            emptyText: Uni.I18n.translate('general.deviceType', 'DSH', 'Device type'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.DeviceType',
            itemId: 'device-type-filter'
        },
        {
            type: 'interval',
            dataIndex: 'startInterval',
            dataIndexFrom: 'startIntervalFrom',
            dataIndexTo: 'startIntervalTo',
            text: Uni.I18n.translate('communications.widget.topfilter.startedBetween', 'DSH', 'Started between'),
            itemId: 'start-interval-filter'
        },
        {
            type: 'interval',
            dataIndex: 'finishInterval',
            dataIndexFrom: 'finishIntervalFrom',
            dataIndexTo: 'finishIntervalTo',
            text: Uni.I18n.translate('communications.widget.topfilter.finishedBetween', 'DSH', 'Finished between'),
            itemId: 'finish-interval-filter'
        }
    ]
});