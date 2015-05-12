Ext.define('Dsh.view.widget.CommunicationsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dsh-view-widget-communicationstopfilter',

    store: 'Dsh.store.CommunicationTasks',

    filters: [
        {
            type: 'combobox',
            dataIndex: 'deviceGroup',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.deviceGroup', 'DSH', 'Device group'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.DeviceGroup'
        },
        {
            type: 'combobox',
            dataIndex: 'currentStates',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
            multiSelect: true,
            displayField: 'localizedValue',
            valueField: 'taskStatus',
            store: 'Dsh.store.filter.CurrentState'
        },
        {
            type: 'combobox',
            dataIndex: 'latestResults',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
            multiSelect: true,
            displayField: 'localizedValue',
            valueField: 'completionCode',
            store: 'Dsh.store.filter.CompletionCodes'
        },
        {
            type: 'combobox',
            dataIndex: 'comTasks',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.commTask', 'DSH', 'Communication task'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.CommunicationTask'
        },
        {
            type: 'combobox',
            dataIndex: 'comSchedules',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.communicationSchedule', 'DSH', 'Communication schedule'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.CommunicationSchedule'
        },
        {
            type: 'combobox',
            dataIndex: 'deviceTypes',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.DeviceType'
        },
        {
            type: 'interval',
            dataIndex: 'startInterval',
            dataIndexFrom: 'startIntervalFrom',
            dataIndexTo: 'startIntervalTo',
            text: Uni.I18n.translate('communications.widget.topfilter.startedBetween', 'DSH', 'Started between')
        },
        {
            type: 'interval',
            dataIndex: 'finishInterval',
            dataIndexFrom: 'finishIntervalFrom',
            dataIndexTo: 'finishIntervalTo',
            text: Uni.I18n.translate('communications.widget.topfilter.finishedBetween', 'DSH', 'Finished between')
        }
    ]
});