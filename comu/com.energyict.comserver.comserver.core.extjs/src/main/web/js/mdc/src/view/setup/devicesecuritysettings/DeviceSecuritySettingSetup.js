/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSecuritySettingSetup',
    itemId: 'deviceSecuritySettingSetup',

    device: null,

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
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
                        toggleId: 'securitySettingLink'
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceSecuritySettingSetupPanel',
                title: Uni.I18n.translate('devicesecuritysetting.securitySettings', 'MDC', 'Security settings'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceSecuritySettingGrid',
                            deviceId: encodeURIComponent(me.device.get('name'))
                        },
                        emptyComponent: this.getEmptyComponent(),
                        previewComponent: {
                            xtype: 'deviceSecuritySettingPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    getEmptyComponent: function () {
        return  {
            xtype: 'uni-form-empty-message',
            text: Uni.I18n.translate('devicesecuritysetting.empty', 'MDC', 'No security settings have been defined yet.')
        };
    }
});


