Ext.define('Dsh.view.widget.Summary', {
    extend: 'Ext.panel.Panel',
    requires: [ 'Dsh.view.widget.common.Bar' ],
    alias: 'widget.summary',
    itemId: 'summary',
    wTitle: Uni.I18n.translate('overview.widget.summary.title', 'DSH', 'Summary'),
    initComponent: function () {
        var me = this;
        this.items = [
            { html: '<h3>' + me.wTitle + '</h3>' },
            {
                xtype: 'dataview',
                itemId: 'summary-dataview',
                itemSelector: 'tbody.item',
                cls: 'summary',
                total: 0,
                tpl:
                    '<table>' +
                        '<tpl for=".">' +
                            '<tbody class="item item-{#}">' +
                                '{% var parentIndex = xindex; %}' +
                                '<tr>' +
                                    '<td class="label">' +
                                        '<a href="#{alias}">{displayName}</a>' +
                                    '</td>' +
                                    '<td width="100%" id="bar-{[parentIndex]}" class="bar-{alias}"></td>' +
                                '</tr>' +
                                '<tpl for="counters">' +
                                    '<tr class="child">' +
                                        '<td class="label">' +
                                            '<a href="#{alias}">{displayName}</a>' +
                                        '</td>' +
                                        '<td width="100%" id="bar-{[parentIndex]}-{#}" class="bar-{alias}"></td>' +
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
                                        label: Math.round(!view.total ? 0 : data.get('count') * 100 / record.get('count')) + '% (' + data.get('count') + ')'
                                    });
                                    bar.render(view.getEl().down('#bar-' + pos + '-' + (idx + 1)));
                                    var href = me.router.getRoute('workspace/datacommunication/' + me.parent).buildUrl(null, {filter: [
                                        { property: view.record.get('alias'), value: data.get('id') }
                                    ]});
                                    view.getEl().down('.item-' + pos + '  tr.child > td > a').set({ href: href });
                                });
                            }
                            var bar =  Ext.widget('bar', {
                                limit: view.total,
                                total: view.total,
                                count: record.get('count'),
                                label: Math.round(!view.total ? 0 : record.get('count') * 100 / view.total) + '% (' + record.get('count') + ')'
                            });
                            bar.render(view.getEl().down('#bar-' + pos));
                            var href = me.router.getRoute('workspace/datacommunication/' + me.parent).buildUrl(null, {filter: [
                                { property: view.record.get('alias'), value: record.get('id') }
                            ]});
                            view.getEl().down('.item-' + pos + ' > tr > td > a').set({ href: href });
                        });
                    }
                }
            }
        ];
        this.callParent(arguments);
    },

    setRecord: function(record) {
        var me = this;

        var view = me.down('#summary-dataview');
        view.total = record.get('total') || 0;
        view.record = record;

        view.bindStore(record.counters());
    }
});