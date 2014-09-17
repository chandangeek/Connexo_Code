Ext.define('Dsh.view.widget.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dsh-side-filter',
    requires: [
        'Uni.component.filter.view.Filter',
        'Dsh.view.widget.common.SideFilterCombo',
        'Dsh.view.widget.common.SideFilterDateTime',
        'Dsh.util.FilterHydrator'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    items: [
        {
            xtype: 'nested-form',
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
                    name: 'state',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
                    displayField: 'localizedValue',
                    valueField: 'taskStatus',
                    store: 'Dsh.store.filter.CurrentState'
                },
                {
                    itemId: 'latest-status',
                    name: 'latestStatus',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestStatus', 'DSH', 'Latest status'),
                    displayField: 'localizedValue',
                    valueField: 'successIndicator',
                    store: 'Dsh.store.filter.LatestStatus'
                },
                {
                    itemId: 'latest-result',
                    name: 'latestResult',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
                    displayField: 'localizedValue',
                    valueField: 'successIndicator',
                    store: 'Dsh.store.filter.LatestResult'
                },
                {
                    itemId: 'comport-pool',
                    name: 'comPortPool',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.comPortPool', 'DSH', 'Communication port pool'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.CommPortPool'
                },
                {
                    itemId: 'connection-type',
                    name: 'connectionType',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.connectionType', 'DSH', 'Connection type'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.ConnectionType'
                },
                {
                    itemId: 'device-type',
                    name: 'deviceType',
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
                    wTitle: Uni.I18n.translate('connection.widget.sideFilter.finishedBetween', 'DSH', 'Finished between')
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
