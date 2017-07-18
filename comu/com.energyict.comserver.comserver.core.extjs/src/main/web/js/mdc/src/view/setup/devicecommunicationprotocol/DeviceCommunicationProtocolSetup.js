/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationProtocolSetup',
    itemId: 'deviceCommunicationProtocolSetup',

    requires: [
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolGrid',
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
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
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('deviceCommunicationProtocol.empty.list.item1', 'MDC', 'No license.')
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


