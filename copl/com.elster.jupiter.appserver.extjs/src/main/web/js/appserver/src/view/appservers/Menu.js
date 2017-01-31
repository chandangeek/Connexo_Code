/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.appservers-menu',

    router: null,

    title: Uni.I18n.translate('general.applicationServer', 'APR', 'Application server'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: me.appServerName,
                itemId: 'apr-overview-link',
                href: me.router.getRoute('administration/appservers/overview').buildUrl({appServerName: me.appServerName})
            },
            {
                text: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                itemId: 'apr-message-services-link',
                href: me.router.getRoute('administration/appservers/overview/messageservices').buildUrl({appServerName: me.appServerName})
            },
            {
                text: Uni.I18n.translate('general.importServices', 'APR', 'Import services'),
                itemId: 'apr-import-services-link',
                href: me.router.getRoute('administration/appservers/overview/importservices').buildUrl({appServerName: me.appServerName})
            },
            {
                text: Uni.I18n.translate('general.webserviceEndpoints', 'APR', 'Web service endpoints'),
                itemId: 'apr-webservices-link',
                href: me.router.getRoute('administration/appservers/overview/webserviceendpoints').buildUrl({appServerName: me.appServerName})
            }
        ];

        me.callParent(arguments);
    }

});

