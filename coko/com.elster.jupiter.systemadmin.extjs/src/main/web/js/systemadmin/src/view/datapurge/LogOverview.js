/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.datapurge.LogOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-purge-log-overview',
    requires: [
        'Sam.view.datapurge.LogPreviewForm',
        'Sam.view.datapurge.LogGrid',
        'Sam.view.datapurge.LogSortingToolbar',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('datapurge.log.breadcrumb', 'SAM', 'Data purge log'),
                ui: 'large',
                items: [
                    {
                        itemId: 'data-purge-log-form',
                        xtype: 'data-purge-log-preview-form'
                    },
                    {
                        itemId: 'data-purge-log-sorting-toolbar',
                        xtype: 'data-purge-log-sorting-toolbar',
                        hideEmpty: false
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            itemId: 'data-purge-log-grid',
                            xtype: 'data-purge-log-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            itemId: 'data-purge-log-no-items-found-panel',
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('datapurge.log.empty', 'SAM', 'No logging has been found for the data purge task.')
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});