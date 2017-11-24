/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.Menu' , {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.reading-types-group-menu',

    router: null,

    title: Uni.I18n.translate('readingtypes.readingTypeGroupName', 'MTR', 'Group Name'),
    objectType: Uni.I18n.translate('readingtypes.readingTypeGroup', 'MTR', 'Reading type group'),

    initComponent: function () {

        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('readingtypes.readingTypeGroupOverview', 'MTR', 'Overview'),
                itemId: 'reading-type-group-overview-link',
                href: me.router.getRoute('administration/readingtypegroups/view').buildUrl()
            },
            {
                text: Uni.I18n.translate('readingtypes.readingTypeGroupReadingTypes', 'MTR', 'Reading Types'),
                itemId: 'reading-type-group-reading-types-link',
                href: me.router.getRoute('administration/readingtypegroups/view').buildUrl()
            }
        ]

        me.callParent(arguments);
    }



});