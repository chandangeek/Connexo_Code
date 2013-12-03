Ext.define('Mdc.view.setup.comport.PoolOutboundComPorts', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.poolOutboundComPorts',
    layout: 'auto',
    itemId: 'pooloutboundcomportgrid',

    initComponent: function () {
        this.columns = [
            {
                text: 'OutboundComPorts',
                xtype: 'templatecolumn',
                tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                    '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                    '{name} - {comPortType} - <tpl if="active==true"{active}><span style="color:lightgreen">active</span><tpl else><span style="color:#ff0000">not active</span></tpl>' +
                    '</caption>' +
                    '<tr>' +
                    '<td>description: </td>' +
                    '<td>{description}</td>' +
                    '<td>modificationDate:</td>' +
                    '<td>{modificationDate:date("d/m/Y - h:i:s")}</td>'+
                    '</tr>'+
                    '</table>',
                flex:1
            }
        ];

        this.buttons = [
            {
                text: 'Add',
                action: 'add',
                style: {
                    background: '#404040 ',
                    borderColor: '#282828 '
                }
            },
            {
                text: 'Delete',
                action: 'delete',
                style: {
                    background: '#404040 ',
                    borderColor: '#282828 '
                }
            }
        ];

        this.callParent();
    }
});
