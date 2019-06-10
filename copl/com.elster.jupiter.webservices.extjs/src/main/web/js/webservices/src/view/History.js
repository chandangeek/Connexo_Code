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
    'Wss.view.webservice.HistoryPreviewContainer',
    'Uni.grid.commander.SortingPanel',
    'Wss.view.webservice.SortMenu'
  ],

  initComponent: function () {
    var me = this;

    me.content = {
        ui: 'large',
        title: Uni.I18n.translate('webservices.webserviceHistory', 'WSS', 'Web service history'),
        dockedItems: [
          {
            dock: 'top',
            store: 'Wss.store.endpoint.Occurrence',
            xtype: 'mss-view-history-history-topfilter',
            itemId: 'mss-view-history-history-topfilter',
            adminView: me.adminView,
          },
          {
            dock: 'top',
            store: 'Wss.store.endpoint.Occurrence',
            xtype: 'uni-grid-commander-sortingpanel',
            menu: 'wss-webservice-sort-menu',
            items: [
              {
                property: 'status',
                direction: Uni.component.sort.model.Sort.DESC
              },
              {
                property: 'startTime',
                direction: Uni.component.sort.model.Sort.DESC
              }
            ]
          },
        ],
        items: [
          {
            xtype: 'webservices-webservice-history-preview',
            itemId: 'webservices-webservice-history-preview',
            adminView: me.adminView,
            router: me.router
          }
        ],
    };

    me.callParent(arguments);
  }
});