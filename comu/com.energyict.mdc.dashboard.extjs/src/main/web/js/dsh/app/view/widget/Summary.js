Ext.define('Dsh.view.widget.Summary', {
    extend: 'Ext.panel.Panel',
    requires: [ 'Dsh.view.widget.common.Bar' ],
    alias: 'widget.summary',
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
                total: 945,
                store: Ext.create('Ext.data.Store', { //TODO: set real
                    fields: ['title', 'alias', 'count', 'child'],
                    data: [
                        {
                            title: 'Success', alias: 'success', count: 245, child: [
                            {title: 'All task successful', alias: 'sdd', count: 83},
                            {title: 'At least one task failed', alias: 'success', count: 162}
                        ]
                        },
                        {title: 'Pending', alias: 'pending', count: 62},
                        {title: 'Failed', alias: 'failed', count: 42}
                    ]
                }),
                tpl:
                    '<table>' +
                        '<tpl for=".">' +
                            '<tbody class="item">' +
                                '{% var parentIndex = xindex; %}' +
                                '<tr>' +
                                    '<td class="label">' +
                                        '<a href="#{alias}">{title}</a>' +
                                    '</td>' +
                                    '<td width="100%" id="bar-{[parentIndex]}" class="bar-{alias}"></td>' +
                                '</tr>' +
                                '<tpl for="child">' +
                                    '<tr class="child">' +
                                        '<td class="label">' +
                                            '<a href="#{alias}">{title}</a>' +
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
                            if (record.get('child')) {
                                Ext.each(record.get('child'), function (data, di) {
                                    var bar = Ext.create('Dsh.view.widget.Bar', {
                                        limit: record.get('count'),
                                        total: view.total,
                                        count: data.count,
                                        label: '(' + data.count + ')'
                                    });
                                    bar.render(view.getEl().down('#bar-' + pos + '-' + (di + 1)));
                                });
                            }
                            var bar = Ext.create('Dsh.view.widget.Bar', {
                                limit: view.total,
                                total: view.total,
                                count: record.get('count'),
                                label: '(' + record.get('count') + ')'
                            });
                            bar.render(view.getEl().down('#bar-' + pos));
                        });
                    }
                }
            }
        ];
        this.callParent(arguments);
    }
});