Ext.define('Dsh.view.widget.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dsh-side-filter',
    requires: [
        'Uni.component.filter.view.Filter',
        'Dsh.view.widget.common.SideFilterCombo',
        'Dsh.view.widget.common.SideFilterDateTime'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    items: [
        {
            xtype: 'nested-form',
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
                    url: '/apps/dsh/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'current-state',
                    name: 'state',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
                    url: '/api/dsr/field/taskstatus',
                    root: 'taskStatuses',
                    valueField: "taskStatus"
          //          displayField: "localizedValue"
                },
                {
                    itemId: 'latest-status',
                    name: 'latestStatus',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestStatus', 'DSH', 'Latest status'),
                    url: '/api/dsr/field/connectiontasksuccessindicators',
                    root: "successIndicators",
                    valueField: "successIndicator"
          //          displayField: "localizedValue"
                },
                {
                    itemId: 'latest-result',
                    name: 'latestResult',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
                    url: '/api/dsr/field/comsessionsuccessindicators',
                    root: "successIndicators",
                    valueField: "successIndicator"
          //          displayField: "localizedValue"
                },
                {
                    itemId: 'comport-pool',
                    name: 'comPortPool',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.comPortPool', 'DSH', 'Communication port pool'),
                    url: '/apps/dsh/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'connection-type',
                    name: 'connectionType',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.connectionType', 'DSH', 'Connection type'),
                    url: '/apps/dsh/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'device-type',
                    name: 'deviceType',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type'),
                    url: '/apps/dsh/app/fakeData/BaseFilterFake.json'
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
