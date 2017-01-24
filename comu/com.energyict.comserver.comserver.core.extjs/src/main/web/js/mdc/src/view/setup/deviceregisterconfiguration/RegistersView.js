Ext.define('Mdc.view.setup.deviceregisterconfiguration.RegistersView', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceRegistersView',
    device: null,
    router: null,

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.Grid',
        'Mdc.view.setup.deviceregisterconfiguration.RegistersTopFilter',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'panel',
                ui: 'large',
                dockedItems: [
                    {
                        dock: 'top',
                        xtype: 'mdc-registers-overview-topfilter',
                        deviceId: me.device.get('name')
                    }
                ]
            },
            {
                xtype: 'panel',
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceRegisterConfigurationGrid',
                            deviceId: encodeURIComponent(me.device.get('name')),
                            showDataLoggerSlaveColumn: !Ext.isEmpty(me.device.get('isDataLogger')) && me.device.get('isDataLogger'),
                            router: me.router
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

