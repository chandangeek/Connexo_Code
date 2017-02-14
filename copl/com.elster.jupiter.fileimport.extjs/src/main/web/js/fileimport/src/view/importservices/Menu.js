/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.importservices.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.fim-import-service-menu',

    router: null,
    importServiceId: null,
    title: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),
    objectType: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'FIM', 'Details'),
                itemId: 'import-services-view-link',
                href: '#/administration/importservices/' + this.importServiceId
            },
			{
                text: Uni.I18n.translate('general.history', 'FIM', 'History'),
                itemId: 'import-services-view-history-link',
                href: '#/administration/importservices/' + this.importServiceId + '/history'
            }
        ];


        me.callParent(arguments);
    }
});

