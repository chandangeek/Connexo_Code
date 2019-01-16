/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Tou.view.SideMenu',
        'Tou.view.DetailForm'
    ],
    alias: 'widget.tou-campaign-detail',
    router: null,

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
                xtype: 'tou-campaigns-detail-form',
                itemId: 'tou-detail-form',
                title: 'Details',
                ui: 'large',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});