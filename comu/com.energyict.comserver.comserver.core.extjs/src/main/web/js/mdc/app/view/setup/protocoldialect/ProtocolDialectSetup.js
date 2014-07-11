Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.protocolDialectSetup',
    itemId: 'protocolDialectSetup',

    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
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
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigId,
                        toggle: 6
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
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('protocolDialects.empty.title', 'MDC', 'No protocol dialects found'),
                            reasons: [
                                Uni.I18n.translate('protocolDialects.empty.list.item1', 'MDC', 'No protocol dialects have been defined yet.')
                            ]
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


