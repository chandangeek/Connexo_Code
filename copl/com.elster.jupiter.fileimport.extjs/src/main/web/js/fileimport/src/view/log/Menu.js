/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.log.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.fim-log-menu',
    router: null,

    initComponent: function () {
        var me = this;

        if (me.router.arguments.importServiceId) {
            me.menuItems = [
                {
                    text: Uni.I18n.translate('general.overview', 'FIM', 'Overview'),
                    itemId: 'import-service-log-view-link',
                    href: '#/administration/importservices/' + me.router.arguments.importServiceId
                }
            ];

            me.menuItems.push(
                {
                    text: Uni.I18n.translate('general.log', 'FIM', 'Log'),
                    itemId: 'history-log-link',
                    href: me.router.getRoute('administration/importservices/importservice/history/occurrence').buildUrl()
                }
            );
        }
        me.callParent(arguments);
    }
});

