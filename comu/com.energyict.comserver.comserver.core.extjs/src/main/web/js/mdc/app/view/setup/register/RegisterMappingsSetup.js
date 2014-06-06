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
        'Mdc.view.setup.devicetype.DeviceTypeMenu'
    ],

   /* content: [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),
                items: [
                    {
                        xtype: 'container',
                        items: [],
                        itemId: 'registerMappingGridContainer'
                    },
                    {
                        xtype: 'container',
                        items: [],
                        itemId: 'registerMappingPreviewContainer'
                    }
                ]
            }
    ],*/


/*,
         {
         xtype: 'registerMappingFilter',
         name: 'filter'
         }*/

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
        this.content= [
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
                                            html: '<h4>' + Uni.I18n.translate('registerMapping.empty.title', 'MDC', 'No register type found') + '</h4><br>' +
                                                Uni.I18n.translate('registerMapping.empty.detail', 'MDC', 'There are no register types. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('registerMapping.empty.list.item1', 'MDC', 'No register types have been defined yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('registerMapping.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            text: Uni.I18n.translate('registerMapping.addRegisterMapping', 'MDC', 'Add register types'),
                                            itemId: 'addRegisterMappingBtn',
                                            xtype: 'button',
                                            action: 'addRegisterMapping'
                                        }
                                    ]
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


