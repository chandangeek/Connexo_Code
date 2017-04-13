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

    hasSecuritySuites: undefined,

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
                title: Uni.I18n.translate('general.securitySettings', 'MDC', 'Security settings'),

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
                        },
                        onLoad: function (store, records, successful) {
                            var securitySuite = false;
                            if(store.count() > 0) {
                                securitySuite = records[0].get('securitySuite')['id'] !== -1;
                            }
                            me.down('#deviceSecuritySettingSetupPanel preview-container deviceSecuritySettingGrid').updateColumns(securitySuite);
                            me.down('#deviceSecuritySettingSetupPanel preview-container deviceSecuritySettingPreview').updateColumns(securitySuite);
                            Uni.view.container.PreviewContainer.prototype.onLoad.call(this, store, records, successful);
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


