Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSecuritySettingSetup',
    itemId: 'deviceSecuritySettingSetup',

    device: null,

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
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
                            mrid: encodeURIComponent(me.device.get('mRID'))
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
                ]
            };

    }
});


