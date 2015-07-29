Ext.define('Dsh.view.widget.Breakdown', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.breakdown',
    itemId: 'breakdown',
    ui: 'medium',
    cls: 'breakdown',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    requires: [
        'Ext.view.View',
        'Dsh.view.widget.common.StackedBar',
        'Ext.button.Button',
        'Dsh.view.widget.HeatMap'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    itemsInCollapsedMode: 5,
    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            style: {padding: 0},
            items: [
                {
                    xtype: 'container',
                    itemId: 'title',
                    html: '<h2>' + Uni.I18n.translate('overview.widget.breakdown.title', 'DSH', 'Breakdown') + '</h2>'
                },
                '->',
                {
                    xtype: 'container',
                    html: '<div class="legend">' +
                        '<ul>' +
                        '<li><span class="color failed"></span> ' + Uni.I18n.translate('overview.widget.breakdown.failed', 'DSH', 'Failed') + '</li>' +
                        '<li><span class="color success"></span> ' + Uni.I18n.translate('overview.widget.breakdown.success', 'DSH', 'Success') + '</li>' +
                        '<li><span class="color ongoing"></span> ' + Uni.I18n.translate('overview.widget.breakdown.ongoing', 'DSH', 'Ongoing') + '</li>' +
                        '</ul>' +
                        '</div>'
                }
            ]
        }
    ],
    items: [
        {
            xtype: 'panel',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            defaults: {
                flex: 1
            },
            items: [
                {
                    xtype: 'panel',
                    itemId: 'summaries-0',
                    style: {
                        marginRight: '20px'
                    }
                },
                {
                    xtype: 'panel',
                    itemId: 'summaries-1'
                }
            ]
        }
    ],

    bindStore: function (store) {
        var me = this;
        me.down('#summaries-0').removeAll(true);
        me.down('#summaries-1').removeAll(true);
        store.each(function (item, idx) {
            item.counters().sort([
                {property: 'total', direction: 'DESC'},
                {property: 'displayName', direction: 'ASC'}
            ]);

            var first = item.counters().first();
            var total = first ? first.get('total') : 0; //Avoid 'Cannot read property 'get' of undefined' error
            var panel = Ext.create('Ext.panel.Panel', {
                ui: 'tile',
                tbar: {
                    xtype: 'container',
                    itemId: 'title',
                    html: '<h3>' + Ext.String.htmlEncode(item.get('displayName')) + '</h3>'
                },
                buttonAlign: 'left',
                buttons: [
                    {
                        text: Uni.I18n.translate('overview.widget.breakdown.showMore', 'DSH', 'Show more'),
                        hidden: item.counters().count() <= me.itemsInCollapsedMode,
                        handler: function () {
                            me.summaryMoreLess(panel);
                        }
                    }
                ],
                items: {
                    xtype: 'dataview',
                    itemId: item.get('alias') + '-dataview',
                    itemSelector: 'tbody.item',
                    total: item.get('total'),
                    store: item.counters(),
                    tpl: '<table width="100%">' +
                        '<tpl for=".">' +
                        '<tbody class="item item-{#}">' +
                        '<tr>' +
                        '<td width="50%"> ' +
                        '<a>' +
                        '<div style="overflow: hidden; text-overflow: ellipsis; padding-right: 20px">{displayName:htmlEncode}</div>' +
                        '</a>' +
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

                                var data = {
                                    failed: record.get('failedCount'),
                                    success: record.get('successCount'),
                                    ongoing: record.get('pendingCount')
                                };

                                var bar = Ext.widget('stacked-bar', {
                                    limit: record.get('total'),
                                    total: total,
                                    count: data,
                                    label: record.get('total')
                                });
                                bar.render(view.getEl().down('#bar-' + pos));

                                var filter = me.router.filter.getWriteData(true, true);
                                filter[item.get('alias')] = record.get('id');
                                var href = me.router.getRoute('workspace/' + me.parent + '/details').buildUrl(null, {filter: filter});
                                view.getEl().down('.item-' + pos + ' a').set({ href: href });
                            });

                            view.collapsed = item.counters().count() > me.itemsInCollapsedMode;
                            view.expandedHeight = view.getHeight();
                            view.collapsedHeight = view.expandedHeight / item.counters().count() * me.itemsInCollapsedMode;
                            if (view.collapsed) view.setHeight(view.collapsedHeight);
                        }
                    }
                }
            });
            me.down('#summaries-' + idx % 2).add(panel);
        });
        me.mixins.bindable.bindStore.apply(this, arguments);
    },

    summaryMoreLess: function (panel) {
        var view = panel.down('dataview');
        panel.down('button').setText(view.collapsed ?
            Uni.I18n.translate('overview.widget.breakdown.showLess', 'DSH', 'Show less') :
            Uni.I18n.translate('overview.widget.breakdown.showMore', 'DSH', 'Show more'));
        view.animate({
            duration: 300,
            to: {
                height: (view.collapsed ? view.expandedHeight : view.collapsedHeight)
            }
        });
        view.collapsed = !view.collapsed;
    }
});