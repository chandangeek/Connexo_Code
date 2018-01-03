/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.Menu' , {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.reading-types-group-menu',

    router: null,

    objectType: Uni.I18n.translate('readingtypes.readingTypeGroup', 'MTR', 'Reading type group'),
    title: Uni.I18n.translate('readingtypes.readingTypeGroupName', 'MTR', 'Group Name'),


    initComponent: function () {
        var me = this;
        me.title = decodeURIComponent(me.router.arguments.aliasName) || Uni.I18n.translate('readingtypes.readingTypeGroupName', 'MTR', 'Group Name');

        me.menuItems = [
            {
                text: Uni.I18n.translate('readingtypes.readingTypeGroupOverview', 'MTR', 'Overview'),
                itemId: 'reading-type-group-overview-link',
                href: me.router.getRoute('administration/readingtypes1/view').buildUrl()
                // href: me.router.getRoute('administration/readingtypegroups/view').buildUrl() // lori set
            },
            {
                text: Uni.I18n.translate('readingtypes.readingTypeGroupReadingTypes', 'MTR', 'Reading types'),
                itemId: 'reading-type-group-reading-types-link',
                href: me.router.getRoute('administration/readingtypes1/readingtypes').buildUrl({aliasName: me.router.arguments.aliasName})
                //href: me.router.getRoute('administration/readingtypegroups/readingtypes').buildUrl({aliasName: me.router.arguments.aliasName})
            }
        ]

        me.callParent(arguments);
    }



});