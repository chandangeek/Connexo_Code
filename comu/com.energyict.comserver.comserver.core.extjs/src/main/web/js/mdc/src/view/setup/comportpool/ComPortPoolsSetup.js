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
            title: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'comPortPoolsGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-comport-pool',
                        title: Uni.I18n.translate('setup.comportpool.ComPortPoolsSetup.NoItemsFoundPanel.title', 'MDC', 'No communication port pools found'),
                        reasons: [
                            Uni.I18n.translate('setup.comportpool.ComPortPoolsSetup.NoItemsFoundPanel.reason1', 'MDC', 'No communication port pools created yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('comPortPool.addComPortPool', 'MDC', 'Add communication port pool'),
                                itemId: 'btn-empty-add-comport-pool-menu',
                                privileges: Mdc.privileges.Communication.admin,
                                menu: {
                                    plain: true,
                                    border: false,
                                    shadow: false,
                                    itemId: 'addComPortPoolMenu',
                                    items: [
                                        {
                                            text: Uni.I18n.translate('comPortPool.inbound', 'MDC', 'Inbound'),
                                            action: 'addInbound',
                                            itemId: 'btn-empty-add-inbound'
                                        },
                                        {
                                            text: Uni.I18n.translate('comPortPool.outbound', 'MDC', 'Outbound'),
                                            action: 'addOutbound',
                                            itemId: 'btn-empty-add-outbound'
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