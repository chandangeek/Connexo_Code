/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comserver.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.comserversidemenu',
    title: Uni.I18n.translate('general.comServer', 'MDC', 'Communication server'),
    objectType: Uni.I18n.translate('general.comServer', 'MDC', 'Communication server'),
    initComponent: function () {
        var me = this,
            serverId = me.serverId;
        var offlineServer = location.hash &&location.hash.indexOf("offline") >=0;
        me.objectType = offlineServer
            ? Uni.I18n.translate('general.mobileComServer', 'MDC', 'Mobile communication server')
            : Uni.I18n.translate('general.comServer', 'MDC', 'Communication server');
        var routePart = offlineServer ? "offlinecomservers" : "comservers";
        me.menuItems = [
            {
                text: Uni.I18n.translate('comserver.sidemenu.details', 'MDC', 'Details'),
                itemId: 'comserverLink',
                href: '#/administration/'+ routePart + '/' + serverId
            },
            {
                text: Uni.I18n.translate('comserver.sidemenu.comports', 'MDC', 'Communication ports'),
                itemId: 'commportsLink',
                href: '#/administration/'+ routePart + '/' + serverId + '/comports'
            }
        ];
        me.callParent(arguments);
    }
});