Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConnectionMethodSetup',
    itemId: 'deviceConnectionMethodSetup',
    deviceTypeId: null,
    deviceConfigId: null,
    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.EmptyGridContainer'
    ],

    initComponent: function () {
        debugger;
        this.side = [

            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        mRID: this.mrid,
                        toggle: 4
                    }
                ]
            }
        ];
        this.content =  [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceConnectionMethodSetupPanel',
                title: Uni.I18n.translate('deviceconnectionmethod.connectionMethods', 'MDC', 'Connection methods'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceConnectionMethodsGrid',
                            mrid: this.mrid
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
                                            html: '<h4>' + Uni.I18n.translate('deviceconnectionmethod.empty.title', 'MDC', 'No connection methods found') + '</h4><br>' +
                                                Uni.I18n.translate('deviceconnectionmethod.empty.detail', 'MDC', 'There are no connection methods. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('deviceconnectionmethod.empty.list.item1', 'MDC', 'No connection methods have been defined yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('deviceconnectionmethod.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            text: Uni.I18n.translate('deviceconnectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'),
                                            itemId: 'createDeviceOutboundConnectionButton',
                                            xtype: 'button',
                                            action: 'createDeviceOutboundConnectionMethod',
                                            margin: '0 5 0 0'
                                        },
                                        {
                                            text: Uni.I18n.translate('deviceconnectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
                                            itemId: 'createDeviceInboundConnectionButton',
                                            xtype: 'button',
                                            action: 'createDeviceInboundConnectionMethod'
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceConnectionMethodPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});


