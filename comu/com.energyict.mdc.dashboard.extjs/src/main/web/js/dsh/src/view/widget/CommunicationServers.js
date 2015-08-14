Ext.define('Dsh.view.widget.CommunicationServers', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communication-servers',
    store: 'CommunicationServerInfos',
    ui: 'tile',

    tbar: {
        xtype: 'container',
        itemId: 'connection-summary-title-panel'
    },

    items: [
        {
            xtype: 'dataview',
            itemId: 'servers-dataview',
            itemSelector: 'tbody.comserver',
            emptyText: Uni.I18n.translate('overview.widget.communicationServers.noServersFound', 'DSH', 'No communication servers found'),
            tpl: new Ext.XTemplate(
                '<table  style="margin: 5px 0 10px 0">',
                    '<tpl for=".">',
                        '<tbody class="comserver">',
                            '<tpl if="!values.expand">',
                                '<tpl if="children">',
                                    '<tr>',
                                        '<td style="padding-right: 5px;"><img src="/apps/dsh/resources/images/widget/{name}.png" /></td>',
                                        '<td>{children.length} {title}</td>',
                                        '<td style="padding-left: 15px;"><img data-qtitle="{children.length} {title}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" src="/apps/sky/resources/images/shared/icon-info-small.png" /></td>',
                                    '</tr>',
                                '<tpl else>',
                                    '<tr>',
                                        '<td style="padding-right: 5px;"><img src="/apps/dsh/resources/images/widget/{name}.png" /></td>',
                                        '<td>{title}</td>',
                                        '<td style="padding-left: 15px;"></td>',
                                    '</tr>',
                                '</tpl>',
                            '<tpl else>',
                                '<tpl for="values.children">',
                                    '<tr id="{comServerId}">',
                                        '<td style="padding-right: 5px;"><img src="/apps/dsh/resources/images/widget/{[parent.name]}.png" /></td>',
                                        '<td><a href="{href}">{title}</a></td>',
                                        '<td style="padding-left: 15px;"><img data-qtitle="{title}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" src="/apps/sky/resources/images/shared/icon-info-small.png" /></td>',
                                    '</tr>',
                                '</tpl>',
                            '</tpl>',
                        '</tbody>',
                    '</tpl>',
                '</table>'
            )
        },
        {
            xtype: 'container',
            itemId: 'target-container',
            layout: 'vbox',
            style: {
                marginRight: '20px'
            },
            items: [
            ]
        }
    ],

    serverTpl: new Ext.XTemplate(
        '<table>',
            '<tr>',
                '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('general.name', 'DSH', 'Name') + '</td>',
                '<td>{comServerName}</td>',
            '</tr>',
            '<tr>',
                '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('general.type', 'DSH', 'Type') + '</td>',
                '<td>{comServerType}</td>',
            '</tr>',
            '<tpl if="blockedSince">',
                '<tr>',
                    '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.downSince', 'DSH', 'Not responding since') + '</td>',
                    '<td>{[Ext.util.Format.date(new Date(values.blockedSince), "D M j, Y G:i")]}</td>',
                '</tr>',
            '</tpl>',
        '</table>'
    ),

    reload: function () {
        var me = this,
            targetContainer = me.down('#target-container'),
            elm = me.down('#servers-dataview'),
            store = Ext.getStore(me.store);

        me.setLoading();
        targetContainer.removeAll();
        store.load(function () {
            var title = '<h3>' + Uni.I18n.translatePlural('overview.widget.communicationServers.header', store.count(), 'DSH', 'Active communication servers ({0})') + '</h3>';
            if(me.down('#connection-summary-title-panel')){
                me.down('#connection-summary-title-panel').update(title);
            }

            var groups = store.getGroups().map(function (item) {
                item.title = Uni.I18n.translate('overview.widget.communicationServers.title.' + item.name, 'DSH', item.name);
                item.expand = (item.name === 'blocked' && item.children && item.children.length < 5);
                var html = '';
                if (item.children) {
                    item.children = item.children.map(function (server) {
                        var data = server.getData();
                        data.title = data.comServerName + ' ' + Uni.I18n.translate('overview.widget.communicationServers.status.' + item.name, 'DSH', item.name);
                        data.href = me.router.getRoute('administration/comservers/detail/overview').buildUrl({id: data.comServerId});
                        data.tooltip = me.serverTpl.apply(data);
                        html += data.tooltip;
                        return data;
                    });
                }
                item.tooltip = html;

                return item;
            });

            elm.bindStore(Ext.create('Ext.data.Store', {
                fields: ['children', 'name', 'title', 'tooltip', 'expand'],
                data: groups
            }));

            targetContainer.add(
                {
                    xtype: 'button',
                    itemId: 'lnk-view-all-communication-servers',
                    ui: 'link',
                    text: Uni.I18n.translate('general.viewAll', 'DSH', 'View all'),
                    href: typeof me.router.getRoute('administration/comservers') !== 'undefined'
                        ? me.router.getRoute('administration/comservers').buildUrl() : ''
                }
            );

            me.setLoading(false);
        });
    }
});