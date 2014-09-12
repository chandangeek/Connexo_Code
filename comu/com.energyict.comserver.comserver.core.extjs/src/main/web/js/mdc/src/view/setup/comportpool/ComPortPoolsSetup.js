Ext.define('Mdc.view.setup.comportpool.ComPortPoolsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comPortPoolsSetup',

    requires: [
        'Mdc.view.setup.comportpool.ComPortPoolsGrid',
        'Mdc.view.setup.comportpool.ComPortPoolPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
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
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('setup.comportpool.ComPortPoolsSetup.NoItemsFoundPanel.title', 'MDC', 'No comuncation port pools found'),
                        reasons: [
                            Uni.I18n.translate('setup.comportpool.ComPortPoolsSetup.NoItemsFoundPanel.reason1', 'MDC', 'No comuncation port pools created yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('comPortPool.addComPortPool', 'MDC', 'Add communication port pool'),
                                menu: {
                                    plain: true,
                                    border: false,
                                    shadow: false,
                                    itemId: 'addComPortPoolMenu',
                                    items: [
                                        {
                                            text: Uni.I18n.translate('comPortPool.inbound', 'MDC', 'Inbound'),
                                            action: 'addInbound'
                                        },
                                        {
                                            text: Uni.I18n.translate('comPortPool.outbound', 'MDC', 'Outbound'),
                                            action: 'addOutbound'
                                        }
                                    ]
                                }
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