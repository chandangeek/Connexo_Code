Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConnectionMethodSetup',
    itemId: 'deviceConnectionMethodSetup',

    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
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

        this.content = [
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
                        emptyComponent: this.getEmptyComponent(),
                        previewComponent: {
                            xtype: 'deviceConnectionMethodPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    getEmptyComponent: function () {
        if (this.isDirectlyAddressable) {
            return  {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('deviceconnectionmethod.empty.title', 'MDC', 'No connection methods found'),
                reasons: [
                    Uni.I18n.translate('deviceconnectionmethod.empty.list.item1', 'MDC', 'No connection methods have been defined yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('deviceconnectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'),
                        itemId: 'createDeviceOutboundConnectionButton',
                        action: 'createDeviceOutboundConnectionMethod'
                    },
                    {
                        text: Uni.I18n.translate('deviceconnectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
                        itemId: 'createDeviceInboundConnectionButton',
                        action: 'createDeviceInboundConnectionMethod'
                    }
                ]
            };
        } else {
            return  {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('deviceconnectionmethod.empty.detailNotAdressable', 'MDC', 'No connection methods can be added'),
                reasons: [
                    Uni.I18n.translate('deviceconnectionmethod.empty.list.detailNotAdressableItem1', 'MDC', 'This device configuration is not directly addressable.')
                ]
            };
        }
    }
});


