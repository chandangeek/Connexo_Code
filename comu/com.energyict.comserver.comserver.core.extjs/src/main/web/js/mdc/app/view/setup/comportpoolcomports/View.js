Ext.define('Mdc.view.setup.comportpoolcomports.View', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comPortPoolsComPortsView',
    itemId: 'comPortPoolsComPortsView',

    requires: [
        'Mdc.view.setup.comportpoolcomports.Grid',
        'Mdc.view.setup.comportpoolcomports.Preview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.comportpool.SubMenu'
    ],

    side: {
        xtype: 'panel',
        ui: 'medium',
        title: Uni.I18n.translate('', 'MDC', 'Communication port pools'),
        width: 350,
        items: [
            {
                xtype: 'comportpoolsubmenu',
                itemId: 'comportpoolsubmenu'
            }
        ]
    },

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('comServerComPorts.communicationPorts', 'MDC', 'Communication ports'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'comPortPoolComPortsGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('comPortPoolPorts.empty.title', 'MDC', 'No communication ports found'),
                        reasons: [
                            Uni.I18n.translate('comPortPoolPorts.empty.list.item1', 'MDC', 'No communication ports are associated to this communication port pool.')
                        ],
                        stepItems: [
                            {
                                action: 'addComPort',
                                text: Uni.I18n.translate('comServerComPorts.add', 'MDC', 'Add communication port')
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'comPortPoolComPortPreview'
                    }
                }
            ]
        }
    ]
});