Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.protocolDialectSetup',
    autoScroll: true,
    itemId: 'protocolDialectSetup',
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
                    html: '<h1>' + Uni.I18n.translate('protocoldialect.prtocolDialects', 'MDC', 'Protocol dialects') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'protocolDialectTitle'
                },
//                {
//                    xtype: 'container',
//                    items: [],
//                    itemId: 'connectionMethodsGridContainer'
//                },
//                {
//                    xtype: 'component',
//                    height: 25
//                },
//                {
//                    xtype: 'container',
//                    items: [],
//                    itemId: 'connectionMethodPreviewContainer'
//
//                }
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
            toggle: 5
        }];
        this.callParent(arguments);
//        this.down('#connectionMethodsGridContainer').add(
//            {
//                xtype: 'connectionMethodsGrid',
//                deviceTypeId: this.deviceTypeId,
//                deviceConfigId: this.deviceConfigId
//            }
//        );
//        this.down('#connectionMethodPreviewContainer').add(
//            {
//                xtype: 'connectionMethodPreview',
//                deviceTypeId: this.deviceTypeId,
//                deviceConfigId: this.deviceConfigId
//            }
//        );
    }
});


