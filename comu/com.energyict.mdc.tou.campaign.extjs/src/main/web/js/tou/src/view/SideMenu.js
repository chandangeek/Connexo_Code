/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.tou-campaign-side-menu',
    router: null,
    title: 'Tou campaign',
    objectType: 'Tou campaign',

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: 'Details',
                itemId: 'tou-campaign-link',
                href: me.router.getRoute('workspace/toucampaigns/toucampaign').buildUrl()
            },
            {
                text: 'Devices',
                itemId: 'tou-campaign-devices-link',
                href: me.router.getRoute('workspace/toucampaigns/toucampaign/devices').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});