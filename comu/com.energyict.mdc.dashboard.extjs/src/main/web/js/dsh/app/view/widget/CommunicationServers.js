Ext.define('Dsh.view.widget.CommunicationServers', {
    extend: 'Ext.view.View',
    alias: 'widget.communication-servers',
    itemId: 'communication-servers',
    store: 'ComServerInfos',
    itemSelector: 'tr.comserver',
    emptyText: 'No communication servers found',
    tpl: new Ext.XTemplate(
        '<div style="padding: 0 10px; display:inline-block">',
            '<h3>Communication servers</h3>',
            '<table style="margin: 5px 0 10px 0">',
                '<tpl for=".">',
                    '<tpl if="xindex &lt; 4">',
                        '<tr class="comserver" >',
                            '<td>',
                                '<a href="#">{comServerName}</a>',
                            '</td>',
                            '<td style="padding-left: 15px;">',
                                '<tpl if="running">',
                                    '<span style="color: green" class="fa fa-caret-square-o-up"/>',
                                '<tpl else>',
                                    '<span style="color: #d80000" class="fa fa-caret-square-o-down"/>',
                                '</tpl>',
                            '</td>',
                        '</tr>',
                    '</tpl>',
                '</tpl>',
            '</table>',
            '<a href="#">View all communication servers</a>',
        '</div>'
    ),
//    listeners: {
//        itemmouseenter: function (view, record, item, index, e, eOpts) {
//            console.log(arguments);
//            Ext.create('Ext.tip.ToolTip', {
//                target: view.el,
//                delegate: view.itemSelector,
//                trackMouse: true,
////                showDelay: 200,
////                hideDelay: 50,
//                html: record.get('comServerName')
//            });
//        }
//    },
    initComponent: function () {
        this.callParent(arguments);
    }
});