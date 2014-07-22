Ext.define('Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceRegisterConfigurationSetup',
    itemId: 'deviceRegisterConfigurationSetup',

    mRID: null,

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationGrid',
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationPreview',
        'Uni.view.container.PreviewContainer',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('deviceregisterconfiguration.devices', 'MDC', 'Devices'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        mRID: me.mRID,
                        toggle: 1
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('deviceregisterconfiguration.deviceregisterconfiguration', 'MDC', 'Registers'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceRegisterConfigurationGrid',
                            mRID: me.mRID
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('deviceregisterconfiguration.empty.title', 'MDC', 'No registers found'),
                            reasons: [
                                Uni.I18n.translate('deviceregisterconfiguration.empty.list.item1', 'MDC', 'No registers have been defined yet.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceRegisterConfigurationPreview'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


