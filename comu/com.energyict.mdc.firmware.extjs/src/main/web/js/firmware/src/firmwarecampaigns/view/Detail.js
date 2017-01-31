/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Fwc.firmwarecampaigns.view.SideMenu',
        'Fwc.firmwarecampaigns.view.DetailForm'
    ],
    alias: 'widget.firmware-campaign-detail',
    router: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'firmware-campaign-side-menu',
                        itemId: 'firmware-campaign-side-menu',
                        router: me.router
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'firmware-campaigns-detail-form',
                itemId: 'firmware-campaigns-detail-form',
                title: Uni.I18n.translate('general.overview', 'FWC', 'Overview'),
                ui: 'large',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});