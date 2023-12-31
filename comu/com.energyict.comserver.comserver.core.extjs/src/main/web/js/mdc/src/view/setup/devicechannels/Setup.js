/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelsSetup',
    itemId: 'deviceLoadProfileChannelsSetup',
    router: null,
    device: null,
    autoScroll : true,
    minWidth: 1350,
    hasSapAttributes: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.devicechannels.Grid',
        'Mdc.view.setup.devicechannels.Preview',
        'Mdc.view.setup.devicechannels.ChannelsTopFilter'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'channelsLink'
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            layout: 'fit',
            title: Uni.I18n.translate('general.channels', 'MDC', 'Channels'),
            items: [
                {
                    xtype: 'preview-container',
                    itemId: 'device-load-profile-channels-preview-container',
                    grid: {
                        xtype: 'deviceLoadProfileChannelsGrid',
                        device: me.device,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('devicechannels.empty.title', 'MDC', 'No channels found'),
                        reasons: [
                            Uni.I18n.translate('devicechannels.empty.list.item1', 'MDC', 'No channels have been defined yet.'),
                            Uni.I18n.translate('devicechannels.empty.list.item2', 'MDC', 'No channels comply with the filter.')
                        ],
                        margins: '16 0 0 0'
                    },
                    previewComponent: {
                        xtype: 'deviceLoadProfileChannelsPreview',
                        router: me.router,
                        device: me.device
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'mdc-view-setup-devicechannels-channelstopfilter',
                    hasSapAttributes: me.hasSapAttributes
                }
            ]
        };

        me.callParent(arguments);
    }
});