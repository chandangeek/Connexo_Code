/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.CommunicationServers', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communication-servers',
    store: 'CommunicationServerInfos',
    ui: 'tile',
    title: ' ',
    header: {
        ui: 'small'
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
                                        '<td style="padding-left: 15px;">' +
                                            '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;"' +
                                               ' data-qtitle="{children.length} {title}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}"></span></td>',
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
                                        '<td style="padding-left: 15px;">' +
                                            '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;"' +
                                            ' data-qtitle="{title}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}"></span></td>',
                                    '</tr>',
                                '</tpl>',
                            '</tpl>',
                        '</tbody>',
                    '</tpl>',
                '</table>'
            )
        }
    ],

    bbar: {
        xtype: 'container',
        itemId: 'dsh-manage-link-container'
    },

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
            linkContainer = me.down('#dsh-manage-link-container'),
            elm = me.down('#servers-dataview'),
            store = Ext.getStore(me.store);

        me.setLoading();
        store.load(function () {
            var title = '<h3>'
                + Ext.String.format(Uni.I18n.translate('overview.widget.communicationServers.header', 'DSH', 'Active communication servers ({0})'), store.count())
                + '</h3>';
            me.setTitle(title);

            var groups = store.getGroups().map(function (item) {
                switch(item.name){
                    case 'blocked' :
                        item.title = Uni.I18n.translate('overview.widget.communicationServers.blocked', 'DSH', 'blocked');
                        break;
                    case 'inactive':
                        item.title = Uni.I18n.translate('overview.widget.communicationServers.inactive', 'DSH', 'inactive');
                        break;
                    case 'running':
                        item.title = Uni.I18n.translate('overview.widget.communicationServers.running', 'DSH', 'running');
                        break;
                    case 'stopped':
                        item.title = Uni.I18n.translate('overview.widget.communicationServers.stopped', 'DSH', 'stopped');
                        break;
                }
                item.expand = (item.name === 'blocked' && item.children && item.children.length < 5);
                var html = '';
                if (item.children) {
                    item.children = item.children.map(function (server) {
                        var data = server.getData();
                        data.title = data.comServerName + ' ';
                        switch(item.name){
                            case 'blocked' :
                                data.title += Uni.I18n.translate('overview.widget.communicationServers.blocked', 'DSH', 'blocked');
                                break;
                            case 'inactive':
                                data.title += Uni.I18n.translate('overview.widget.communicationServers.inactive', 'DSH', 'inactive');
                                break;
                            case 'running':
                                data.title += Uni.I18n.translate('overview.widget.communicationServers.running', 'DSH', 'running');
                                break;
                            case 'stopped':
                                data.title += Uni.I18n.translate('overview.widget.communicationServers.stopped', 'DSH', 'stopped');
                                break;
                        }
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

            linkContainer.removeAll();
            linkContainer.add(
                {
                    xtype: 'button',
                    itemId: 'lnk-view-all-communication-servers',
                    ui: 'link',
                    text: Uni.I18n.translate('overview.widget.communicationServers.manageLinkText', 'DSH', 'Manage communication servers'),
                    href: typeof me.router.getRoute('administration/comservers') !== 'undefined'
                        ? me.router.getRoute('administration/comservers').buildUrl() : ''
                }
            );

            me.setLoading(false);
        });
    }
});