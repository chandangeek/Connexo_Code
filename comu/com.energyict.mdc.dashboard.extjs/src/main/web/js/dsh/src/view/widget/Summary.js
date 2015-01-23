Ext.define('Dsh.view.widget.Summary', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    requires: [ 'Dsh.view.widget.common.Bar' ],
    alias: 'widget.summary',
    itemId: 'summary',
    title: Uni.I18n.translate('overview.widget.summary.title', 'DSH', 'Summary'),
    header: {
        ui: 'small'
    },
    layout: 'hbox',
    initComponent: function () {
        var me = this;

        this.items = [
            {
                flex: 1,
                xtype: 'container',
                itemId: 'target-container',
                layout: 'vbox',
                style: {
                    marginRight: '20px'
                },
                items: []
            },
            {
                flex: 2,
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
                    '<tpl if="href">' +
                    '<a href="{href}">{displayName}</a><tpl else>{displayName}</tpl>' +
                    '</td>' +
                    '<td width="100%" id="bar-{[parentIndex]}" class="bar-{[parentIndex]} bar-{name}"></td>' +
                    '</tr>' +
                    '<tpl for="counters">' +
                    '<tr class="child">' +
                    '<td class="label">{displayName}</td>' +
                    '<td width="100%" id="bar-{[parentIndex]}-{#}" class="bar-{[parentIndex]} bar-{name}"></td>' +
                    '</tr>' +
                    '</tpl>' +
                    '</tbody>' +
                    '</tpl>' +
                    '</table>',
                listeners: {
                    refresh: function (view) {
                        Ext.suspendLayouts();
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
                            Ext.resumeLayouts();
                        });
                    }
                }
            }
        ];
        this.callParent(arguments);
    },

    setRecord: function (record) {
        var me = this,
            view = me.down('#summary-dataview'),
            targetContainer = me.down('#target-container'),
            total = record.get('total'),
            target = record.get('target');

        view.total = total || 0;
        view.record = record;

        record.counters().each(function (item) {
            if (item.get('id')) {
                var filter = me.router.filter.getWriteData(true, true);
                filter[record.get('alias')] = item.get('id');
                var href = me.router.getRoute('workspace/' + me.parent + '/details').buildUrl(null, {filter: filter});
                item.set('href', href);
            }
        });

        if (target) {
            targetContainer.show();
            me.initKpi(record);
        } else {
            targetContainer.hide();
        }

        view.bindStore(record.counters());
        me.setTitle(Uni.I18n.translatePlural('overview.widget.' + me.parent + '.header', total, 'DSH', '<h3>' + me.wTitle + ' ({0})' + '</h3>'));
    },

    initKpi: function(record) {
        var me = this,
            targetContainer = me.down('#target-container'),
            total = record.get('total'),
            target = record.get('target'),
            counters = record.counters();

        var success = counters.getAt(counters.findBy(function(r){return r.get('name') === 'success'}));
        var successRate = Math.round(!total ? 0 : success.get('count') * 100 / total);
        var diff = successRate - target;
        var direction = diff >= 0 ? 'above' : 'below';
        var color = diff >= 0 ? 'bar-success' : 'bar-failed';

        Ext.suspendLayouts();
        targetContainer.removeAll();
        targetContainer.add([
            {
                xtype: 'bar',
                threshold: record.get('target'),
                margin: '10 0',
                width: '100%',
                limit: total,
                total: total,
                count: success.get('count'),
                cls: color
            },
            {
                cls: 'large',
                html: '<h4>' + Uni.I18n.translatePlural('overview.widget.' + me.parent + '.label.success', successRate, 'DSH', '<b>{0}%</b> success') + '</h4>'
            },
            {
                cls: direction,
                html: Uni.I18n.translatePlural('overview.widget.' + me.parent + '.label.' + direction, Math.abs(diff), 'DSH', '<b>{0}%</b> ' + direction)
            }
        ]);
        Ext.resumeLayouts();
    }
});