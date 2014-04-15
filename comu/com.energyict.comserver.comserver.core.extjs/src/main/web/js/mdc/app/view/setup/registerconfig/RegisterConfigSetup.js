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
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.view.breadcrumb.Trail'
    ],
    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            itemId: 'stepsContainer',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('registerConfig.registerConfigs', 'MDC', 'Register configurations') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'registerConfigTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerConfigGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerConfigPreviewContainer'

                }
            ]}
    ],

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'stepsMenu'
        }/*,
        {
            xtype: 'registerConfigFilter',
            name: 'filter'
        }*/
    ],


    initComponent: function () {
        this.side = [{
            xtype: 'deviceConfigurationMenu',
            itemId: 'stepsMenu',
            deviceTypeId: this.deviceTypeId,
            deviceConfigurationId: this.deviceConfigId,
            toggle: 1
        }];
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


