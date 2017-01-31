/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.LoggingPage', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.webservice-logging-page',
    requires: [
        'Wss.view.Menu',
        'Uni.util.FormEmptyMessage',
        'Wss.view.LoggingGrid',
        'Uni.view.container.PreviewContainer'
    ],

    router: null,
    record: null,

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'webservices-menu',
                        itemId: 'webservices-menu',
                        router: me.router,
                        record: me.record
                    }
                ]
            }
        ];

        me.content = {
            title: Uni.I18n.translate('general.Logging', 'WSS', 'Logging'),
            xtype: 'panel',
            ui: 'large',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'wss-logging-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('webservices.log.empty.list', 'WSS', 'There are no logs for this web service endpoint')
                    }
                }
            ]

        };

        me.callParent(arguments);
    }
});