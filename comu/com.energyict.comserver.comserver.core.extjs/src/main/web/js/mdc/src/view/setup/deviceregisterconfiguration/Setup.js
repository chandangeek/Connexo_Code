Ext.define('Mdc.view.setup.deviceregisterconfiguration.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceRegisterConfigurationSetup',
    itemId: 'deviceRegisterConfigurationSetup',

    mRID: null,

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.deviceregisterconfiguration.Grid',
        'Uni.view.container.PreviewContainer',
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
                            xtype: 'container',
                            itemId: 'previewComponentContainer'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


