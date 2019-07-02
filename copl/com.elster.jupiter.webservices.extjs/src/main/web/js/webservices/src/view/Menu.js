/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.webservices-menu',

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
                text: Uni.I18n.translate('general.history', 'WSS', 'History'),
                itemId: 'webservice-history-link',
                privileges: Wss.privileges.Webservices.viewHistory,
                href: me.router
                    .getRoute(basename + '/view/history')
                    .buildUrl({endpointId: me.record.get('id')})
            },
            {
                text: Uni.I18n.translate('general.endpointStatusHistory', 'WSS', 'Endpoint status history'),
                itemId: 'webservice-logs-link',
                href: me.router
                    .getRoute(basename + '/view/status')
                    .buildUrl({endpointId: me.record.get('id')})
            },
        ];

        me.callParent(arguments);
    }

});

