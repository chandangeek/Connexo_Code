Ext.define('Dsh.view.widget.CommunicationServers', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.communication-servers',
    initComponent: function () {
        var router = this.router;
        this.items = [
            {
                xtype: 'dataview',
                store: 'CommunicationServerInfos',
                itemSelector: 'tr.comserver',
                emptyText: Uni.I18n.translate('overview.widget.communicationServers.noServersFound', 'DSH', 'No communication servers found'),
                tooltipTpl: new Ext.XTemplate(
                    '<table>',
                    '<tr>',
                        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.communicationServer', 'DSH', 'Communication server') + '</td>',
                    '<td>{comServerName}</td>',
                    '</tr>',
                    '<tr>',
                        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.onlineRemote', 'DSH', 'Online/Remote') + '</td>',
                    '<td>{comServerType}</td>',
                    '</tr>',
                    '<tpl if="blockedSince">',
                    '<tr>',
                        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.downSince', 'DSH', 'Down since') + '</td>',
                    '<td></td>{[Ext.util.Format.date(new Date(values.blockedSince), "D M j, Y G:i")]}',
                    '</tr>',
                    '</tpl>',
                    '</table>'
                ),
                tooltip:  Ext.create('Ext.tip.ToolTip'),
                tpl: new Ext.XTemplate(
                    '<div>',
                        '<h3>' + Uni.I18n.translate('overview.widget.communicationServers.header', 'DSH', 'Down communication servers') + '</h3>',
                        '<table  style="margin: 5px 0 10px 0">',
                            '<tpl for=".">',
                                '<tpl if="!running && xindex &lt; 4">',
                                    '<tr class="comserver" >',
                                        '<td>',
                                            '<a href="' + router.getRoute('administration/comservers/detail/overview').buildUrl({id: '{comServerId}'}) + '">{comServerName}</a>',
                                        '</td>',
                                        '<td style="padding-left: 15px;"><img src="/apps/uni/build/resources/images/shared/icon-info-small.png" /></td>',
                                    '</tr>',
                                '</tpl>',
                            '</tpl>',
                        '</table>',
                        '<a href="' + router.getRoute('administration/comservers').buildUrl() + '">' + Uni.I18n.translate('overview.widget.communicationServers.viewAll', 'DSH', 'View all communication servers') + '</a>',
                    '</div>'
                ),
                listeners: {
                    itemmouseenter: function(view, record, item) {
                        view.tooltip.update(view.tooltipTpl.apply(record.getData()));
                        view.tooltip.setTarget(item.getElementsByTagName('img')[0]);
                    }
                }
            }
        ];
        this.callParent(arguments);
    },
    reload: function(){
        this.down('dataview').store.load();
    }
});