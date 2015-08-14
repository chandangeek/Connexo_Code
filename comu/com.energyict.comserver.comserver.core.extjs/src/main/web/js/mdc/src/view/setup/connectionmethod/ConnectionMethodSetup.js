Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connectionMethodSetup',
    itemId: 'connectionMethodSetup',
    deviceTypeId: null,
    deviceConfigId: null,

    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
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
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigId
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'connectionMethodSetupPanel',
                title: Uni.I18n.translate('general.connectionMethods', 'MDC', 'Connection methods'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'connectionMethodsGrid',
                            deviceTypeId: this.deviceTypeId,
                            deviceConfigId: this.deviceConfigId
                        },
                        emptyComponent: this.getEmptyComponent(),
                        previewComponent: {
                            xtype: 'connectionMethodPreview'
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
                title: Uni.I18n.translate('connectionMethod.empty.title', 'MDC', 'No connection methods found'),
                reasons: [
                    Uni.I18n.translate('connectionMethod.empty.list.item1', 'MDC', 'No connection methods have been defined yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('connectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        itemId: 'createOutboundConnectionButton',
                        action: 'createOutboundConnectionMethod'
                    },
                    {
                        text: Uni.I18n.translate('connectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method'),
                        itemId: 'createInboundConnectionButton',
                        action: 'createInboundConnectionMethod'
                    }
                ]
            };
        } else {
            return {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('connectionMethod.empty.title', 'MDC', 'No connection methods found'),
                reasons: [
                    Uni.I18n.translate('connectionMethod.empty.list.detailNotAdressableItem1', 'MDC', 'This device configuration is not directly addressable.')
                ]
            };
        }
    }
});


