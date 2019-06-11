/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.History', {
  extend: 'Uni.view.container.ContentContainer',
  alias: 'widget.webservices-history',
  router: null,
  store: null,

  requires: [
    'Wss.store.endpoint.Occurrence',
    'Wss.view.HistoryTopFilter',
    'Wss.view.webservice.HistoryPreviewContainer'
  ],

  initComponent: function () {
    var me = this;

    me.content = {
        ui: 'large',
        title: Uni.I18n.translate('webservices.webserviceHistory', 'WSS', 'Web service history'),
        items: [
          {
            xtype: 'webservices-webservice-history-preview',
            itemId: 'webservices-webservice-history-preview',
            adminView: me.adminView,
            router: me.router
          }
        ],
        dockedItems: [
          {
            dock: 'top',
            store: 'Wss.store.endpoint.Occurrence',
            xtype: 'mss-view-history-history-topfilter',
            itemId: 'mss-view-history-history-topfilter',
            adminView: me.adminView,
          },
        ]
    };

    me.callParent(arguments);
  }
});