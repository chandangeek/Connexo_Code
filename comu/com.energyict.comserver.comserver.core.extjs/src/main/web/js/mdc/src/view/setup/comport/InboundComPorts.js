Ext.define('Mdc.view.setup.comport.InboundComPorts', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.inboundComPorts',
    layout: 'auto',
    itemId: 'inboundcomportgrid',

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
                    '<td width="25%">description: </td>' +
                    '<td width="25%">{description}</td>' +
                    '<td width="25%">modificationDate:</td>' +
                    '<td width="25%">{modificationDate:date("d/m/Y - h:i:s")}</td>'+
                    '</tr>'+
                    '</table>',
                flex:1
            }
        ];

        this.buttons = [
            {
                text: Uni.I18n.translate('general.add','MDC','Add'),
                action: 'add',
                style: {
                    background: '#404040 ',
                    borderColor: '#282828 '
                }
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
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

