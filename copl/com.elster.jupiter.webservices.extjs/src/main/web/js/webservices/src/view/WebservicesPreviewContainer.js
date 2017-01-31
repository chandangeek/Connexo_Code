/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.WebservicesPreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.webservices-preview-container',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Wss.view.Grid',
        'Wss.view.Preview'
    ],

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'no-webservices',
        title: Uni.I18n.translate('webservices.empty.title', 'WSS', 'No web service endpoints found'),
        reasons: [
            Uni.I18n.translate('webservices.mpty', 'WSS', 'No web service endpoints have been defined yet.')
        ],
        stepItems: [
            {
                text: Uni.I18n.translate('general.addWebserviceEndpoint', 'WSS', 'Add web service endpoint'),
                privileges: Wss.privileges.Webservices.admin,
                itemId: 'wss-no-webservice-endpoints-add-btn',
            }
        ]
    },

    router: null,

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'webservices-grid',
            itemId: 'grd-webservices',
            router: me.router
        };

        me.previewComponent = {
            xtype: 'webservices-preview',
            itemId: 'pnl-webservices-preview',
        };

        me.callParent(arguments);
    }
});