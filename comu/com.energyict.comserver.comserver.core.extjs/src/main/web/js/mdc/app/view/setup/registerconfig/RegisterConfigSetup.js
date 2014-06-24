Ext.define('Mdc.view.setup.registerconfig.RegisterConfigSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerConfigSetup',
    itemId: 'registerConfigSetup',
    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Mdc.view.setup.registerconfig.RegisterConfigGrid',
        'Mdc.view.setup.registerconfig.RegisterConfigFilter',
        'Mdc.view.setup.registerconfig.RegisterConfigPreview',
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu'
    ],

    content: [
        {
            ui: 'large',
            xtype: 'panel',
            title: Uni.I18n.translate('registerConfig.registerConfigs', 'MDC', 'Register configurations'),
            items: [
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerConfigGridContainer'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerConfigPreviewContainer'

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
                        toggle: 1
                    }
                ]
            }
        ];
        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'registerConfigSetupPanel',
                title: Uni.I18n.translate('registerConfig.registerConfigs', 'MDC', 'Register configurations'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'registerConfigGrid',
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
                                            html: '<h4>' + Uni.I18n.translate('registerConfig.empty.title', 'MDC', 'No register configurations found') + '</h4><br>' +
                                                Uni.I18n.translate('registerConfig.empty.detail', 'MDC', 'There are no register configurations. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('registerConfig.empty.list.item1', 'MDC', 'No register configurations have been added yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('registerConfig.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            text: Uni.I18n.translate('registerConfig.addRegisterConfiguration', 'MDC', 'Add register configuration'),
                                            itemId: 'createRegisterConfigurationButton',
                                            xtype: 'button',
                                            action: 'createRegisterConfig'
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'registerConfigPreview',
                            deviceTypeId: this.deviceTypeId,
                            deviceConfigId: this.deviceConfigId
                        }
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
})
;