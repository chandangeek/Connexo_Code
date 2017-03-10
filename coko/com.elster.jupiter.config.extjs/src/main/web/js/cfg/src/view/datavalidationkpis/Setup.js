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
            title: Uni.I18n.translate('general.dataValidationKpis', 'CFG', 'Data validation KPIs'),
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
                        title: Uni.I18n.translate('datavalidationkpis.empty.title', 'CFG', 'No data validation KPIs found'),
                        reasons: [
                            Uni.I18n.translate('datavalidationkpis.empty.list.item1', 'CFG', 'No data validation KPIs have been created yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('datavalidationkpis.add', 'CFG', 'Add data validation KPI'),
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