/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.view.RegisteredDevicesOnGateway', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registered-devices-on-gateway-view',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.grid.FilterPanelTop',
        'Mdc.registereddevices.store.RegisteredDevicesOnGateway',
        'Mdc.registereddevices.store.RegisteredDevicesKPIFrequencies'
    ],

    device: undefined,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'mdc-registered-devices-on-gateway-device-menu',
                        device: me.device,
                        toggleId: 'registeredDevices'
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.registeredDevices', 'MDC', 'Registered devices'),
                items: [
                    {
                        xtype: 'uni-grid-filterpaneltop',
                        itemId: 'mdc-registered-devices-on-gateway-filters',
                        store: 'Mdc.registereddevices.store.RegisteredDevicesOnGateway',
                        filters: [
                            {
                                type: 'combobox',
                                name: 'frequency',
                                dataIndex: 'frequency',
                                emptyText: Uni.I18n.translate('general.frequency', 'MDC', 'Frequency'),
                                itemId: 'mdc-registered-devices-on-gateway-frequency-combo',
                                store: 'Mdc.registereddevices.store.RegisteredDevicesKPIFrequencies',
                                queryMode: 'local',
                                editable: false,
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                type: 'interval',
                                itemId: 'mdc-registered-devices-on-gateway-period-filter',
                                dataIndex: 'period',
                                dataIndexFrom: 'start',
                                dataIndexTo: 'end',
                                text: Uni.I18n.translate('general.period', 'MDC', 'Period'),
                                fromAndToValueRequired: true,
                                maxValue: new Date()
                            }
                        ]
                    },
                    {
                        xtype: 'registered-devices-graph',
                        itemId: 'mdc-registered-devices-on-gateway-graph',
                        data: me.data
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.on('boxready', me.onBoxReady, me);
    },

    onBoxReady: function() {
        this.down('#mdc-registered-devices-on-gateway-filters').down('#filter-clear-all').hide();
        this.down('#mdc-registered-devices-on-gateway-filters').down('#filter-apply-all').hide();
        this.down('#mdc-registered-devices-on-gateway-filters').down('#filter-apply-all').fireEvent('click');
    }
});
