/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.registereddevices.view.RegisteredDevices', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registered-devices-view',

    requires: [
        'Mdc.registereddevices.view.RegisteredDevicesGraph',
        'Uni.grid.FilterPanelTop',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.registereddevices.store.AvailableKPIs',
        'Mdc.privileges.RegisteredDevicesKpi'
    ],

    data: undefined,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.registeredDevices', 'MDC', 'Registered devices'),
                items: [
                    {
                        xtype: 'no-items-found-panel',
                        itemId: 'mdc-registered-devices-view-no-kpis',
                        hidden: true,
                        title: Uni.I18n.translate('registeredDevicesKPIs.empty.title', 'MDC', 'No registered devices KPIs found'),
                        reasons: [
                            Uni.I18n.translate('registeredDevicesKPIs.empty.list.item1', 'MDC', 'No registered devices KPIs have been defined yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('registeredDevicesKPIs.add', 'MDC', 'Add registered devices KPI'),
                                itemId: 'mdc-registered-devices-view-add-kpi',
                                action: 'addRegisteredDevicesKpi',
                                privileges: Mdc.privileges.RegisteredDevicesKpi.admin
                            }
                        ]
                    },
                    {
                        xtype: 'uni-grid-filterpaneltop',
                        itemId: 'mdc-registered-devices-filters',
                        filters: [
                            {
                                type: 'combobox',
                                itemId: 'mdc-registered-devices-device-group-filter',
                                dataIndex: 'deviceGroups',
                                emptyText: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                                multiSelect: false,
                                displayField: 'name',
                                valueField: 'id',
                                store: 'Mdc.registereddevices.store.AvailableKPIs'
                            },
                            {
                                type: 'interval',
                                itemId: 'mdc-registered-devices-period-filter',
                                dataIndex: 'period',
                                dataIndexFrom: 'periodStart',
                                dataIndexTo: 'periodEnd',
                                text: Uni.I18n.translate('general.period', 'MDC', 'Period')
                            }
                        ]
                    },
                    {
                        xtype: 'registered-devices-graph',
                        itemId: 'mdc-registered-devices-graph',
                        data: me.data
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});