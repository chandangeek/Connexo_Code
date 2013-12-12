Ext.define('Mdc.view.setup.comportpool.ComPortPools', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.setupComPortPools',

    requires: [
        'Mdc.store.ComPortPools'
    ],
    overflowY: 'auto',
    layout: 'fit',
    itemId: 'comportpoolgrid',

    store: 'ComPortPools',
    initComponent: function () {
        this.columns = [
            {
                text: 'Communication port pools',
                xtype: 'templatecolumn',
                tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                    '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                        '{name} - <tpl if="active==true"{active}><span style="color:lightgreen">active</span><tpl else><span style="color:#ff0000">not active</span></tpl>' +
                    '</caption>' +
                    '<tr>' +
                        '<td width="25%">direction: </td>' +
                        '<td width="25%">{direction}</td>' +
                        '<td width="25%">type: </td>' +
                        '<td width="25%">{type}</td>' +
                    '</tr>'+
                    '</table>',
                flex:1
            }
        ];

        this.buttons = [
            {
                text: 'Add',
                action: 'add',
                menu: [{
                    text: 'Inbound'
                },{
                    text: 'Outbound'
                }]
            },
            {
                text: 'Delete',
                action: 'delete'
            }
        ];

        this.callParent();
    }
});