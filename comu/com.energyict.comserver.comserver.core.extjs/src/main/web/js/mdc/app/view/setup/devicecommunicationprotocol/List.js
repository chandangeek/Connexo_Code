Ext.define('Mdc.view.setup.devicecommunicationprotocol.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.setupDeviceCommunicationProtocols',
    store: 'DeviceCommunicationProtocols',
    itemId: 'devicecommunicationprotocolgrid',
    overflowY: 'auto',
    layout: 'fit',
    initComponent: function () {
        this.columns = [
            {
                text: 'Device Communication Protocols',
                xtype: 'templatecolumn',
                tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                    '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                    '{id} - {name}' +
                    '</caption>' +
                    '<tr>' +
                    '<td>Java class name: </td> ' +
                    '<td>{licensedProtocol.protocolName}</td>' +
                    '</tr>' +
                    '<tr>' +
                    '<td>Device protocol version: </td>' +
                    '<td>{deviceProtocolVersion}</td>' +
                    '</tr>' +
                    '</table>',
                flex: 1
            }
        ];

        this.buttons = [
            {
                text: 'Add',
                action: 'add'
            },
            {
                text: 'Delete',
                action: 'delete'
            }
        ];

        this.callParent(arguments);
    }
});