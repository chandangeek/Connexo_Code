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
         xtype: 'registerConfigFilter',
         name: 'filter'
         }*/
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceConfigurationMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigId,
                        toggle: 1
                    }
                ]
            }
        ];
        this.callParent(arguments);
        this.down('#registerConfigGridContainer').add(
            {
                xtype: 'registerConfigGrid',
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId
            }
        );
        this.down('#registerConfigPreviewContainer').add(
            {
                xtype: 'registerConfigPreview',
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId
            }
        );
    }
});