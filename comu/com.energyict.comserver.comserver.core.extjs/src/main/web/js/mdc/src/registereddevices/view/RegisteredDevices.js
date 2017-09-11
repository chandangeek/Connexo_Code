/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.registereddevices.view.RegisteredDevices', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registered-devices-view',

    requires: [
        'Mdc.registereddevices.view.RegisteredDevicesGraph',
        'Uni.grid.FilterPanelTop',
        'Mdc.store.DeviceGroupsNoPaging'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.registeredDevices', 'MDC', 'Registered devices'),
                items: [
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
                                store: 'Mdc.store.DeviceGroupsNoPaging'
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
                        xtype: 'registered-devices-graph'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});