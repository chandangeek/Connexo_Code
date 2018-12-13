/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.datacollectionkpis.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dataCollectionKpiSetup',
    itemId: 'dataCollectionKpiSetup',
    requires: [
        'Mdc.view.setup.datacollectionkpis.Grid',
        'Mdc.view.setup.datacollectionkpis.Preview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.dataCollectionKpis', 'MDC', 'Data collection KPIs'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dataCollectionKpisGrid',
                        itemId: 'datacollectionkpisgrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-datacollectionkpis',
                        title: Uni.I18n.translate('datacollectionkpis.empty.title', 'MDC', 'No data collection KPIs found'),
                        reasons: [
                            Uni.I18n.translate('datacollectionkpis.empty.list.item1', 'MDC', 'No data collection KPIs have been created yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('datacollectionkpis.add', 'MDC', 'Add data collection KPI'),
                                action: 'addDataCollectionKpi',
                                privileges: Mdc.privileges.DataCollectionKpi.admin
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'dataCollectionKpisPreview',
                        itemId: 'datacollectionkpispreview'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});