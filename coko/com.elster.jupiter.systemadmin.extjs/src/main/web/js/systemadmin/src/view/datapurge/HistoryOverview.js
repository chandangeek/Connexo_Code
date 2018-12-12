/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.datapurge.HistoryOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-purge-history-overview',
    requires: [
        'Sam.view.datapurge.HistoryGrid',
        'Sam.view.datapurge.HistoryDetails',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('datapurge.history.breadcrumb', 'SAM', 'Data purge history'),
                ui: 'large',
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            itemId: 'data-purge-history-grid',
                            xtype: 'data-purge-history-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            itemId: 'data-purge-history-no-items-found-panel',
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('datapurge.history.empty', 'SAM', 'No data has been purged so far.')
                        },
                        previewComponent: {
                            itemId: 'data-purge-history-details',
                            xtype: 'data-purge-history-details'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});