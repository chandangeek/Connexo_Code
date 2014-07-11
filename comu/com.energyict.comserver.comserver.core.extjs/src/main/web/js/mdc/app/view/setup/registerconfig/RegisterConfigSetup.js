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
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Mdc.view.setup.registerconfig.RegisterConfigAndRulesPreviewContainer'
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
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('registerConfig.empty.title', 'MDC', 'No register configurations found'),
                            reasons: [
                                Uni.I18n.translate('registerConfig.empty.list.item1', 'MDC', 'No register configurations have been added yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('registerConfig.addRegisterConfiguration', 'MDC', 'Add register configuration'),
                                    itemId: 'createRegisterConfigurationButton',
                                    action: 'createRegisterConfig'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'register-config-and-rules-preview-container',
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