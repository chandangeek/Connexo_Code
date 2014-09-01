Ext.define('Dsh.view.widget.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dsh-side-filter',
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    requires: [
        'Uni.component.filter.view.Filter',
        'Dsh.store.ConnectionCurrentStates',
        'Dsh.view.widget.common.SideFilterCombo'
    ],
    items: [
        {
            xtype: 'filter-form',
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
                    itemId: 'device-group',
                    name: 'deviceGroup',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceGroup', 'DSH', 'Device group'),
                    url: 'http://localhost:8080/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'current-state',
                    name: 'currentState',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
                    url: 'http://localhost:8080/apps/dashboard/app/fakeData/CurrentStateFilterFake.json'
                },
                {
                    itemId: 'latest-status',
                    name: 'latestStatus',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestStatus', 'DSH', 'Latest status'),
                    url: 'http://localhost:8080/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'latest-result',
                    name: 'latestResult',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
                    url: 'http://localhost:8080/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'comport-pool',
                    name: 'comPortPool',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.comPortPool', 'DSH', 'Communication port pool'),
                    url: 'http://localhost:8080/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'connection-type',
                    name: 'connectionType',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.connectionType', 'DSH', 'Connection type'),
                    url: 'http://localhost:8080/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'device-type',
                    name: 'deviceType',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type'),
                    url: 'http://localhost:8080/apps/dashboard/app/fakeData/BaseFilterFake.json'
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            text: Uni.I18n.translate('searchItems.searchAll', 'MDC', 'Apply'),
                            ui: 'action',
                            itemId: 'searchAllItems',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('searchItems.clearAll', 'MDC', 'Clear all'),
                            itemId: 'clearAllItems',
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});
