/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.HistoryOccurrence', {
  extend: 'Uni.view.container.ContentContainer',
  alias: 'widget.webservice-history-occurence',
  router: null,
  store: null,

  requires: [
    'Uni.view.container.EmptyGridContainer',
    'Wss.view.endpoint.WebserviceLogMenu',
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
                  xtype: 'webservices-menu-log',
                  itemId: 'webservices-menu-log',
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
              // privileges: Wss.privileges.Webservices.admin,
              menu: {
                  xtype: 'webservices-endpoint-action-menu',
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
            xtype: 'preview-container',
            grid: {
              xtype: 'webservice-history-occurrence-grid',
            },
            emptyComponent: {
                xtype: 'uni-form-empty-message',
                itemId: 'no-logs-found',
                text: Uni.I18n.translate('general.log-empty', 'WSS', 'There are no logs for this web service endpoint'),
            }
          }
        ]
    };

    me.callParent(arguments);
  }
});