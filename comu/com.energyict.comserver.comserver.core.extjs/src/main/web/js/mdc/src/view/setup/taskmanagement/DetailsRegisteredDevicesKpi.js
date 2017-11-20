/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.taskmanagement.DetailsRegisteredDevicesKpi', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registered-devices-kpi-details',
    requires: [],
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'registered-devices-kpi-details-panel',
                    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                    flex: 1,
                    items: {
                        xtype: 'form',
                        itemId: 'registered-devices-kpi-details-form',
                        margin: '0 0 0 100',
                        defaults: {
                            labelWidth: 250,
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                                itemId: 'registered-devices-kpi-device-group',
                                name: 'deviceGroup'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('datacollectionkpis.calculationFrequency', 'MDC', 'Calculation frequency'),
                                itemId: 'registered-devices-kpi-frequency',
                                name: 'frequency'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.target', 'MDC', 'Target'),
                                itemId: 'registered-devices-kpi-target',
                                name: 'target'
                            }
                        ]
                    }
                },
                {
                    xtype: 'uni-button-action',
                    margin: '20 0 0 0',
                    privileges: function () {
                        return me.canAdministrate;
                    },
                    menu: me.actionMenu
                }
            ]
        };
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'uni-view-menu-side',
                        itemId: 'registered-devices-kpi-details-side-menu',
                        title: Uni.I18n.translate('general.registeredDevicesKPI', 'MDC', 'Registered devices KPI'),
                        objectType: Uni.I18n.translate('general.registeredDevicesKPI', 'MDC', 'Registered devices KPI'),
                        menuItems: [
                            {
                                text: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                                itemId: 'registered-devices-kpi-details-overview-link'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});
