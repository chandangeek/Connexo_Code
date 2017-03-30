/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.protocolDialectSetup',
    itemId: 'protocolDialectSetup',

    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigId
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'protocolDialectsSetupPanel',
                title: Uni.I18n.translate('protocoldialect.protocolDialects', 'MDC', 'Protocol dialects'),
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'protocolDialectsGridContainer',
                        grid: {
                            xtype: 'protocolDialectsGrid',
                            deviceTypeId: this.deviceTypeId,
                            deviceConfigId: this.deviceConfigId
                        },
                        emptyComponent: {
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('protocolDialects.empty', 'MDC', 'No protocol dialects have been defined yet.')
                        },
                        previewComponent: {
                            xtype: 'protocolDialectPreview',
                            deviceTypeId: this.deviceTypeId,
                            deviceConfigId: this.deviceConfigId
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});


