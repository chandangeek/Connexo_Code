/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.Devices', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Tou.view.SideMenu',
        'Tou.view.DevicesGrid',
        'Tou.view.DevicesStatusFilter'
    ],
    alias: 'widget.tou-campaign-devices',
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
                        xtype: 'tou-campaign-side-menu',
                        itemId: 'tou-campaign-side-menu',
                        router: me.router
                    }
                ]
            }
        ];

        me.content = [
            {
                itemId: 'tou-campaigns-devices-panel',
                title: 'Devices',
                ui: 'large',
                items: [
                    {
                        xtype: 'tou-view-devicesStatusFilter',
                        itemId: 'tou-campaign-filter'
                    }
                    ,
                    {
                        xtype: 'emptygridcontainer',
                        itemId: 'tou-campaigns-devices-empty-grid-container',
                        grid: {
                            xtype: 'tou-campaign-devices-grid',
                            itemId: 'tou-campaign-devices-grid',
                            router: me.router,
                            campaignIsOngoing: me.campaignIsOngoing
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'tou-campaigns-devices-empty-component',
                            title: 'No devices',
                            reasons: [
                                'No devices comply with the filter.'
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});