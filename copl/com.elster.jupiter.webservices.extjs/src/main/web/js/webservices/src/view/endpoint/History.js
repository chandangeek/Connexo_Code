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
    'Wss.store.endpoint.Occurrence',
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
        title: Uni.I18n.translate('webservices.webserviceHistory', 'WSS', 'Web service history'),
        items: [
          {
            xtype: 'webservices-webservice-history-preview',
            itemId: 'webservices-webservice-history-preview',
            router: me.router,
          }
        ],
        dockedItems: [
          {
            dock: 'top',
            store: 'Wss.store.endpoint.Occurrence',
            xtype: 'mss-view-history-history-topfilter',
            itemId: 'mss-view-history-history-topfilter'
          },
          {
            xtype: 'filter-toolbar',
            title: Uni.I18n.translate('webservices.filter.sort', 'WSS', 'Sort'),
            name: 'sortitemspanel',
            itemId: 'mss-view-history-sort-toolbar',
            emptyText: Uni.I18n.translate('general.none','WSS','None'),
            tools: [
                {
                    xtype: 'button',
                    action: 'addSort',
                    itemId: 'add-sort-btn',
                    text: Uni.I18n.translate('general.history.addSort', 'FIM', 'Add sort'),
                    // menu: {
                    //     xtype: 'fim-history-sort-menu',
                    //     itemId: 'menu-history-sort',
                    //     name: 'addsortitemmenu'
                    // }
                }
            ]
        },
        ]
    };

    me.callParent(arguments);
  }
});