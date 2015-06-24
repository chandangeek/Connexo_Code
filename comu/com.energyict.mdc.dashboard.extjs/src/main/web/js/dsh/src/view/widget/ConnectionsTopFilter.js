Ext.define('Dsh.view.widget.ConnectionsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dsh-view-widget-connectionstopfilter',

    store: 'Dsh.store.ConnectionTasks',

    filters: [
        {
            type: 'combobox',
            dataIndex: 'deviceGroups',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.deviceGroup', 'DSH', 'Device group'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.DeviceGroup',
            itemId: 'device-group-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'currentStates',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
            multiSelect: true,
            displayField: 'localizedValue',
            valueField: 'taskStatus',
            store: 'Dsh.store.filter.CurrentState',
            itemId: 'current-state-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'latestStates',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.latestStatus', 'DSH', 'Latest status'),
            multiSelect: true,
            displayField: 'localizedValue',
            valueField: 'successIndicator',
            store: 'Dsh.store.filter.LatestStatus',
            itemId: 'latest-state-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'latestResults',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
            multiSelect: true,
            displayField: 'localizedValue',
            valueField: 'successIndicator',
            store: 'Dsh.store.filter.LatestResult',
            itemId: 'latest-result-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'comPortPools',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.comPortPool', 'DSH', 'Communication port pool'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.CommPortPool',
            itemId: 'com-port-pools-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'connectionTypes',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.connectionType', 'DSH', 'Connection type'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.ConnectionType',
            itemId: 'connection-type-filter'
        },
        {
            type: 'combobox',
            dataIndex: 'deviceTypes',
            emptyText: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type'),
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