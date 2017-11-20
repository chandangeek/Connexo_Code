/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.taskmanagement.DetailsDataQualityKpi', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-quality-kpi-details',
    requires: [],
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'data-quality-kpi-details-panel',
                    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                    flex: 1,
                    items: {
                        xtype: 'form',
                        itemId: 'data-quality-kpi-details-form',
                        margin: '0 0 0 100',
                        defaults: {
                            labelWidth: 250,
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'CFG', 'Device group'),
                                itemId: 'data-quality-kpi-device-group',
                                name: 'deviceGroup'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('datavalidationkpis.calculationFrequency', 'CFG', 'Calculation frequency'),
                                itemId: 'data-quality-kpi-frequency',
                                name: 'frequency'
                            }
                        ]
                    }
                },
                {
                    xtype: 'uni-button-action',
                    margin: '20 0 0 0',
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
                        title: Uni.I18n.translate('general.dataqualitykpi.', 'CFG', 'Data quality KPI'),
                        objectType: Uni.I18n.translate('general.dataqualitykpi.', 'CFG', 'Data quality KPI'),
                        menuItems: [
                            {
                                text: Uni.I18n.translate('general.details', 'CFG', 'Details'),
                                itemId: 'data-quality-kpi-details-overview-link'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});
