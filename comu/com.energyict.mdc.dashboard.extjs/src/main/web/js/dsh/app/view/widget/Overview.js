Ext.define('Dsh.view.widget.Overview', {
    extend: 'Ext.panel.Panel',
    requires: [ 'Dsh.view.widget.common.Bar' ],
    alias: 'widget.overview',
    itemId: 'overview',
    title: Uni.I18n.translate('overview.widget.overview.title', 'DSH', 'Overview'),
    ui: 'medium',
    style: {
        paddingTop: 0
    },
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    defaults: {
        flex: 1,
        style: {
            paddingRight: '20px'
        }
    },
    total: 0,

    bindStore: function (store) {
        var me = this;
        store.each(function (item, idx) {
            var panel = Ext.create('Ext.panel.Panel', {
                tbar: {
                    xtype: 'container',
                    itemId: 'title',
                    html: '<h3>' + item.get('displayName') + '</h3>'
                },
                items: {
                    xtype: 'dataview',
                    itemId: item.get('alias') + '-dataview',
                    itemSelector: 'tbody.item',
                    total: item.get('total') || me.total,
                    store: item.counters(),
                    tpl:
                        '<table>' +
                            '<tpl for=".">' +
                                '<tbody class="item item-{#}">' +
                                '<tr>' +
                                    '<td>' +
                                        '<div style="width: 200px; overflow: hidden; text-overflow: ellipsis; padding-right: 20px">' +
                                            '<a href="#{id}">{displayName}</a>' +
                                        '</div>' +
                                    '</td>' +
                                    '<td width="100%" id="bar-{#}"></td>' +
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
                                    total: item.get('total') || view.total,
                                    count: record.get('count'),
                                    label: record.get('count')
                                }).render(view.getEl().down('#bar-' + pos));
                                var href = me.router.getRoute('workspace/datacommunication/' + me.parent).buildUrl(null, {filter: [
                                    { property: item.get('alias'), value: record.get('id') }
                                ]});
                                view.getEl().down('.item-' + pos + ' > tr > td > div > a').set({ href: href });
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