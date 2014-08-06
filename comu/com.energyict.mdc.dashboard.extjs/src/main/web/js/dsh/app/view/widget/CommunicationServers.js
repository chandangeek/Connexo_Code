Ext.define('Dsh.view.widget.CommunicationServers', {
    extend: 'Ext.container.Container',
    alias: 'widget.communication-servers',
    itemId: 'communication-servers',
    style: {
        paddingRight: '50px',
        borderRight: '3px dotted grey'
    },
    items: [
        {
            xtype: 'dataview',
            store: 'CommunicationServerInfos',
            itemSelector: 'tr.comserver',
            emptyText: 'No communication servers found', //TODO: localize
            tpl: new Ext.XTemplate(
                '<div>',
                    '<h3>Communication servers</h3>', //TODO: localize
                    '<table style="margin: 5px 0 10px 0">',
                        '<tpl for=".">',
                            '<tpl if="xindex &lt; 4">',
                                '<tr class="comserver" >',
                                    '<td>',
                                        '<a href="#">{comServerName}</a>',
                                    '</td>',
                                    '<td style="padding-left: 15px;">',
                                        '<tpl if="running">',
                                            '<span style="color: green" class="fa fa-caret-square-o-up"/>', //TODO: set real img
                                        '<tpl else>',
                                            '<span style="color: #d80000" class="fa fa-caret-square-o-down"/>', //TODO: set real img
                                        '</tpl>',
                                    '</td>',
                                '</tr>',
                            '</tpl>',
                        '</tpl>',
                    '</table>',
                    '<a href="#">View all communication servers</a>', //TODO: localize
                '</div>'
            ),
            listeners: {
                afterrender: function (view) {
                    view.store.load();
                },
                itemmouseenter: function (view, record) {
                    Ext.create('Ext.tip.ToolTip', {
                        itemId: 'comservers-tooltip',
                        target: view.el,
                        delegate: view.itemSelector,
                        trackMouse: true,
                        showDelay: 50,
                        hideDelay: 0,
                        html:
                            '<table>' +
                                '<tr>' +
                                    '<td style="text-align: right; padding-right: 10px; white-space: nowrap">Communication server</td>' +
                                    '<td>' + record.get('comServerName') + '</td>' +
                                '</tr>' +
                                '<tr>' +
                                    '<td style="text-align: right; padding-right: 10px; white-space: nowrap">Online/remote</td>' +
                                    '<td>' + record.get('comServerType').charAt(0).toUpperCase() + record.get('comServerType').slice(1).toLocaleLowerCase() + '</td>' +
                                '</tr>' +
                                '<tr>' +
                                    '<td style="text-align: right; padding-right: 10px; white-space: nowrap">Down since</td>' +
                                    '<td>' + Ext.util.Format.date(new Date(), 'D M j, Y G:i') + '</td>' +
                                '</tr>' +
                            '</table>'
                    });
                }
            }
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});