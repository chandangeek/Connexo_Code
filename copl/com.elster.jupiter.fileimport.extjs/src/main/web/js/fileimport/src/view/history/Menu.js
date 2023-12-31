/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.history.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.fim-history-menu',

    router: null,
    showImportService: false,

    title: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),
    objectType: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'FIM', 'Details'),
                hidden: me.showImportService,
                itemId: 'import-service-view-link',
                href: '#/administration/importservices/' + this.importServiceId
            }
        ];

        if (!me.showImportService) {
            me.menuItems.push(
                {
                    text: Uni.I18n.translate('general.history', 'FIM', 'History'),
                    itemId: 'import-service-history-link',
                    href: me.router.getRoute('administration/importservices/importservice/history').buildUrl()
                }
            );
        }


        me.callParent(arguments);
    }
});

