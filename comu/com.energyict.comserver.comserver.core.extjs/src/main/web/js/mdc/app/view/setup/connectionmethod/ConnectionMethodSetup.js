Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connectionMethodSetup',
    itemId: 'connectionMethodSetup',
    deviceTypeId: null,
    deviceConfigId: null,
    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.view.container.EmptyGridContainer'
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
                        toggle: 4
                    }
                ]
            }
        ];
        this.content =  [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'connectionMethodSetupPanel',
                title: Uni.I18n.translate('connectionMethod.connectionMethods', 'MDC', 'Connection methods'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'connectionMethodsGrid',
                            deviceTypeId: this.deviceTypeId,
                            deviceConfigId: this.deviceConfigId
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
                                            html: '<h4>' + Uni.I18n.translate('connectionMethod.empty.title', 'MDC', 'No connection methods found') + '</h4><br>' +
                                                Uni.I18n.translate('connectionMethod.empty.detail', 'MDC', 'There are no connection methods. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('connectionMethod.empty.list.item1', 'MDC', 'No connection methods have been defined yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('connectionMethod.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            text: Uni.I18n.translate('connectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'),
                                            itemId: 'createOutboundConnectionButton',
                                            xtype: 'button',                                            
                                            action: 'createOutboundConnectionMethod',
                                            margin: '0 5 0 0'
                                        },
                                        {
                                            text: Uni.I18n.translate('connectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
                                            itemId: 'createInboundConnectionButton',
                                            xtype: 'button',
                                            action: 'createInboundConnectionMethod'
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'connectionMethodPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});


