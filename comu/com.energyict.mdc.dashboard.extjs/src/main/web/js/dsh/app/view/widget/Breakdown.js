Ext.define('Dsh.view.widget.Breakdown', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.breakdown',
    height: 450,
    itemId: 'breakdown',
    title: Uni.I18n.translate('overview.widget.breakdown.title', 'DSH', 'Breakdown'),
    ui: 'medium',
    cls: 'breakdown',
    layout: {
        type: 'table',
        tableAttrs: {
            style: {
                width: '100%'
            }
        },
        columns: 2
    },
    width: '100%',
    defaults: {
        // applied to each contained panel
        bodyStyle: 'margin-right: 20px'
    },
    requires: [
        'Ext.view.View',
        'Dsh.view.widget.common.StackedBar',
        'Ext.button.Button'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    tbar: [
        '->',
        {
            xtype: 'container',
            html:
                '<div class="legend"><ul>' +
                '<li><span class="color failed"></span> Failed</li>' +
                '<li><span class="color success"></span> Success</li>' +
                '<li><span class="color pending"></span> Pending</li>' +
                '</ul></div>'
        }
    ],
    bindStore: function(store) {
        var me = this;
        store.each(function (item) {
            var panel = Ext.create('Ext.panel.Panel', {
                title: item.get('displayName'),
                bbar: [
                    '->',
                    {
                        xtype: 'button',
                        ui: 'link',
                        text: 'show more'
                    }
                ],
                items: {
                    xtype: 'dataview',
                    itemId: item.get('alias') + '-dataview',
                    itemSelector: 'tbody.item',
                    total: item.get('total'),
                    store: item.counters(),
                    tpl:
                        '<table>' +
                            '<tpl for=".">' +
                                '<tbody class="item">' +
                                '<tr>' +
                                    '<td class="label" style="min-width: 200px">' +
                                        '<a href="#{id}">{displayName}</a>' +
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
                                var limit = _.reduce(data, function(memo, item) {return memo + item;}, 0);
                                var bar = Ext.widget('stacked-bar', {
                                    limit: limit,
                                    total: view.total,
                                    count: data,
                                    label: limit
                                });
                                bar.render(view.getEl().down('#bar-' + pos));
                            });
                        }
                    }
                }
            });
            me.add(panel);
        });

        me.mixins.bindable.bindStore.apply(this, arguments);
        // todo: place heat map
//        me.add({
//            xtype: 'heat-map',
//            rowspan: 2
//        });
    }
})
;