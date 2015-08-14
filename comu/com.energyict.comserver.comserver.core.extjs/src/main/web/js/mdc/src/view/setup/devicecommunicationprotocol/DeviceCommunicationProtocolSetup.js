Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationProtocolSetup',
    itemId: 'deviceCommunicationProtocolSetup',

    requires: [
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolGrid',
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.deviceComProtocols', 'MDC', 'Communication protocols'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceCommunicationProtocolGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceCommunicationProtocol.empty.title', 'MDC', 'No communication protocols found'),
                        reasons: [
                            Uni.I18n.translate('deviceCommunicationProtocol.empty.list.item1', 'MDC', 'No license.')
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


