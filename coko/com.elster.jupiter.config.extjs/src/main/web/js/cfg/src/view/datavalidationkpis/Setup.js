/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.datavalidationkpis.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-data-validation-kpi-setup',
    requires: [
        'Cfg.view.datavalidationkpis.Grid',
        'Cfg.view.datavalidationkpis.Preview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.dataQualityKpis', 'CFG', 'Data quality KPIs'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'cfg-data-validation-kpis-grid',
                        itemId: 'data-validation-kpis-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-datavalidationkpis',
                        title: Uni.I18n.translate('dataqualitykpis.empty.title', 'CFG', 'No data quality KPIs found'),
                        reasons: [
                            Uni.I18n.translate('dataqualitykpis.empty.list.item1', 'CFG', 'No data quality KPIs have been created yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('dataqualitykpis.add', 'CFG', 'Add data quality KPI'),
                                action: 'addDataValidationKpi',
                                privileges: Cfg.privileges.Validation.admin
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'cfg-data-validation-kpis-preview',
                        itemId: 'preview-data-validation-kpis'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});