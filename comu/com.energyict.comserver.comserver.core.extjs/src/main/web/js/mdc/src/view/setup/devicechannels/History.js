/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.History', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.property.HistoryGrid',
        'Mdc.view.setup.devicechannels.HistoryPreview',
        'Mdc.view.setup.devicechannels.HistoryFilter',
        'Mdc.store.HistoryChannels'
    ],
    alias: 'widget.device-channels-history',
    device: null,
    returnLink: null,

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
            title: Uni.I18n.translate('general.deviceChannelsHistory.title', 'MDC', 'History'),
            items: [
                {
                    xtype: 'preview-container',
                    itemId: 'device-channels-history-preview-container',
                    grid: {
                        xtype: 'device-channels-history-grid',
                        channelRecord: me.channel,
                        store: 'Mdc.store.HistoryChannels',
                        deviceId: me.deviceId,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceChannelsHistory.empty.title', 'MDC', 'No history items found'),
                        reasons: [
                            Uni.I18n.translate('deviceChannelsHistory.empty.list.item1', 'MDC', "The filter hasn't been specified yet."),
                            Uni.I18n.translate('deviceChannelsHistory.empty.list.item2', 'MDC', 'No history items comply with the filter.')
                        ],
                        margins: '16 0 0 0'
                    },
                    previewComponent: {
                        xtype: 'deviceLoadProfileHistoryChannelDataPreview',
                        itemId: 'deviceLoadProfileHistoryChannelDataPreview',
                        channelRecord: me.channel,
                        router: me.router,
                        device: me.device
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'device-channels-history-filter'
                }
            ]
        };

        me.callParent(arguments);
    }
});