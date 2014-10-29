Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSecuritySettingSetup',
    itemId: 'deviceSecuritySettingSetup',

    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        mRID: this.mrid,
                        toggle: 9
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
                            mrid: this.mrid
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
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('devicesecuritysetting.empty.title', 'MDC', 'No security settings found'),
                reasons: [
                    Uni.I18n.translate('devicesecuritysetting.empty.list.item1', 'MDC', 'No security settings have been defined yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('devicesecuritysetting.addSecuritySetting', 'MDC', 'Add security setting'),
                        itemId: 'createDeviceSecuritySettingButton',
                        action: 'createDeviceSecuritySetting'
                    }
                ]
            };

    }
});


