/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.Devices', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Fwc.firmwarecampaigns.view.SideMenu',
        'Fwc.firmwarecampaigns.view.DevicesGrid',
        'Fwc.firmwarecampaigns.view.DevicesStatusFilter'
    ],
    alias: 'widget.firmware-campaign-devices',
    router: null,
    deviceType: null,
    campaignIsOngoing: null,

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
                itemId: 'firmware-campaigns-devices-panel',
                title: Uni.I18n.translate('general.devices', 'FWC', 'Devices'),
                ui: 'large',
                items: [
                    {
                        xtype: 'fwc-firmwarecampaigns-view-devicesStatusFilter',
                        itemId: 'firmware-campaign-filter'
                    }
                    ,
                    {
                        xtype: 'emptygridcontainer',
                        itemId: 'firmware-campaigns-devices-empty-grid-container',
                        grid: {
                            xtype: 'firmware-campaign-devices-grid',
                            itemId: 'firmware-campaign-devices-grid',
                            router: me.router,
                            campaignIsOngoing: me.campaignIsOngoing
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'firmware-campaigns-devices-empty-component',
                            title: Uni.I18n.translate('firmware.campaigns.devices.empty.title', 'FWC', 'No devices'),
                            reasons: [
                                Uni.I18n.translate('firmware.campaigns.devices.empty.list.item1', 'FWC', 'Selected device group doesn\'t contain devices of device type \'{0}\'.', [me.deviceType.localizedValue]),
                                Uni.I18n.translate('firmware.campaigns.devices.empty.list.item2', 'FWC', 'No devices comply with the filter.')
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});