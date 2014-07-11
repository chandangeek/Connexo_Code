Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceProtocolDialectSetup',
    itemId: 'deviceProtocolDialectSetup',

    mRID: null,

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    side: [
        {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'navigationSubMenu',
                    itemId: 'stepsMenu'
                }
            ]
        }
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
                        mRID: this.mRID,
                        toggle: 5
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
                            mRID: this.mRID
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
                            mRID: this.mRID
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});


