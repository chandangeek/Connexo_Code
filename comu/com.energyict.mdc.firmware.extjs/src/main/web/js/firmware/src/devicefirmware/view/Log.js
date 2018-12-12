/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.view.Log', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-firmware-log',

    requires: [
        'Fwc.devicefirmware.view.LogGrid',
        'Fwc.devicefirmware.view.LogPreview',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    router: null,
    device: null,
    title: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'deviceMenu',
            router: me.router,
            device: me.device
        };

        me.content = {
            ui: 'large',
            title: me.title,
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-firmware-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('deviceFirmware.empty', 'FWC', 'No log lines have been added yet')
                    },
                    previewComponent: {
                        xtype: 'device-firmware-log-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});