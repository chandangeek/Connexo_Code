/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.firmware-campaign-side-menu',
    router: null,
    title: Uni.I18n.translate('firmware.campaigns.firmwareCampaign', 'FWC', 'Firmware campaign'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('firmware.campaigns.firmwareCampaign', 'FWC', 'Firmware campaign'),
                itemId: 'firmware-campaign-link',
                href: me.router.getRoute('workspace/firmwarecampaigns/firmwarecampaign').buildUrl()
            },
            {
                text: Uni.I18n.translate('general.devices', 'FWC', 'Devices'),
                itemId: 'firmware-campaign-devices-link',
                href: me.router.getRoute('workspace/firmwarecampaigns/firmwarecampaign/devices').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});