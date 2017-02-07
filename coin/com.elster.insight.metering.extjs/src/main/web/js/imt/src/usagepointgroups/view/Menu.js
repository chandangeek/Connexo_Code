/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usagepointgroups-menu',
    xtype: 'usagepointgroups-menu',
    usagePointGroup: null,
    title: Uni.I18n.translate('general.usagePointGroups', 'IMT', 'Usage point groups'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: me.usagePointGroup.get('name'),
                itemId: 'usagepointgroups-view-link',
                href: me.router.getRoute('usagepoints/usagepointgroups/view').buildUrl({usagePointGroupId: me.usagePointGroup.getId()})
            }
        ];

        me.callParent(arguments);
    }
});


