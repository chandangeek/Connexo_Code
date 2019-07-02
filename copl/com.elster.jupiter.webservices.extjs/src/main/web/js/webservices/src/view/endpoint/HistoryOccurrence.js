/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.HistoryOccurrence', {
  extend: 'Uni.view.container.ContentContainer',
  alias: 'widget.webservice-history-occurence',
  router: null,
  store: null,

  requires: [
    'Wss.view.Menu',
    'Wss.store.endpoint.Occurrence',
    'Wss.view.HistoryTopFilter',
    'Wss.view.webservice.HistoryPreviewContainer',
    'Wss.view.endpoint.OccurrenceForm',
    'Wss.view.endpoint.OccurrenceGrid',
    'Wss.view.endpoint.ActionMenu'
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
                  record: me.endpoint
              }
          ]
      }
    ];

    me.content = {
        ui: 'large',
        title: Uni.I18n.translate('general.log', 'WSS', 'Log'),
        tools: [
          {
              xtype: 'uni-button-action',
              itemId: 'webservicePreviewMenuButton',
              menu: {
                  xtype: 'webservices-endpoint-action-menu',
                  adminView: me.adminView,
                  record: me.occurrence
              }
          }
        ],
        items: [
          {
            xtype: 'webservice-history-occurence-form',
            record: me.occurrence,
            router: me.router,
            frame: true,
          },
          {
            xtype: 'webservice-history-occurrence-grid',
          }
        ]
    };

    me.callParent(arguments);
  }
});