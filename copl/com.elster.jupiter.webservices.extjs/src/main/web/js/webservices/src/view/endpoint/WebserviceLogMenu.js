/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.WebserviceLogMenu', {
  extend: 'Uni.view.menu.SideMenu',
  alias: 'widget.webservices-menu-log',

  router: null,
  record: null,
  objectType: Uni.I18n.translate('webservices.webserviceEndpoint', 'WSS', 'Web service endpoint'),


  initComponent: function () {
      var me = this;
      var route = me.router.getRoute();
      var basename = route.key.split('/').slice(0, 2).join('/');

      me.title = me.record.get('name') || Uni.I18n.translate('webservices.webserviceEndpoint', 'WSS', 'Web service endpoint');

      me.menuItems = [
          {
              text: Uni.I18n.translate('general.details', 'WSS', 'Details'),
              itemId: 'webservice-overview-link',
              href: me.router
                  .getRoute(basename + '/view')
                  .buildUrl({endpointId: me.record.get('id')})
          },
          {
              itemId: 'wenservoces-log-link',
              text: Uni.I18n.translate('general.log', 'WSS', 'Log'),
              href: me.router
                  .getRoute(basename + '/view/history/occurrence')
                  .buildUrl({endpointId: me.record.get('id')})
          },
          {
              text: Uni.I18n.translate('general.endpointStatusHistory', 'WSS', 'Endpoint status history'),
              itemId: 'wenservoces-logs-link',
              href: me.router
                  .getRoute(basename + '/view/status')
                  .buildUrl({endpointId: me.record.get('id')})
          },
      ];

      me.callParent(arguments);
  }

});

