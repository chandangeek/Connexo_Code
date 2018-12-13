/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.webservices-setup',
    router: null,
    store: null,

    requires: [
        'Wss.view.WebservicesPreviewContainer'
    ],
    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('webservices.webserviceEndpoints', 'WSS', 'Web service endpoints'),
            items: [
                {
                    xtype: 'webservices-preview-container',
                    router: me.router
                }
            ]
        };


        me.callParent(arguments);
    }
});