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
    'Wss.view.endpoint.OccurrenceForm'
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
        items: [
          {
            xtype: 'webservice-history-occurence-form',
            record: me.occurrence,
            frame: true,
          }
        ]
    };

    me.callParent(arguments);
  }
});