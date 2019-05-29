/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.webservice.HistoryPreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.webservices-webservice-history-preview',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Wss.view.webservice.HistoryGrid',
        'Wss.view.webservice.HistoryForm'
    ],

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'no-webservice-history',
        title: Uni.I18n.translate('webservices.history.empty.title', 'WSS', 'No web service history lines found'),
        reasons: [
            Uni.I18n.translate('webservices.history.empty', 'WSS', 'No web service history lines collected yet.')
        ],
    },

    router: null,

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'wss-webservice-history-grid',
            itemId: 'wss-webservice-history-grid',
            endpoint: me.endpoint,
            router: me.router,
            store: Boolean(me.endpoint)
              ? store = 'Wss.store.endpoint.EndpointOccurrence'
              : store = 'Wss.store.endpoint.Occurrence'
        };

        me.previewComponent = {
            xtype: 'webservices-webservice-history-form',
            itemId: 'webservices-webservice-history-form',
            router: me.router,
            endpoint: me.endpoint,
            frame: true
        };

        me.callParent(arguments);
    }
});