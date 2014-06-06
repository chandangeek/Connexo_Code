Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationProtocolSetup',
    itemId: 'deviceCommunicationProtocolSetup',
    requires: [
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolGrid',
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolPreview',
        'Uni.view.container.PreviewContainer'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceCommunicationProtocol.protocols', 'MDC', 'Communication protocols'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceCommunicationProtocolGrid'
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
                                        html: '<b>' + Uni.I18n.translate('deviceCommunicationProtocol.empty.title', 'MDC', 'No communication protocols found') + '</b><br>' +
                                            Uni.I18n.translate('deviceCommunicationProtocol.empty.detail', 'MDC', 'There are no communication protocols. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('deviceCommunicationProtocol.empty.list.item1', 'MDC', 'No license.') + '</li></lv><br>'
                                    }
                                ]
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'deviceCommunicationProtocolPreview'
                    }
                }
            ]}
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});


