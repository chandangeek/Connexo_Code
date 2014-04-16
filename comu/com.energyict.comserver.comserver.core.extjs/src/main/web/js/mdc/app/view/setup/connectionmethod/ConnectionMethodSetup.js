Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connectionMethodSetup',
    itemId: 'connectionMethodSetup',
    deviceTypeId: null,
    deviceConfigId: null,
    requires: [
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
                    html: '<h1>' + Uni.I18n.translate('connectionMethod.connectionMethods', 'MDC', 'Connection methods') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'connectionMethodTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'connectionMethodsGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'connectionMethodPreviewContainer'

                }
            ]}
    ],

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'stepsMenu'
        }
    ],


    initComponent: function () {
        this.side = [{
            xtype: 'deviceConfigurationMenu',
            itemId: 'stepsMenu',
            deviceTypeId: this.deviceTypeId,
            deviceConfigurationId: this.deviceConfigId,
            toggle: 4
        }];
        this.callParent(arguments);
        this.down('#connectionMethodsGridContainer').add(
            {
                xtype: 'connectionMethodsGrid',
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId
            }
        );
        this.down('#connectionMethodPreviewContainer').add(
            {
                xtype: 'connectionMethodPreview',
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId
            }
        );
    }
});


