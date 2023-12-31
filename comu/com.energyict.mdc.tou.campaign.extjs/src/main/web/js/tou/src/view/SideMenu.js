/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.tou-campaign-side-menu',
    router: null,
    title: Uni.I18n.translate('tou.campaigns.touCampaign', 'TOU', 'ToU calendar campaign'),
    objectType: Uni.I18n.translate('tou.campaigns.touCampaign', 'TOU', 'ToU calendar campaign'),

    initComponent: function () {
        var me = this;

        me.menuItems = [{
                text: Uni.I18n.translate('general.details', 'TOU', 'Details'),
                itemId: 'tou-campaign-link',
                href: me.router.getRoute('workspace/toucampaigns/toucampaign').buildUrl()
            }, {
                text: Uni.I18n.translate('general.devices', 'TOU', 'Devices'),
                itemId: 'tou-campaign-devices-link',
                href: me.router.getRoute('workspace/toucampaigns/toucampaign/devices').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});