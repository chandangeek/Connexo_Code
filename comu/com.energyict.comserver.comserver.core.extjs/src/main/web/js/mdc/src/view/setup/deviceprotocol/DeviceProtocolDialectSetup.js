/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceProtocolDialectSetup',
    itemId: 'deviceProtocolDialectSetup',

    device: null,
    requires: [
        'Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this;
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'protocolLink'
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceProtocolDialectsSetupPanel',
                title: Uni.I18n.translate('protocoldialect.protocolDialects', 'MDC', 'Protocol dialects'),
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'protocolDialectsGridContainer',
                        grid: {
                            xtype: 'deviceProtocolDialectsGrid',
                            deviceId: encodeURIComponent(me.device.get('name'))
                        },
                        emptyComponent: {
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('protocolDialects.empty', 'MDC', 'No protocol dialects have been defined yet.')
                        },
                        previewComponent: {
                            xtype: 'deviceProtocolDialectPreview',
                            deviceId: me.device.get('name')
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});


