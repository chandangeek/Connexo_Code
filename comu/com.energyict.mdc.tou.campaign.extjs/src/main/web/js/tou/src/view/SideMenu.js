/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.tou-campaign-side-menu',
    router: null,
    title: 'ToU campaign',
    objectType: 'ToU campaign',

    initComponent: function () {
        var me = this;

        me.menuItems = [{
                text: Uni.I18n.translate('tou.campaigns.details', 'TOU', 'Details'),
                itemId: 'tou-campaign-link',
                href: me.router.getRoute('workspace/toucampaigns/toucampaign').buildUrl()
            }, {
                text: Uni.I18n.translate('tou.campaigns.devices', 'TOU', 'Devices'),
                itemId: 'tou-campaign-devices-link',
                href: me.router.getRoute('workspace/toucampaigns/toucampaign/devices').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});