/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.history.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.output-readings-history',
    requires: [
        'Imt.purpose.view.history.HistoryGrid',
        'Imt.purpose.view.history.HistoryFilter',
        'Imt.purpose.view.history.HistoryIntervalFilter',
        'Imt.purpose.view.history.HistoryIntervalPreview',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.purpose.view.registers.RegisterDataPreview',
        'Imt.purpose.view.history.HistoryRegisterPreview'
    ],

    usagePoint: null,
    purposes: null,
    output: null,
    filterDefault: null,
    isBulk: false,
    store: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint,
                        purposes: me.purposes
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            itemId: 'output-readings-history-panel',
            ui: 'large',
            layout: 'fit',
            title: Uni.I18n.translate('general.history', 'IMT', 'History'),
            items: [
                {
                    xtype: 'preview-container',
                    itemId: 'output-readings-history-preview-container',
                    grid: {
                        xtype: 'output-readings-history-grid',
                        itemId: 'output-readings-history-grid',
                        store: me.store,
                        output: me.output,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('outputReadingsHistory.empty.title', 'IMT', 'No history items found'),
                        reasons: [
                            Uni.I18n.translate('outputReadingsHistory.empty.list.item1', 'IMT', "The filter hasn't been specified yet."),
                            Uni.I18n.translate('outputReadingsHistory.empty.list.item2', 'IMT', 'No history items comply with the filter.')
                        ]
                    },
                    previewComponent: {
                        xtype: me.getPreviewComponent(me.output.get('outputType')),
                        itemId: 'output-readings-history-preview',
                        output: me.output,
                        outputType: me.output.get('outputType'),
                        router: me.router,
                        hidden: true
                    }
                }
            ],
            dockedItems: [
                {
                    xtype: me.isBulk ? 'output-readings-history-filter' : 'output-readings-history-interval-filter',
                    itemId: me.isBulk ? 'output-readings-history-filter' : 'output-readings-history-interval-filter',
                    store: me.store,
                    dock: 'top',
                    filterDefault: me.filterDefault,
                    hidden: !me.isBulk
                }
            ]
        };

        me.callParent(arguments);
    },

    getPreviewComponent: function (type) {
        if (type === 'register')
            return 'history-register-preview';

        return 'history-interval-preview'
    }
});