/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.webservices-menu',

    router: null,
    record: null,


    initComponent: function () {
        var me = this;

        me.title = me.record.get('name');

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'WSS', 'Details'),
                itemId: 'webservice-overview-link',
                href: me.router.getRoute('administration/webserviceendpoints/view').buildUrl({endpointId: me.record.get('id')})
            },
            {
                text: Uni.I18n.translate('general.logging', 'WSS', 'Logging'),
                itemId: 'wenservoces-logs-link',
                href: me.router.getRoute('administration/webserviceendpoints/view/logs').buildUrl({endpointId: me.record.get('id')})
            }

        ];

        me.callParent(arguments);
    }

});

