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

    content: [
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
/*,
         {
         xtype: 'registerMappingFilter',
         name: 'filter'
         }*/
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'deviceTypeMenu',
                itemId: 'stepsMenu',
                deviceTypeId: this.deviceTypeId,
                toggle: 1
            }
        ];
        this.callParent(arguments);
        this.down('#registerMappingGridContainer').add(
            {
                xtype: 'registerMappingsGrid',
                deviceTypeId: this.deviceTypeId
            }
        );
        this.down('#registerMappingPreviewContainer').add(
            {
                xtype: 'registerMappingPreview',
                deviceTypeId: this.deviceTypeId
            }
        );
    }
});


