/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.Menu' , {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.reading-types-group-menu',

    router: null,

    title: Uni.I18n.translate('readingtypes.readingTypeGroup', 'MTR', 'Reading type group'),
    objectType: Uni.I18n.translate('readingtypes.readingTypeGroup', 'MTR', 'Reading type group'),

    initComponent: function () {

        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('readingtypes.readingTypeGroupOverview', 'MTR', 'Overview'),
                itemId: 'reading-type-group-overview-link',
                href: me.router.getRoute('administration/relativeperiods/relativeperiod').buildUrl()
            },
            {
                text: Uni.I18n.translate('readingtypes.readingTypeGroupReadingTypes', 'MTR', 'Reading Types'),
                itemId: 'reading-type-group-reading-types-link',
                href: me.router.getRoute('administration/relativeperiods/relativeperiod/usage').buildUrl()
            }
        ]

        me.callParent(arguments);
    }



});