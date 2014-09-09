Ext.define('Mdc.view.setup.register.RegisterMappingsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerMappingsSetup',
    itemId: 'registerMappingSetup',

    deviceTypeId: null,

    requires: [
        'Mdc.view.setup.register.RegisterMappingsGrid',
        'Mdc.view.setup.register.RegisterMappingsFilter',
        'Mdc.view.setup.register.RegisterMappingPreview',
        'Uni.view.navigation.SubMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.devicetype.DeviceTypeMenu'
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        toggle: 1
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'registerMappingsSetupPanel',
                title: Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'registerMappingsGrid',
                            deviceTypeId: this.deviceTypeId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('registerMapping.empty.title', 'MDC', 'No register type found'),
                            reasons: [
                                Uni.I18n.translate('registerMapping.empty.list.item1', 'MDC', 'No register types have been defined yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('registerMapping.addRegisterMapping', 'MDC', 'Add register types'),
                                    itemId: 'addRegisterMappingBtn',
                                    action: 'addRegisterMapping'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'registerMappingPreview',
                            deviceTypeId: this.deviceTypeId
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});


