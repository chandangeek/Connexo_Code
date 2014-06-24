Ext.define('Mdc.view.setup.comportpool.ComPortPoolsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comPortPoolsSetup',
    requires: [
        'Mdc.view.setup.comportpool.ComPortPoolsGrid',
        'Mdc.view.setup.comportpool.ComPortPoolPreview'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('', 'MDC', 'Communication port pools'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'comPortPoolsGrid'
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
                                src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: '<b>' + Uni.I18n.translate('', 'MDC', 'No comuncation port pools found') + '</b><br>' +
                                            Uni.I18n.translate('', 'MDC', 'There are no comuncation port pools. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('', 'MDC', 'No comuncation port pools created yet') + '</li></lv><br>' +
                                            Uni.I18n.translate('', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('comportpool.addComPortPool', 'MDC', 'Add communication port pool'),
                                        menu: {
                                            plain: true,
                                            border: false,
                                            shadow: false,
                                            itemId: 'addComPortPoolMenu',
                                            items: [
                                                {
                                                    text: Uni.I18n.translate('comportpool.inbound', 'MDC', 'Inbound'),
                                                    action: 'addInbound'
                                                },
                                                {
                                                    text: Uni.I18n.translate('comportpool.outbound', 'MDC', 'Outbound'),
                                                    action: 'addOutbound'
                                                }
                                            ]
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'comPortPoolPreview'
                    }
                }
            ]
        }
    ]
});