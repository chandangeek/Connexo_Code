Ext.define('Mdc.view.devicecommunicationprotocol.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceCommunicationProtocolBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Mdc.view.devicecommunicationprotocol.List'
    ],

    items: [
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
                    html: '<h1>Device Communication Protocols</h1>'
                },
                {
                    xtype: 'deviceCommunicationProtocolList'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});