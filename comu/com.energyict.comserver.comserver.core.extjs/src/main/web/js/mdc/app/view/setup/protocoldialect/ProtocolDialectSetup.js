Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.protocolDialectSetup',
    itemId: 'protocolDialectSetup',
    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.view.container.PreviewContainer'
    ],

    side: [
        {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'navigationSubMenu',
                    itemId: 'stepsMenu'
                }
            ]
        }
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
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'left'
                            },
                            minHeight: 20,
                            items: [
                                {
                                    xtype: 'image',
                                    margin: '0 10 0 0',
                                    src: "../mdc/resources/images/information.png",
                                    height: 20,
                                    width: 20
                                },
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'component',
                                            html: '<h4>' + Uni.I18n.translate('protocolDialects.empty.title', 'MDC', 'No protocol dialects found') + '</h4><br>' +
                                                Uni.I18n.translate('protocolDialects.empty.detail', 'MDC', 'There are no protocol dialects. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('protocolDialects.empty.list.item1', 'MDC', 'No protocol dialects have been defined yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('protocolDialects.empty.steps', 'MDC', 'Possible steps:')
                                        }
                                    ]
                                }
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


