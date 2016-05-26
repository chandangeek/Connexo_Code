Ext.define('Mdc.view.setup.deviceregisterconfiguration.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceRegisterConfigurationSetup',
    itemId: 'deviceRegisterConfigurationSetup',
    device: null,

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.deviceregisterconfiguration.Grid',
        'Uni.view.container.PreviewContainer',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'registersLink'
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceRegisterConfigurationGrid',
                            mRID: encodeURIComponent(me.device.get('mRID'))
                        },
                        emptyComponent: {
                            xtype: 'uni-form-empty-message',
                            itemId: 'ctr-no-device-register-config',
                            text: Uni.I18n.translate('deviceregisterconfiguration.empty', 'MDC', 'No registers have been defined yet.')
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


