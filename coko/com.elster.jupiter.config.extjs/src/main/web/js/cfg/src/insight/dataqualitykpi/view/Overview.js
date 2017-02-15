/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ins-data-quality-kpi-overview',
    requires: [
        'Uni.util.FormInfoMessage',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.insight.dataqualitykpi.view.Grid',
        'Cfg.insight.dataqualitykpi.view.DetailsForm'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: me.router.getRoute().getTitle(),
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        itemId: 'data-quality-kpi-overview-info',
                        text: Uni.I18n.translate('ins.dataqualitykpi.overview.info', 'CFG', 'KPIs for inactive purposes of usage points are not calculated')
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'ins-data-quality-kpi-grid',
                            itemId: 'data-quality-kpis-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'data-quality-kpi-overview-empty-message',
                            title: Uni.I18n.translate('dataqualitykpis.empty.title', 'CFG', 'No data quality KPIs found'),
                            reasons: [
                                Uni.I18n.translate('dataqualitykpis.empty.list.item1', 'CFG', 'No data quality KPIs have been created yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('dataqualitykpis.add', 'CFG', 'Add data quality KPI'),
                                    itemId: 'data-quality-kpi-overview-empty-message-add-btn',
                                    privileges: Cfg.privileges.Validation.admin,
                                    href: me.router.getRoute('administration/datavalidationkpis/add').buildUrl()
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'ins-data-quality-kpi-details-form',
                            itemId: 'data-quality-kpi-preview',
                            frame: true
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});