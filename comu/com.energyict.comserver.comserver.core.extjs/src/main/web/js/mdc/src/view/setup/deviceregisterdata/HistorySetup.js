/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.HistorySetup', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.deviceregisterdata.HistoryFilter',
        'Mdc.view.setup.deviceregisterdata.billing.HistoryGrid',
        'Mdc.view.setup.deviceregisterdata.flags.HistoryGrid',
        'Mdc.view.setup.deviceregisterdata.numerical.HistoryGrid',
        'Mdc.view.setup.deviceregisterdata.text.HistoryGrid',
        'Mdc.view.setup.deviceregisterdata.billing.HistoryPreview',
        'Mdc.view.setup.deviceregisterdata.flags.HistoryPreview',
        'Mdc.view.setup.deviceregisterdata.numerical.HistoryPreview',
        'Mdc.view.setup.deviceregisterdata.text.HistoryPreview'
    ],
    alias: 'widget.device-register-history',
    device: null,
    register: null,
    type: null,
    showFilter: true,

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
                    itemId: 'device-registers-history-container',
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
                        itemId: 'preview-device-registers-history',
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
                    itemId: 'device-register-history-filter',
                    hidden: !me.showFilter
                }
            ]
        };

        me.callParent(arguments);
    }
});