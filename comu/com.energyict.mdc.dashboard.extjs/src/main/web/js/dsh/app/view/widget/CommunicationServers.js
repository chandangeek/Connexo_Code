Ext.define('Dsh.view.widget.CommunicationServers', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communication-servers',
    itemId: 'communication-servers',
    initComponent: function () {
        var router = this.router;
        this.items = [
            {
                xtype: 'dataview',
                store: 'CommunicationServerInfos',
                itemSelector: 'tr.comserver',
                emptyText: Uni.I18n.translate('overview.widget.communicationServers.noServersFound', 'DSH', 'No communication servers found'),
                tpl: new Ext.XTemplate(
                    '<div>',
                        '<h3>' + Uni.I18n.translate('overview.widget.communicationServers.title', 'DSH', 'Communication servers') + '</h3>',
                        '<table style="margin: 5px 0 10px 0">',
                            '<tpl for=".">',
                                '<tpl if="xindex &lt; 4">',
                                    '<tr class="comserver" >',
                                        '<td>',
                                            '<a href="' + router.getRoute('administration/comservers/detail/overview').buildUrl({id: '{comServerName}'}) + '">{comServerName}</a>',
                                        '</td>',
                                        '<td style="padding-left: 15px;">',
                                            '<tpl if="running">',
                                                '<span style="color: #00aa00" class="fa fa-caret-square-o-up"/>', //TODO: set real img
                                            '<tpl else>',
                                                '<span style="color: #d80000" class="fa fa-caret-square-o-down"/>', //TODO: set real img
                                            '</tpl>',
                                        '</td>',
                                    '</tr>',
                                '</tpl>',
                            '</tpl>',
                        '</table>',
                        '<a href="' + router.getRoute('administration/comservers').buildUrl() + '">' + Uni.I18n.translate('overview.widget.communicationServers.viewAll', 'DSH', 'View all communication servers') + '</a>',
                    '</div>'
                ),
                listeners: {
                    afterrender: function (view) {
                        view.store.load();
                    },
                    itemmouseenter: function (view, record) {
                        Ext.create('Ext.tip.ToolTip', {
                            itemId: 'communication-servers-tooltip',
                            target: view.el,
                            delegate: view.itemSelector,
                            trackMouse: true,
                            showDelay: 50,
                            hideDelay: 0,
                            html: '<table>' +
                                '<tr>' +
                                '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.communicationServer', 'DSH', 'Communication server') + '</td>' +
                                '<td>' + record.get('comServerName') + '</td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.onlineRemote', 'DSH', 'Online/remote') + '</td>' +
                                '<td>' + record.get('comServerType').charAt(0).toUpperCase() + record.get('comServerType').slice(1).toLocaleLowerCase() + '</td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.downSince', 'DSH', 'Down since') + '</td>' +
                                '<td>' + Ext.util.Format.date(new Date(), 'D M j, Y G:i') + '</td>' +
                                '</tr>' +
                                '</table>',
                            listeners: {
                                hide: function (tooltip) {
                                    tooltip.destroy();
                                }
                            }
                        });
                    }
                }
            }
        ];
        this.callParent(arguments);
    }
});