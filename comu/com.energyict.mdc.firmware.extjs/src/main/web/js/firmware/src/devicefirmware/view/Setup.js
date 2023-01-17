/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-firmware-setup',

    requires: [
        'Fwc.devicefirmware.view.ActionMenu',
        'Mdc.view.setup.device.DeviceMenu',
        'Fwc.devicefirmware.view.DeviceFirmwareHistoryGrid'
    ],

    router: null,
    device: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [{
                    xtype: 'deviceMenu',
                    router: me.router,
                    device: me.device
                }]
            }
        ];

        me.content = {
            xtype: 'tabpanel',
            ui: 'large',
            title: Uni.I18n.translate('general.firmware', 'FWC', 'Firmware'),
            itemId: 'firmware-TabPanel',
            activeTab: me.activeTab,
            items: [
                {
                    itemId: 'device-firmwares',
                    title: Uni.I18n.translate('general.firmware', 'FWC', 'Firmware'),
                    items: {}
                },
                {
                    itemId: 'device-firmware-history',
                    title: Uni.I18n.translate('general.history', 'FWC', 'History'),
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-history-grid',
                        itemId: 'device-history-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        itemId: 'no-usage-found',
                        text: Uni.I18n.translate('relativeperiod.usage.empty', 'FWC', 'No device firmware history')
                    }
                }
            ]

        };
        me.callParent(arguments);
    }
});
