Ext.define('Mdc.view.setup.devicecommunicationprotocol.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.setupDeviceCommunicationProtocols',
    store: Ext.create('Mdc.store.DeviceCommunicationProtocols'),
    itemId: 'setupDeviceCommunicationProtocols',
    overflowY: 'auto',
    layout: 'fit',
    requires: ['Ext.ux.PreviewPlugin'],
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
                    '<td>Java class name: &nbsp; &nbsp;{javaClassName}</td>' +
                    '</tr>' +
                    '</table>',
                flex: 1
            }
        ];
        this.callParent(arguments);
    }
});