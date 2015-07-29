Ext.define('Dsh.view.widget.Overview', {
    extend: 'Ext.panel.Panel',
    requires: [ 'Dsh.view.widget.common.Bar' ],
    alias: 'widget.overview',
    itemId: 'overview',
    ui: 'medium',
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    defaults: {flex: 1},
    total: 0,
    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            style: {padding: 0},
            items: [
                {
                    xtype: 'container',
                    itemId: 'title',
                    html: '<h2>' + Uni.I18n.translate('overview.widget.overview.title', 'DSH', 'Overview') + '</h2>'
                }
            ]
        }
    ],
    bindStore: function (store) {
        var me = this;
        me.removeAll(true);
        store.each(function (item, idx) {
            item.counters().sort([
                {property: 'count', direction: 'DESC'},
                {property: 'displayName', direction: 'ASC'}
            ]);

            item.counters().each(function (record) {
                if (record.get('id')) {
                    var filter = me.router.filter.getWriteData(true, true);
                    filter[item.get('alias')] = record.get('id');
                    var href = me.router.getRoute('workspace/' + me.parent + '/details').buildUrl(null, filter);
                    record.set('href', href);
                }
            });

            var panel = Ext.create('Ext.panel.Panel', {
                style: {padding: '20px', marginRight: !(idx % 2) ? '20px' : 0},
                ui: 'tile',
                tbar: {
                    xtype: 'container',
                    itemId: 'title',
                    html: '<h3>' + Ext.String.htmlEncode(item.get('displayName')) + '</h3>'
                },
                items: {
                    xtype: 'dataview',
                    itemId: item.get('alias') + '-dataview',
                    itemSelector: 'tbody.item',
                    total: item.get('total') || me.total,
                    store: item.counters(),
                    tpl: '<table width="100%">' +
                        '<tpl for=".">' +
                        '<tbody class="item item-{#}">' +
                        '<tr>' +
                        '<td width="50%">' +
                        '<div style="overflow: hidden; text-overflow: ellipsis; padding-right: 20px">' +
                        '<tpl if="href">' +
                        '<a href="{href}">{displayName}</a>' +
                        '<tpl else>' +
                        '{displayName}' +
                        '</tpl>' +
                        '</div>' +
                        '</td>' +
                        '<td width="50%" id="bar-{#}"></td>' +
                        '</tr>' +
                        '</tbody>' +
                        '</tpl>' +
                        '</table>',
                    listeners: {
                        refresh: function (view) {
                            Ext.each(view.getNodes(), function (node, index) {
                                var record = view.getRecord(node),
                                    pos = index + 1;

                                var bar = Ext.widget('bar', {
                                    limit: record.get('count'),
                                    total: view.total,
                                    count: record.get('count'),
                                    label: record.get('count')
                                }).render(view.getEl().down('#bar-' + pos));
                            });
                        }
                    }
                }
            });

            me.add(panel);
        });
        me.mixins.bindable.bindStore.apply(this, arguments);
    }
});