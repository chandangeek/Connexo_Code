/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.Status', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.webservice-endpoint-status',
    requires: [
        'Wss.view.Menu',
        'Uni.util.FormEmptyMessage',
        'Wss.view.endpoint.StatusGrid',
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
            title: Uni.I18n.translate('general.endpointStatusHistory', 'WSS', 'Endpoint status history'),
            xtype: 'panel',
            ui: 'large',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'wss-endpoint-status-grid',
                        itemId: 'wss-endpoint-status-grid-id'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate(
                            'endpointStatusHistory.empty.list',
                            'WSS',
                            'There are no endpoint status history records for this web service endpoint'
                        )
                    }
                }
            ]

        };

        me.callParent(arguments);
    }
});