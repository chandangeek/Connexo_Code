Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConnectionMethodSetup',
    itemId: 'deviceConnectionMethodSetup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this;

        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'connectionMethodsLink'
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceConnectionMethodSetupPanel',
                title: Uni.I18n.translate('general.connectionMethods', 'MDC', 'Connection methods'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceConnectionMethodsGrid',
                            deviceId: encodeURIComponent(me.device.get('name'))
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
                        privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                        itemId: 'createDeviceOutboundConnectionButton',
                        action: 'createDeviceOutboundConnectionMethod',
                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions
                    },
                    {
                        text: Uni.I18n.translate('deviceconnectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
                        privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                        itemId: 'createDeviceInboundConnectionButton',
                        action: 'createDeviceInboundConnectionMethod',
                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions
                    }
                ]
            };
        } else {
            return  {
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('deviceconnectionmethod.empty.list.detailNotAdressableItem1', 'MDC', 'This device configuration is not directly addressable.')
            };
        }
    }
});


