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
        'Uni.util.FormInfoMessage',
        'Mdc.registereddevices.store.AvailableKPIs',
        'Mdc.privileges.RegisteredDevicesKpi',
        'Mdc.registereddevices.store.RegisteredDevicesKPIsData'
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
                            Uni.I18n.translate('registeredDevicesKPIs.empty.list.item', 'MDC', 'No registered devices KPIs have been defined yet.')
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
                        store: 'Mdc.registereddevices.store.RegisteredDevicesKPIsData',
                        filters: [
                            {
                                type: 'combobox',
                                editable: false,
                                itemId: 'mdc-registered-devices-device-group-filter',
                                dataIndex: 'kpiId',
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
                                dataIndexFrom: 'start',
                                dataIndexTo: 'end',
                                text: Uni.I18n.translate('general.period', 'MDC', 'Period'),
                                fromAndToValueRequired: true,
                                maxValue: new Date()
                            }
                        ]
                    },
                    {
                        xtype: 'uni-form-info-message',
                        itemId: 'mdc-registered-devices-view-no-data',
                        text: Uni.I18n.translate('registeredDevicesKPIs.noData', 'MDC', 'This registered devices KPI has no data yet.')
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
        me.on('boxready', me.onBoxReady, me);
    },

    onBoxReady: function() {
        this.down('#mdc-registered-devices-filters').down('#filter-clear-all').hide();
        this.down('#mdc-registered-devices-filters').down('#filter-apply-all').hide();
        this.down('#mdc-registered-devices-filters').down('#filter-apply-all').fireEvent('click');
    }
});