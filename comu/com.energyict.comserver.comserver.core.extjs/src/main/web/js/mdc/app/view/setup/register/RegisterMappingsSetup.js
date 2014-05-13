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
            xtype: 'container',
            itemId: 'stepsContainer',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types') + '</h1>',
                    itemId: 'registerTypeTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerMappingGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerMappingPreviewContainer'
                }
            ]}
    ],

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'stepsMenu'
        }/*,
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


