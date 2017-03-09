/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.HistorySetup', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.deviceregisterdata.HistoryFilter',
        'Mdc.view.setup.deviceregisterdata.numerical.HistoryGrid',
        'Mdc.view.setup.deviceregisterdata.numerical.HistoryPreview'
    ],
    alias: 'widget.device-register-history',
    device: null,
    register: null,
    type: null,

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
                        toggleId: 'registersLink'
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
                    itemId: 'device-registers-container',
                    grid: {
                        xtype: 'device-registers-history-' + me.type,
                        itemId: 'device-registers-history',
                        registerRecord: me.register,
                        deviceId: me.deviceId,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceRegistersHistory.empty.title', 'MDC', 'No history items found'),
                        reasons: [
                            Uni.I18n.translate('deviceRegistersHistory.empty.list.item1', 'MDC', "The filter hasn't been specified yet."),
                            Uni.I18n.translate('deviceRegistersHistory.empty.list.item2', 'MDC', 'No history items comply with the filter.')
                        ],
                        margins: '16 0 0 0'
                    },
                    previewComponent: {
                        xtype: 'preview-device-registers-history-' + me.type,
                        itemId: 'preview-device-registers-history-' + me.type,
                        channelRecord: me.channel,
                        router: me.router,
                        device: me.device
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'device-register-history-filter',
                    itemId: 'device-register-history-filter'
                }
            ]
        };

        me.callParent(arguments);
    }
});