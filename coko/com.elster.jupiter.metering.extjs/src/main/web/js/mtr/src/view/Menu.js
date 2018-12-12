/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.reading-types-group-menu',

    router: null,

    objectType: Uni.I18n.translate('readingtypes.readingTypeSet', 'MTR', 'Reading type set'),
    title: Uni.I18n.translate('readingtypes.readingTypeGroupName', 'MTR', 'Set Name'),

    initComponent: function () {
        var me = this;
        me.title = decodeURIComponent(me.router.arguments.aliasName) || Uni.I18n.translate('readingtypes.readingTypeGroupName', 'MTR', 'Set Name');

        me.items = [
            {
                text: Uni.I18n.translate('readingtypes.readingTypeGroupOverview', 'MTR', 'Overview'),
                itemId: 'reading-type-group-overview-link',
                href: me.router.getRoute('administration/readingtypes/view').buildUrl()
            },
            {
                text: Uni.I18n.translate('readingtypes.readingTypeGroupReadingTypes', 'MTR', 'Reading types'),
                itemId: 'reading-type-group-reading-types-link',
                href: me.router.getRoute('administration/readingtypes/readingtypes').buildUrl({aliasName: me.router.arguments.aliasName})
            }
        ]

        me.callParent(arguments);
    }
});