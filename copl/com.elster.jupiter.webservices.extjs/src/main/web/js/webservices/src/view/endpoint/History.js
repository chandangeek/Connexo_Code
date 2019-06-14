/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.History', {
  extend: 'Uni.view.container.ContentContainer',
  alias: 'widget.webservice-history',
  router: null,
  store: null,

  requires: [
    'Wss.view.Menu',
    'Wss.store.endpoint.EndpointOccurrence',
    'Wss.view.HistoryTopFilter',
    'Wss.view.webservice.HistoryPreviewContainer'
  ],

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
        ui: 'large',
        title: Uni.I18n.translate('general.history', 'WSS', 'History'),
        items: [
          {
            xtype: 'webservices-webservice-history-preview',
            itemId: 'webservices-webservice-history-preview',
            endpoint: me.record,
            router: me.router,
            adminView: me.adminView
          }
        ],
        dockedItems: [
          {
            dock: 'top',
            store: 'Wss.store.endpoint.EndpointOccurrence',
            xtype: 'mss-view-history-history-topfilter',
            itemId: 'mss-view-history-history-topfilter',
            endpoint: me.record
          }
        ]
    };

    me.callParent(arguments);
  }
});