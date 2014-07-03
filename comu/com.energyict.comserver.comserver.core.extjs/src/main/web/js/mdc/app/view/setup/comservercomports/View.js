Ext.define('Mdc.view.setup.comservercomports.View', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comServerComPortsView',
    itemId: 'comServerComPortsView',
    requires: [
        'Mdc.view.setup.comservercomports.Grid',
        'Mdc.view.setup.comservercomports.Preview',
        'Uni.view.container.PreviewContainer',
        'Mdc.view.setup.comservercomports.AddMenu',
        'Mdc.view.setup.comserver.SubMenu'
    ],
    side: {
        xtype: 'panel',
        ui: 'medium',
        title: Uni.I18n.translate('comserver.title.communicationServers', 'MDC', 'Communication servers'),
        width: 300,
        items: [{
            xtype: 'comserversubmenu',
            itemId: 'comserversubmenu'
        }]
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
                        xtype: 'comServerComPortsGrid'
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
                                        html: '<b>' + Uni.I18n.translate('comServerComPorts.empty.title', 'MDC', 'No communication ports found') + '</b><br>' +
                                            Uni.I18n.translate('comServerComPorts.empty.detail', 'MDC', 'There are no communication ports. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('comServerComPorts.empty.list.item1', 'MDC', 'No communication ports are associated to this communication server.') + '</li></lv><br>' +
                                            Uni.I18n.translate('comServerComPorts.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        action: 'addComPort',
                                        text: Uni.I18n.translate('comServerComPorts.add', 'MDC', 'Add communication port'),
                                        menu: {
                                            xtype: 'comServerComPortsAddMenu'
                                        }
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