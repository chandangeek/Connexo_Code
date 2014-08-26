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
    tbar: [
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
                '<li><span class="color pending"></span> ' + Uni.I18n.translate('overview.widget.breakdown.pending', 'DSH', 'Pending') + '</li>' +
                '</ul>' +
                '</div>'
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
                        marginRight: '10px'
                    }
                },
                {
                    xtype: 'panel',
                    itemId: 'summaries-1',
                    style: {
                        marginLeft: '10px'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.add({
            xtype: 'heat-map',
            itemId: 'heatmap',
            router: me.router
        });

    },

    bindStore: function (store) {
        var me = this;
        store.each(function (item, idx) {
            var panel = Ext.create('Ext.panel.Panel', {
                tbar: {
                    xtype: 'container',
                    itemId: 'title',
                    html: '<h3>' + item.get('displayName') + '</h3>'
                },
                bbar: [
                    '->',
                    {
                        xtype: 'button',
                        ui: 'link',
                        text: Uni.I18n.translate('overview.widget.breakdown.showMore', 'DSH', 'show more'),
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
                    tpl: '<table>' +
                        '<tpl for=".">' +
                        '<tbody class="item item-{#}">' +
                        '<tr>' +
                        '<td>' +
                        '<a>' +
                        '<div style="width: 200px; overflow: hidden; text-overflow: ellipsis; padding-right: 20px">{displayName}</div>' +
                        '</a>' +
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

                                var data = {
                                    failed: record.get('failedCount'),
                                    pending: record.get('pendingCount'),
                                    success: record.get('successCount')
                                };
                                var limit = _.reduce(data, function (memo, item) {
                                    return memo + item;
                                }, 0);
                                var bar = Ext.widget('stacked-bar', {
                                    limit: limit,
                                    total: item.get('total'),
                                    count: data,
                                    label: limit
                                });
                                bar.render(view.getEl().down('#bar-' + pos));
                                var href = me.router.getRoute('workspace/datacommunication/' + me.parent).buildUrl(null, {filter: [
                                    { property: item.get('alias'), value: record.get('id') }
                                ]});
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
            Uni.I18n.translate('overview.widget.breakdown.showLess', 'DSH', 'show less') :
            Uni.I18n.translate('overview.widget.breakdown.showMore', 'DSH', 'show more'));
        view.animate({
            duration: 300,
            to: {
                height: (view.collapsed ? view.expandedHeight : view.collapsedHeight)
            }
        });
        view.collapsed = !view.collapsed;
    }
});