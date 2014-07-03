Ext.define('Mdc.view.setup.comserver.ComServersSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comServersSetup',
    requires: [
        'Mdc.view.setup.comserver.ComServersGrid',
        'Mdc.view.setup.comserver.ComServerPreview'
    ],
    content: {
        ui: 'large',
        title: Uni.I18n.translate('comserver.title.communicationServers', 'MDC', 'Communication servers'),
        items: [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'comServersGrid'
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
                                    html: '<b>' + Uni.I18n.translate('comserver.empty.title', 'MDC', 'No comunication servers found') + '</b><br>' +
                                        Uni.I18n.translate('comserver.empty.detail', 'MDC', 'There are no comunication servers. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                        Uni.I18n.translate('comserver.empty.list.item1', 'MDC', 'No comunication servers created yet') + '</li></lv><br>' +
                                        Uni.I18n.translate('comserver.empty.steps', 'MDC', 'Possible steps:')
                                },
                                {
                                    xtype: 'button',
                                    margin: '10 0 0 0',
                                    text: Uni.I18n.translate('deviceType.add', 'MDC', 'Add online comunication server'),
                                    action: 'addOnlineComServer',
                                    href: '#/administration/comservers/add/online'
                                }
                            ]
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'comServerPreview'
                }
            }
        ]}

});

