Ext.define('Mdc.view.setup.comport.PoolInboundComPorts', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.poolInboundComPorts',
    layout: 'auto',
    itemId: 'poolinboundcomportgrid',

    initComponent: function () {
        this.columns = [
            {
                text: Uni.I18n.translate('comports.inboundComports','MDC','Inbound comports'),
                xtype: 'templatecolumn',
                tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                    '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                    '{name} - {comPortType} - <tpl if="active==true"{active}><span style="color:lightgreen">active</span><tpl else><span style="color:#ff0000">not active</span></tpl>' +
                    '</caption>' +
                    '<tr>' +
                    '<td width="20%">description: </td>' +
                    '<td width="30%">{description}</td>' +
                    '<td width="20%">modificationDate:</td>' +
                    '<td width="30%">{modificationDate:date("d/m/Y - h:i:s")}</td>'+
                    '</tr>'+
                    '</table>',
                flex:1
            }
        ];

        this.callParent();
    }
});

