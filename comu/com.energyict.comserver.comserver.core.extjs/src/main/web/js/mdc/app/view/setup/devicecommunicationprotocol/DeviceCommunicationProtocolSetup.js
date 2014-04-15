Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationProtocolSetup',
    itemId: 'deviceCommunicationProtocolSetup',
    requires: [
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolGrid',
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolPreview',
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolFilter'
    ],

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('deviceCommunicationProtocol.protocols','MDC','Protocols') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'protocolTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'deviceCommunicationProtocolGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'deviceCommunicationProtocolPreview'
                }
            ]}
    ],

 /*   side: [
           {
               xtype: 'deviceCommunicationProtocolFilter',
               name: 'filter'
           }
       ],
*/

    initComponent: function () {
        this.callParent(arguments);
        this.down('#deviceCommunicationProtocolGridContainer').add(
            {
                xtype: 'deviceCommunicationProtocolGrid'
            }
        );
    }
});


