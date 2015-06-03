Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceProtocolDialectSetup',
    itemId: 'deviceProtocolDialectSetup',

    device: null,
    requires: [
        'Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
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
                        toggleId: 'protocolLink'
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'deviceProtocolDialectsSetupPanel',
                title: Uni.I18n.translate('protocoldialect.protocolDialects', 'MDC', 'Protocol dialects'),
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'protocolDialectsGridContainer',
                        grid: {
                            xtype: 'deviceProtocolDialectsGrid',
                            mRID: encodeURIComponent(me.device.get('mRID'))
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('protocolDialects.empty.title', 'MDC', 'No protocol dialects found'),
                            reasons: [
                                Uni.I18n.translate('protocolDialects.empty.list.item1', 'MDC', 'No protocol dialects have been defined yet.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceProtocolDialectPreview',
                            mRID: me.device.get('mRID')
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});


