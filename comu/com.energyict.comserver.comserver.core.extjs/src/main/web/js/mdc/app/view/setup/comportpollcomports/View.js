Ext.define('Mdc.view.setup.comportpollcomports.View', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comPortPoolsComPortsView',
    itemId: 'comPortPoolsComPortsView',
    requires: [
        'Mdc.view.setup.comportpollcomports.Grid',
        'Mdc.view.setup.comservercomports.Preview',
        'Uni.view.container.PreviewContainer'
    ],
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
                                        name: 'emptyComponent',
                                        html: '<b>' + Uni.I18n.translate('comPortPoolPorts.empty.title', 'MDC', 'No communication ports found') + '</b><br>' +
                                            Uni.I18n.translate('comPortPoolPorts.empty.detail', 'MDC', 'There are no communication ports. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('comPortPoolPorts.empty.list.item1', 'MDC', 'No communication ports are associated to this communication port pool.') + '</li></lv><br>' +
                                            Uni.I18n.translate('comPortPoolPorts.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        action: 'addComPort',
                                        text: Uni.I18n.translate('comServerComPorts.add', 'MDC', 'Add communication port')
                                    }
                                ]
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'comServerComPortPreview'
                    }
                }
            ]
        }
    ]
});