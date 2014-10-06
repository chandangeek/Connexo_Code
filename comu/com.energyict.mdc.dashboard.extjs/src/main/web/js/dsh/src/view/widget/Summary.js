Ext.define('Dsh.view.widget.Summary', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    requires: [ 'Dsh.view.widget.common.Bar' ],
    alias: 'widget.summary',
    itemId: 'summary',
    wTitle: Uni.I18n.translate('overview.widget.summary.title', 'DSH', 'Summary'),
    initComponent: function () {
        var me = this;
        this.items = [
            {
                html: '<h3>' + me.wTitle + '</h3>',
                itemId: 'connection-summary-title-panel'
            },
            {
                xtype: 'dataview',
                itemId: 'summary-dataview',
                itemSelector: 'tbody.item',
                cls: 'summary',
                total: 0,
                tpl: '<table>' +
                    '<tpl for=".">' +
                    '<tbody class="item item-{#}">' +
                    '{% var parentIndex = xindex; %}' +
                    '<tr>' +
                    '<td class="label">' +
                    '<a href="#{alias}">{displayName}</a>' +
                    '</td>' +
                    '<td width="100%" id="bar-{[parentIndex]}" class="bar-{[parentIndex]}"></td>' +
                    '</tr>' +
                    '<tpl for="counters">' +
                    '<tr class="child">' +
                    '<td class="label">{displayName}</td>' +
                    '<td width="100%" id="bar-{[parentIndex]}-{#}" class="bar-{[parentIndex]}"></td>' +
                    '</tr>' +
                    '</tpl>' +
                    '</tbody>' +
                    '</tpl>' +
                    '</table>',
                listeners: {
                    refresh: function (view) {
                        Ext.each(view.getNodes(), function (node, index) {
                            var record = view.getRecord(node),
                                pos = index + 1;

                            if (record.counters()) {
                                record.counters().each(function (data, idx) {
                                    var bar = Ext.widget('bar', {
                                        limit: record.get('count'),
                                        total: view.total,
                                        count: data.get('count'),
                                        label: !record.get('count') ? 0 : Math.round(!view.total ? 0 : data.get('count') * 100 / record.get('count')) + '% (' + data.get('count') + ')'
                                    });
                                    bar.render(view.getEl().down('#bar-' + pos + '-' + (idx + 1)));
                                });
                            }

                            var bar = Ext.widget('bar', {
                                limit: view.total,
                                total: view.total,
                                count: record.get('count'),
                                label: Math.round(!view.total ? 0 : record.get('count') * 100 / view.total) + '% (' + record.get('count') + ')'
                            });
                            bar.render(view.getEl().down('#bar-' + pos));

                            var filter = {};
                            filter[view.record.get('alias')] = record.get('id');
                            var href = me.router.getRoute('workspace/' + me.parent + '/details').buildUrl(null, {filter: filter});
                            view.getEl().down('.item-' + pos + ' > tr > td > a').set({ href: href });
                        });
                    }
                }
            }
        ];
        this.callParent(arguments);
    },

    summaryTitleUpdate: function (total) {
        var me = this,
            title = me.down('#connection-summary-title-panel');
            title.update('<h3>' + me.wTitle + ' (' + total + ' ' + Uni.I18n.translate('overview.widget.' + me.parent, 'Dsh', me.parent) + ')' + '</h3>')
    },

    setRecord: function (record) {
        var me = this,
            view = me.down('#summary-dataview'),
            title = me.down('#connection-summary-title-panel'),
            total = record.get('total');
        view.total = total || 0;
        view.record = record;
        view.bindStore(record.counters());
        me.summaryTitleUpdate(total)
    }
});