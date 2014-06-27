Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceProtocolDialectSetup',
    itemId: 'deviceProtocolDialectSetup',
    mRID: null,

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu',
        'Uni.view.container.PreviewContainer'
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
                                            html: '<h4>' + Uni.I18n.translate('protocolDialects.empty.title', 'MDC', 'No protocol dialects found') + '</h4><br>' +
                                                Uni.I18n.translate('protocolDialects.empty.detail', 'MDC', 'There are no protocol dialects. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('protocolDialects.empty.list.item1', 'MDC', 'No protocol dialects have been defined yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('protocolDialects.empty.steps', 'MDC', 'Possible steps:')
                                        }
                                    ]
                                }
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


