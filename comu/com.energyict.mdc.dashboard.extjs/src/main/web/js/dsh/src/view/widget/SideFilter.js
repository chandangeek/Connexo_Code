Ext.define('Dsh.view.widget.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dsh-side-filter',
    requires: [
        'Uni.component.filter.view.Filter',
        'Dsh.view.widget.common.SideFilterCombo',
        'Dsh.view.widget.common.SideFilterDateTime',
        'Dsh.store.filter.CurrentState',
        'Dsh.store.filter.LatestStatus',
        'Dsh.store.filter.LatestResult',
        'Dsh.store.filter.CommPortPool',
        'Dsh.store.filter.ConnectionType',
        'Dsh.store.filter.DeviceType'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    items: [
        {
            xtype: 'nested-form',
            itemId: 'filter-form',
            hydrator: 'Dsh.util.FilterHydrator',
            ui: 'filter',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                xtype: 'side-filter-combo',
                labelAlign: 'top'
            },
            items: [
                {
                    itemId: 'current-state',
                    name: 'currentStates',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
                    displayField: 'localizedValue',
                    valueField: 'taskStatus',
                    store: 'Dsh.store.filter.CurrentState'
                },
                {
                    itemId: 'latest-status',
                    name: 'latestStates',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestStatus', 'DSH', 'Latest status'),
                    displayField: 'localizedValue',
                    valueField: 'successIndicator',
                    store: 'Dsh.store.filter.LatestStatus'
                },
                {
                    itemId: 'latest-result',
                    name: 'latestResults',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
                    displayField: 'localizedValue',
                    valueField: 'successIndicator',
                    store: 'Dsh.store.filter.LatestResult'
                },
                {
                    itemId: 'comport-pool',
                    name: 'comPortPools',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.comPortPool', 'DSH', 'Communication port pool'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.CommPortPool'
                },
                {
                    itemId: 'connection-type',
                    name: 'connectionTypes',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.connectionType', 'DSH', 'Connection type'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.ConnectionType'
                },
                {
                    itemId: 'device-type',
                    name: 'deviceTypes',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.DeviceType'
                },
                {
                    xtype: 'side-filter-date-time',
                    itemId: 'started-between',
                    name: 'startedBetween',
                    wTitle: Uni.I18n.translate('connection.widget.sideFilter.startedBetween', 'DSH', 'Started between')
                },
                {
                    xtype: 'side-filter-date-time',
                    itemId: 'finished-between',
                    name: 'finishedBetween',
                    wTitle: Uni.I18n.translate('connection.widget.sideFilter.finishedBetween', 'DSH', 'Finished successfully between')
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.apply', 'DSH', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.clearAll', 'DSH', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});
