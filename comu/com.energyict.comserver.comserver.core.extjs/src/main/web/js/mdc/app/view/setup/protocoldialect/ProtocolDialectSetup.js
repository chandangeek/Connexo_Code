Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.protocolDialectSetup',
    itemId: 'protocolDialectSetup',
    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu'
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
                    html: '<h1>' + Uni.I18n.translate('protocoldialect.protocolDialects', 'MDC', 'Protocol dialects') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'protocolDialectTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'protocolDialectsGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'protocolDialectPreviewContainer'
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
        this.side = [
            {
                xtype: 'deviceConfigurationMenu',
                itemId: 'stepsMenu',
                deviceTypeId: this.deviceTypeId,
                deviceConfigurationId: this.deviceConfigId,
                toggle: 6
            }
        ];
        this.callParent(arguments);
        this.down('#protocolDialectsGridContainer').add(
            {
                xtype: 'protocolDialectsGrid',
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId
            }
        );
        this.down('#protocolDialectPreviewContainer').add(
            {
                xtype: 'protocolDialectPreview',
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId
            }
        );
    }
});


