/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.taskmanagement.DetailsDataCollectionKpi', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-collection-kpi-details',
    requires: [],
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'data-collection-kpi-details-panel',
                    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                    flex: 1,
                    items: {
                        xtype: 'form',
                        itemId: 'data-collection-kpi-details-form',
                        margin: '0 0 0 100',
                        defaults: {
                            labelWidth: 250,
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                                itemId: 'data-collection-kpi-device-group',
                                name: 'deviceGroup'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('datacollectionkpis.calculationFrequency', 'MDC', 'Calculation frequency'),
                                itemId: 'data-collection-kpi-frequency',
                                name: 'frequency'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('datacollectionkpis.connectionKpi', 'MDC', 'Connection KPI'),
                                itemId: 'data-collection-kpi-connection',
                                name: 'connectionKpiContainer'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('datacollectionkpis.communicationKpi', 'MDC', 'Communication KPI'),
                                itemId: 'data-collection-kpi-communication',
                                name: 'communicationKpiContainer'
                            },

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
                        itemId: 'data-collection-kpi-details-side-menu',
                        title: Uni.I18n.translate('general.datadataCollectionKPI', 'MDC', 'Data collection KPI'),
                        objectType: Uni.I18n.translate('general.datadataCollectionKPI', 'MDC', 'Data collection KPI'),
                        menuItems: [
                            {
                                text: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                                itemId: 'data-collection-kpi-details-overview-link'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});
