/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usagepointgroups-menu',
    xtype: 'usagepointgroups-menu',
    usagePointGroup: null,
    objectType: Uni.I18n.translate('general.usagePointGroup', 'IMT', 'Usage point group'),

    initComponent: function () {
        var me = this;
        me.title = me.usagePointGroup.get('name') || Uni.I18n.translate('general.usagePointGroup', 'IMT', 'Usage point group');
        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
                itemId: 'usagepointgroups-view-link',
                href: me.router.getRoute('usagepoints/usagepointgroups/view').buildUrl({usagePointGroupId: me.usagePointGroup.getId()})
            }
        ];

        me.callParent(arguments);
    }
});


