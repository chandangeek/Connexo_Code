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

        me.title = me.record.get('name') || Uni.I18n.translate('webservices.webserviceEndpoint', 'WSS', 'Web service endpoint');

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'WSS', 'Details'),
                itemId: 'webservice-overview-link',
                href: me.router.getRoute('administration/webserviceendpoints/view').buildUrl({endpointId: me.record.get('id')})
            },
            {
                text: Uni.I18n.translate('general.history', 'WSS', 'History'),
                itemId: 'wenservoces-history-link',
                href: me.router
                    .getRoute('administration/webserviceendpoints/view/history')
                    .buildUrl({endpointId: me.record.get('id')})
            },
            {
                text: Uni.I18n.translate('general.endpointStatusHistory', 'WSS', 'Endpoint status history'),
                itemId: 'wenservoces-logs-link',
                href: me.router.getRoute('administration/webserviceendpoints/view/status').buildUrl({endpointId: me.record.get('id')})
            },
        ];

        me.callParent(arguments);
    }

});

