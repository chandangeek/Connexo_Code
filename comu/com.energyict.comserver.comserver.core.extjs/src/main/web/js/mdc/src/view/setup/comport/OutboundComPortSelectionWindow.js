Ext.define('Mdc.view.setup.comport.OutboundComPortSelectionWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.outboundComPortSelectionWindow',
    title: 'Select a comport',
    layout: 'fit',
    width: 350,
    height: 400,
    modal: true,
    constrain: true,
    autoShow: true,
    requires: [
        'Ext.grid.*'
    ],

    activeIndex: 0,
    party: null,

    initComponent: function () {
        var comports = Ext.create('Mdc.store.ComPorts');
        comports.filter('direction','outbound');
        this.items = [
            {
                xtype: 'grid',
                itemId: 'outboundComPortSelectionGrid',
                autoScroll: true,
                store: comports,
                columns: [
                    {
                        text: Uni.I18n.translate('comports.comports','MDC','ComPorts'),
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
                ]
            }
        ];

        this.buttons = [
            {
                text: Uni.I18n.translate('general.select','MDC','Select'),
                action: 'select'
            },
            {
                text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                action: 'cancel'
            }
        ];

        this.callParent(arguments);
    }
});
