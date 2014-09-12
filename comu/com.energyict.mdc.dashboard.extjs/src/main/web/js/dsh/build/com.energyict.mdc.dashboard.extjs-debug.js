Ext.define('Dsh.view.widget.HeaderSection', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.header-section',
    itemId: 'header-section',
    wTitle: Uni.I18n.translate('overview.widget.headerSection.title', 'DSH', 'Overview'),
    layout: 'column',

    initComponent: function () {
        var me = this;
        this.items = [
            {
                itemId: 'headerTitle',
                baseCls: 'x-panel-header-text-container-large',
                html: me.wTitle
            },
//            {
//                xtype: 'combobox',
//                style: {
//                    float: 'left',
//                    marginTop: '18px'
//                },
//                fieldLabel: Uni.I18n.translate('overview.widget.headerSection.deviceGroupLabel', 'DSH', 'for device group'),
//                labelWidth: 150,
//                displayField: 'group',
//                valueField: 'id',
//                value: 1,
//                store: Ext.create('Ext.data.Store', {
//                    fields: ['id', 'group'],
//                    data: [ //TODO: set real store
//                        { 'id': 1, 'group': 'North region' },
//                        { 'id': 2, 'group': 'South region' },
//                        { 'id': 3, 'group': 'West region' },
//                        { 'id': 4, 'group': 'East region' }
//                    ]
//                })
//            },
            {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                style: 'float: right; margin-top: 18px',
                items: [
                    {
                        xtype: 'displayfield',
                        itemId: 'last-updated-field',
                        style: 'margin-right: 10px'
                    },
                    {
                        xtype: 'button',
                        itemId: 'refresh-btn',
                        text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                        style: {
                            lineHeight: 'none'
                        },
                        iconCls: 'fa fa-refresh fa-lg' //TODO: set real img,
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});

Ext.define('Dsh.view.widget.common.Bar', {
    alias: 'widget.bar',
    extend: 'Ext.Component',

    requires: [
        'Ext.Template',
        'Ext.CompositeElement',
        'Ext.TaskManager',
        'Ext.layout.component.ProgressBar'
    ],

    total: 100,
    limit: 100,
    count: 0,

    baseCls: Ext.baseCSSPrefix + 'bar',

    trackTpl: '<div class="{baseCls}-track" style="width: {count}%;"></div>',
    renderTpl: [
        '<tpl for=".">',
            '<span class="{baseCls}-label">{label}</span>',
            '<div class="{baseCls}-container" style="width: 100%;">',
                '<div class="{baseCls}-fill" style="width: {limit}%;">',
                    '{track}',
                '</div>',
            '<div>',
        '</tpl>'
    ],

    prepareData: function(){
        var me = this;
        return {
            limit: !me.total ? 0 : Math.round(me.limit * 100 / me.total),
            label: me.label,
            count: !me.limit ? 0 : Math.round(me.count * 100 / me.limit)
        }
    },

    initRenderData: function() {
        var me = this;

        var track = new Ext.XTemplate(me.trackTpl);
        var data = Ext.apply(me.callParent(), me.prepareData());
        return Ext.apply(data, {track: track.apply(data)});
    }
});

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
                                        label: !record.get('count') ? 0 : Math.round(!view.total ? 0 : data.get('count') * 100 / record.get('count')) + '% (' + data.get('count') + ')'
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

Ext.define('Dsh.view.widget.CommunicationServers', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communication-servers',
    initComponent: function () {
        var router = this.router;
        this.items = [
            {
                xtype: 'dataview',
                store: 'CommunicationServerInfos',
                itemSelector: 'tr.comserver',
                emptyText: Uni.I18n.translate('overview.widget.communicationServers.noServersFound', 'DSH', 'No communication servers found'),
                tpl: new Ext.XTemplate(
                    '<div>',
                        '<h3>' + Uni.I18n.translate('overview.widget.communicationServers.title', 'DSH', 'Communication servers') + '</h3>',
                        '<table style="margin: 5px 0 10px 0">',
                            '<tpl for=".">',
                                '<tpl if="!running && xindex &lt; 4">',
                                    '<tr class="comserver" >',
                                        '<td>',
                                            '<a href="' + router.getRoute('administration/comservers/detail/overview').buildUrl({id: '{comServerId}'}) + '">{comServerName}</a>',
                                        '</td>',
                                        '<td style="padding-left: 15px;">',
                                            '<tpl if="running">',
                                                '<span style="color: #00aa00" class="fa fa-caret-square-o-up"/>', //TODO: set real img
                                            '<tpl else>',
                                                '<span style="color: #d80000" class="fa fa-caret-square-o-down"/>', //TODO: set real img
                                            '</tpl>',
                                        '</td>',
                                    '</tr>',
                                '</tpl>',
                            '</tpl>',
                        '</table>',
                        '<a href="' + router.getRoute('administration/comservers').buildUrl() + '">' + Uni.I18n.translate('overview.widget.communicationServers.viewAll', 'DSH', 'View all communication servers') + '</a>',
                    '</div>'
                ),
                listeners: {
                    afterrender: function (view) {
                        view.store.load();
                    },
                    itemmouseenter: function (view, record) {
                        Ext.create('Ext.tip.ToolTip', {
                            itemId: 'communication-servers-tooltip',
                            target: view.el,
                            delegate: view.itemSelector,
                            trackMouse: true,
                            showDelay: 50,
                            hideDelay: 0,
                                html: '<table>' +
                                    '<tr>' +
                                        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.communicationServer', 'DSH', 'Communication server') + '</td>' +
                                        '<td>' + record.get('comServerName') + '</td>' +
                                    '</tr>' +
                                    '<tr>' +
                                        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.onlineRemote', 'DSH', 'Online/Remote') + '</td>' +
                                        '<td>' + record.get('comServerType').charAt(0).toUpperCase() + record.get('comServerType').slice(1).toLocaleLowerCase() + '</td>' +
                                    '</tr>' +
                                    '<tr>' +
                                        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.downSince', 'DSH', 'Down since') + '</td>' +
                                        '<td>' + Ext.util.Format.date(new Date(), 'D M j, Y G:i') + '</td>' +
                                    '</tr>' +
                                '</table>',
                            listeners: {
                                hide: function (tooltip) {
                                    tooltip.destroy();
                                }
                            }
                        });
                    }
                }
            }
        ];
        this.callParent(arguments);
    },
    reload: function(){
        this.down('dataview').store.load();
    }
});

Ext.define('Dsh.view.widget.QuickLinks', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.quick-links',
    data: [],
    items: [
        {
            itemId: 'quicklinksTplPanel',
            tpl: new Ext.XTemplate(
                '<div>',
                    '<h3>' + Uni.I18n.translate('overview.widget.quicklinks.title', 'DSH', 'Quick links') + '</h3>',
                    '<ul style="list-style: none; padding: 0; margin-top: 5px">',
                        '<tpl for=".">',
                            '<li><a href="{href}">{link}</a></li>',
                        '</tpl>',
                    '</ul>',
                '</div>'
            )
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
        this.down('#quicklinksTplPanel').data = this.data;
    }
});

Ext.define('Dsh.view.widget.ReadOutsOverTime', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.read-outs-over-time',
    itemId: 'read-outs-over-time',
    wTitle: Uni.I18n.translate('overview.widget.readOutsOverTime.title', 'DSH', 'Read-outs over time'),
    layout: 'fit',
    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                style: {
                    paddingBottom: '30px'
                },
                items: [
                    {
                        xtype: 'container',
                        itemId: 'readOutsTitle',
                        baseCls: 'x-panel-header-text-container-medium',
                        html: me.wTitle
                    },
                    {
                        xtype: 'container',
                        flex: 1,
                        width: 200,
                        html: '<svg id="read-outs-legend-container" style="width: 100%"></svg>'
                    }
                ]
            },
            {
                xtype: 'container',
                height: 400,
                style: {
                    marginTop: '-130px',
                    marginBottom: '180px'
                },
                listeners: {
                    scope: this,
                    afterrender: function (container) {
                        var me = this;
                        Ext.defer(function () {
                            new Highcharts.Chart({
                                chart: {
                                    type: 'spline',
                                    zoomType: 'x',
                                    renderTo: container.el.dom,
                                    events: {
                                        load: function () {
                                            $('#' + container.getId() + " .highcharts-legend").appendTo("#read-outs-legend-container");
                                        }
                                    }
                                },
                                title: {
                                    text: ''
                                },
                                legend: {
                                    align: 'left',
                                    verticalAlign: 'top',
                                    floating: true,
                                    x: 25,
                                    y: -5
                                },
                                credits: {
                                    enabled: false
                                },
                                exporting: {
                                    enabled: false
                                },
                                tooltip: {
                                    valueSuffix: '%'
                                },
                                plotOptions: {
                                    series: {
                                        animation: false,
                                        pointInterval: 3600 * 1000
                                    },
                                    spline: {
                                        lineWidth: 3,
                                        states: {
                                            hover: {
                                                lineWidth: 5
                                            }
                                        },
                                        marker: {
                                            enabled: false
                                        }
                                    }
                                },
                                xAxis: {
                                    lineWidth: 2,
                                    type: 'datetime',
                                    dateTimeLabelFormats: {
                                        day: '%H:%M'
                                    },
                                    tickInterval: 3600 * 1000
                                },
                                yAxis: {
                                    title: {
                                        text: 'Number of connections'
                                    },
                                    labels: {
                                        format: '{value}%'
                                    },
                                    lineWidth: 2,
                                    tickWidth: 1,
                                    gridLineWidth: 0,
                                    floor: 0,
                                    ceiling: 100,
                                    tickInterval: 10
                                },
                                series: [
                                    {
                                        name: 'Target',
                                        data: me.getFakeData()
                                    },
                                    {
                                        name: 'Pending',
                                        dashStyle: 'longdash',
                                        data: me.getFakeData()
                                    },
                                    {
                                        name: 'Waiting',
                                        data: me.getFakeData()
                                    },
                                    {
                                        name: 'Failed',
                                        data: me.getFakeData()
                                    },
                                    {
                                        name: 'On hold',
                                        data: me.getFakeData()
                                    }
                                ]
                            });
                        }, 100);
                    }
                }
            }
        ];
        this.callParent(arguments);
    },
    getFakeData: function () {
        var fakeDataArray = [];
        for (var i = 0; i < 24; i++) {
            fakeDataArray.push(Math.floor((Math.random() * 100)));
        }
        return fakeDataArray;
    }
});

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
        me.removeAll(true);
        store.each(function (item, idx) {
            item.counters().sort({ property: 'count', direction: 'DESC' });
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
                                    total: view.total,
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

Ext.define('Dsh.view.widget.common.StackedBar', {
    alias: 'widget.stacked-bar',
    extend: 'Dsh.view.widget.common.Bar',

    tooltipTpl: '<table><tpl foreach="."><tr><td>{[xkey]}</td><td>{.}</td></tr></tpl></table>',
    trackTpl: [
        '<div data-qtip="{tooltip}">',
        '<tpl foreach="count">',
            '<div class="{parent.baseCls}-track {parent.baseCls}-track-stacked {[xkey]}" style="width: {.}%;"></div>',
        '</tpl>',
        '</div>'
    ],

    prepareData: function(){
        var me = this;
        var counts = _.object(_.map(me.count, function (value, key) {
            return [key, !me.limit ? 0 : Math.round(value * 100 / me.limit)];
        }));
        return Ext.apply(me.callParent(), {
            count: counts,
            tooltip: new Ext.XTemplate(me.tooltipTpl).apply(me.count)
        });
    }
});

Ext.define('Dsh.view.widget.HeatMap', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.heat-map',
    layout: 'fit',
    minHeight: 450,
    tbar: [
        '->',
        {
            xtype: 'fieldcontainer',
            fieldLabel: 'Combine',
            items: {
                xtype: 'combobox',
                itemId: 'combine-combo',
                displayField: 'localizedValue',
                queryMode: 'local',
                valueField: 'breakdown',
                store: 'Dsh.store.CombineStore',
                autoSelect: true
            }
        },
        '->'
    ],
    items: {
        xtype: 'box',
        minHeight: 400,
        itemId: 'heatmapchart'
    },

    setChartData: function (data) {
        var me = this;
        me.chart.series[0].setData([], true);
        me.chart.series[0].setData(data, true);
    },

    setXAxis: function (categories, title) {
        var me = this;
        me.chart.series[0].xAxis.update({title: {text: title}}, false);
        me.chart.series[0].xAxis.update({categories: categories}, false);
    },

    setYAxis: function (categories, title) {
        var me = this;
        me.chart.series[0].yAxis.update({title: {text: title}}, false);
        me.chart.series[0].yAxis.update({categories: categories}, false);
    },

    storeToHighchartData: function (store) {
        var data = [],
            x = 0,
            y = 0;
        store.each(function (rec) {
            Ext.each(rec.data.data, function (item) {
                var count = item.count,
                    value = (count == 0 ? count.toString() : count);
                data.push([x, y, value]);
                ++y;
            });
            y = 0;
            ++x;
        });
        return data;
    },

    getCombo: function () {
        return this.down('#combine-combo');
    },

    loadChart: function (store, xTitle) {
        if (store.getCount() > 0) {
            var me = this,
                ycat = [],
                xcat = store.collect('displayValue')
                ;
            Ext.each(store.getAt(0).data.data, function (item) {
                ycat.push(item.displayName);
            });
            me.setXAxis(xcat, xTitle);
            me.setYAxis(ycat, 'Latest result');
            me.setChartData(me.storeToHighchartData(store));
        }
    },

    initComponent: function () {
        var me = this,
            xTitle = '',
            store = Ext.getStore('Dsh.store.ConnectionResultsStore');
        this.callParent(arguments);
        store.on('load', function () {
            me.loadChart(store, xTitle)
        });
        var combo = me.getCombo(),
            cmp = me.down('#heatmapchart');
        combo.getStore().on('load', function (store) {
            if (store.getCount() > 0) {
                var val = store.getAt(1);
                combo.select(val);
                xTitle = val.get('localizedValue')
            }
        });
        combo.on('change', function (combo, newValue) {
            store.proxy.extraParams.filter = '[{"property":"breakdown","value": "' + newValue + '"}]';
            store.load();
        });
        cmp.on('afterrender', function () {
            Ext.defer(function () {
                me.renderChart(cmp.getEl().dom);
            }, 100);
        });

    },

    renderChart: function (container) {
        var me = this;
        this.chart = new Highcharts.Chart({
            chart: {
                type: 'heatmap',
                renderTo: container
            },
            exporting: {
                enabled: false
            },
            credits: {
                enabled: false
            },
            title: null,
            xAxis: {
                title: {
                    style: {
                        "color": "#707070",
                        "fontWeight": "bold"
                    }
                }
            },
            yAxis: {
                title: {
                    style: {
                        "color": "#707070",
                        "fontWeight": "bold"
                    }
                }
            },
            colorAxis: {
                min: 0,
                minColor: '#FFFFFF',
                maxColor: Highcharts.getOptions().colors[0]
            },
            options: {
                width: '100%',
                height: '100%'
            },
            legend: {
                align: 'right',
                layout: 'vertical',
                margin: 0,
                verticalAlign: 'top',
                symbolHeight: 350
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.series.xAxis.categories[this.point.x] + '</b><br><b>' +
                        this.series.yAxis.categories[this.point.y] + '</b><br><b>' + this.point.value + '</b>';
                }
            },
            series: [
                {
                    name: 'Latest Result',
                    borderWidth: 1,
                    dataLabels: {
                        enabled: true,
                        color: 'black',
                        style: {
                            textShadow: 'none',
                            HcTextStroke: null
                        }
                    }
                }
            ]
        }, function () {
            me.doLayout();
        });
    }
})
;

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
        me.down('#summaries-0').removeAll(true);
        me.down('#summaries-1').removeAll(true);
        store.each(function (item, idx) {
            item.counters().sort({ property: 'total', direction: 'DESC' });
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
                                var bar = Ext.widget('stacked-bar', {
                                    limit: record.get('total'),
                                    total: item.get('total'),
                                    count: data,
                                    label: record.get('total')
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

Ext.define('Dsh.view.CommunicationOverview', {
    extend: 'Ext.container.Container',
    requires: [
        'Dsh.view.widget.HeaderSection',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown'
    ],
    alias: 'widget.communication-overview',
    itemId: 'communication-overview',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        padding: '15px'
    },
    defaults: {
        style: {
            marginTop: '30px',
            paddingTop: '30px',
            borderTop: '3px dotted grey'
        }
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'header-section',
                wTitle: Uni.I18n.translate('communication.widget.headerSection.title', 'DSH', 'Communication overview'),
                style: 'none'
            },
            {
                layout: 'hbox',
                style: {
                    marginTop: '30px',
                    border: 'none'
                },
                defaults: {
                    style: {
                        paddingRight: '50px'
                    }
                },
                items: [
                    {
                        xtype: 'summary',
                        flex: 3,
                        wTitle: Uni.I18n.translate('communication.widget.summary.title', 'DSH', 'Communication summary'),
                        style: {
                            paddingRight: '150px'
                        }
                    },
                    {
                        xtype: 'communication-servers',
                        itemId: 'communication-servers',
                        router: me.router,
                        flex: 1,
                        style: {
                            borderRight: '3px dotted grey'
                        }
                    },
                    {
                        xtype: 'quick-links',
                        itemId: 'quick-links',
                        flex: 1,
                        style: {
                            paddingLeft: '50px'
                        },
                        data: [
                            {
                                link: Uni.I18n.translate('communication.widget.quicklinks.viewAll', 'DSH', 'View all communications'),
                                href: me.router.getRoute('workspace/datacommunication/communications').buildUrl()
                            },
                            {
                                link: Uni.I18n.translate('connection.widget.headerSection.title', 'DSH', 'Connection overview'),
                                href: me.router.getRoute('workspace/datacommunication/connection').buildUrl()
                            }
                        ]
                    }
                ]
            },
//            {
//                xtype: 'read-outs-over-time'
//            },
            {
                xtype: 'overview',
                category: 'Communication'
            },
            {
                xtype: 'breakdown',
                parent: 'communications',
                router: me.router
            }
        ];
        this.callParent(arguments);
    }
});

Ext.define('Dsh.model.communication.Overview', {
    extend: 'Ext.data.Model',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/communicationoverview',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

Ext.define('Dsh.model.CommunicationServerInfo', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'comServerId', type: 'int' },
        { name: 'comServerName', type: 'string' },
        { name: 'comServerType', type: 'string' },
        { name: 'running', type: 'boolean' },
        { name: 'blocked', type: 'boolean' },
        { name: 'blockTime', type: 'auto' }
    ],
    associations: [
        { name: 'blockTime', type: 'hasOne', model: 'Dsh.model.TimeInfo', associationKey: 'blockTime' }
    ],
    proxy: {
        type: 'ajax',
        url: '../../api/dsr/comserverstatussummary',
        reader: {
            type: 'json',
            root: 'comServerStatusInfos'
        }
    }
});

Ext.define('Dsh.store.CommunicationServerInfos', {
    extend: 'Ext.data.Store',
    storeId: 'CommunicationServerInfos',
    requires: ['Dsh.model.CommunicationServerInfo'],
    model: 'Dsh.model.CommunicationServerInfo',
    sorters: [{ property: 'running', direction: 'ASC' }]
});

Ext.define('Dsh.controller.CommunicationOverview', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.communication.Overview'
    ],
    stores: [
        'CommunicationServerInfos'
    ],
    views: [
        'Dsh.view.CommunicationOverview'
    ],
    refs: [
        { ref: 'communicationOverview', selector: '#communication-overview' },
        { ref: 'header', selector: '#header-section' },
        { ref: 'summary', selector: '#summary' },
        { ref: 'communicationServers', selector: '#communication-servers' },
        { ref: 'overview', selector: '#overview' },
        { ref: 'breakdown', selector: '#breakdown' }
    ],

    init: function () {
        this.control({
            '#communication-overview #refresh-btn': {
                click: this.loadData
            }
        });
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('communication-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            model = me.getModel('Dsh.model.communication.Overview');
        me.getCommunicationOverview().setLoading();
        me.getCommunicationServers().reload();
        model.load(null, {
                success: function (record) {
                    me.getSummary().setRecord(record.getSummary());
                    me.getOverview().bindStore(record.overviews());
                    me.getBreakdown().bindStore(record.breakdowns());
                    me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Ext.util.Format.date(new Date(), 'H:i'));
                },
                callback: function () {
                    me.getCommunicationOverview().setLoading(false);
                }
            }
        );
    }
});

Ext.define('Dsh.view.widget.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dsh-action-menu',
    plain: true,
    items: [
        {   itemId: 'assign',
            text: 'Item 1',
            action: 'assign'
        },
        {
            itemId: 'close',
            text: 'item 2',
            action: 'close'
        }
    ]
});

Ext.define('Dsh.view.widget.CommunicationsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communications-list',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dsh.view.widget.ActionMenu'
    ],
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('communication.widget.details.commTask', 'DSH', 'Communications'),
                dataIndex: 'name',
                flex: 2
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('communication.widget.details.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return val.name ? val.name : '';
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('communication.widget.details.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? val.displayValue : '';
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                dataIndex: 'latestResult',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? val.displayValue : '';
                }
            },
            {
                itemId: 'nextCommunication',
                text: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Next communication'),
                dataIndex: 'nextCommunication',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'successfulFinishTime',
                text: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Finished succesfuly on'),
                dataIndex: 'successfulFinishTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn'
            }
        ]
    }
});


Ext.define('Dsh.view.widget.PreviewCommunication', {
    extend: 'Ext.form.Panel',
    alias: 'widget.preview_communication',
    title: '',
    frame: true,
    layout: {
        type: 'column'
    },
    tools: [
//        {
//            xtype: 'button',
//            text: Uni.I18n.translate('general.actions', 'ISE', 'Actions'),
//            iconCls: 'x-uni-action-iconD',
//            menu: {
//                xtype: 'dsh-action-menu'
//            }
//        }
    ],
    items: [
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.commTask', 'DSH', 'Name'),
                    name: 'name'
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.commTask', 'DSH', 'Communication task(s)'),
                    name: 'title'
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.device', 'DSH', 'Device'),
                    name: 'device',
                    renderer: function (val) {
                        return '<a href="#/devices/' + val.id + '">' + val.name + '</a>'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        return '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.deviceConfig', 'DSH', 'Device configuration'),
                    name: 'devConfig',
                    renderer: function (val) {
                        var res = '';
                        val && (res = '<a href="#/administration/devicetypes/' +
                            val.devType.id + '/deviceconfigurations/' +
                            val.config.id +
                            '">' +
                            val.config.name +
                            '</a>');
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.frequency', 'DSH', 'Frequency'),
                    name: 'comScheduleName'
                },
                {
                    fieldLabel: ' ',
                    name: 'comScheduleFrequency',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            res = Uni.I18n.translate('communication.widget.details.every', 'DSH', 'Every')
                                + ' '
                                + val.every.count
                                + ' '
                                + val.every.timeUnit;
                        }
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.urgency', 'DSH', 'Urgency'),
                    name: 'urgency'
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.executeOnInbound', 'DSH', 'Always execute on inbound'),
                    name: 'alwaysExecuteOnInbound',
                    renderer: function (val) {
                        return val ? 'Yes' : 'No'
                    }
                }
            ]
        },
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.currentState', 'DSH', 'Current state'),
                    name: 'currentState',
                    renderer: function (val) {
                        return val.displayValue ? val.displayValue : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.latestResult', 'DSH', 'Latest result'),
                    name: 'latestResult',
                    renderer: function (val) {
                        return val.displayValue ? val.displayValue : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Started on'),
                    name: 'startTime',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'm/d/Y h:i:s');
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.finishedOn', 'DSH', 'Finished successful on'),
                    name: 'successfulFinishTime',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'm/d/Y h:i:s');
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.nextComm', 'DSH', 'Next communication'),
                    name: 'nextCommunication',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'm/d/Y h:i:s');
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    }
});

Ext.define('Dsh.view.widget.PreviewConnection', {
    extend: 'Ext.form.Panel',
    alias: 'widget.preview_connection',
    title: '',
    frame: true,
    layout: {
        type: 'column'
    },
    tools: [
//        {
//            xtype: 'button',
//            text: Uni.I18n.translate('general.actions', 'ISE', 'Actions'),
//            iconCls: 'x-uni-action-iconD',
//            menu: {
//                xtype: 'dsh-action-menu'
//            }
//        }
    ],
    items: [
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.device', 'DSH', 'Device'),
                    name: 'device',
                    renderer: function (val) {
                        return val ? '<a href="#/devices/' + val.id + '">' + val.name + '</a>' : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        return val ? '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>' : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.deviceConfig', 'DSH', 'Device configuration'),
                    name: 'devConfig',
                    renderer: function (val) {
                        var res = '';
                        val && (res = '<a href="#/administration/devicetypes/' +
                            val.devType.id + '/deviceconfigurations/' +
                            val.config.id +
                            '">' +
                            val.config.name +
                            '</a>');
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.direction', 'DSH', 'Direction'),
                    name: 'direction'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connType', 'DSH', 'Connection type'),
                    name: 'connectionType'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connMethod', 'DSH', 'Connection method'),
                    name: 'connectionMethod',
                    renderer: function (val) {
                        return val ? val.name : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connWindow', 'DSH', 'Connection window'),
                    name: 'window'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.strategy', 'DSH', 'Strategy'),
                    name: 'connectionStrategy',
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.nextConnection', 'DSH', 'Next connection'),
                    name: 'nextExecution',
                    renderer: function (val) {
                        return val ? Ext.Date.format(val, 'm/d/Y h:i:s') : '';
                    }
                }
            ]
        },
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.currentState', 'DSH', 'Current state'),
                    name: 'currentState',
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.latestStatus', 'DSH', 'Latest status'),
                    name: 'latestStatus',
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                    name: 'latestResult',
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commTasks', 'DSH', 'Communication tasks'),
                    name: 'taskCount',
                    renderer: function (val) {
                        return '<tpl><span class="fa fa-check fa-lg" style="color: green; width: 24px; vertical-align: 0% !important;"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '') + '</span><br></tpl>' +
                            '<tpl><span class="fa fa-times fa-lg" style="color: red; width: 24px; vertical-align: 0% !important;"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '') + '<br></tpl>' +
                            '<tpl><span class="fa fa-ban fa-lg" style="color: #333333; width: 24px; vertical-align: 0% !important"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '') + '</tpl>'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.startedOn', 'DSH', 'Started on'),
                    name: 'startDateTime',
                    renderer: function (val) {
                        return val ? Ext.Date.format(val, 'm/d/Y h:i:s') : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.finishedOn', 'DSH', 'Finished on'),
                    name: 'endDateTime',
                    renderer: function (val) {
                        return val ? Ext.Date.format(val, 'm/d/Y h:i:s') : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.duration', 'DSH', 'Duration'),
                    name: 'duration',
                    renderer: function (val) {
                        return val ? val.count + ' ' + val.timeUnit : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commServer', 'DSH', 'Communication server'),
                    name: 'comServer',
                    renderer: function (val) {
                        return val ? '<a href="#/administration/comservers/' + val.id + '">' + val.name + '</a>' : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commPort', 'DSH', 'Communication port'),
                    name: 'comPortPool',
                    renderer: function (val) {
                        return val ? val.name : ''
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    }
});

Ext.define('Dsh.view.Communications', {
    extend: 'Uni.view.container.ContentContainer',
    //   extend: 'Ext.container.Container',
    alias: 'widget.communications-details',
    itemId: 'communicationsdetails',
    overflowY: 'auto',
    requires: [
        'Dsh.view.widget.CommunicationsList',
        'Dsh.view.widget.PreviewCommunication'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.communication.title', 'DSH', 'Communications'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'communications-list',
                        itemId: 'communicationslist'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('workspace.dataCommunication.connections.empty.title', 'DSH', 'No connections found'),
                        reasons: [
                            Uni.I18n.translate('workspace.dataCommunication.connections.empty.list.item1', 'DSH', 'No connections in the system.'),
                            Uni.I18n.translate('workspace.dataCommunication.connections.empty.list.item2', 'DSH', 'No connections found due to applied filters.')
                        ]
                    },
                    previewComponent: {
                        items: [
                            {
                                xtype: 'preview_communication',
                                itemId: 'communicationdetails'
                            },
                            {
                                xtype: 'preview_connection',
                                itemId: 'connectiondetails'
                            }
                        ]
                    }
                }
            ]
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});


Ext.define('Dsh.model.CommunicationTask', {
    extend: 'Ext.data.Model',
    fields: [
        "name",
        "device",
        "id",
        "deviceConfiguration",
        "deviceType",
        "comScheduleName",
        "comScheduleFrequency",
        "urgency",
        "currentState",
        "alwaysExecuteOnInbound",
        "latestResult",
        { name: 'startTime', type: 'date', dateFormat: 'time'},
        { name: 'successfulFinishTime', type: 'date', dateFormat: 'time'},
        { name: 'nextCommunication', type: 'date', dateFormat: 'time'},
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                return data.name + ' on ' + data.device.name;
            }
        },
        {
            name: 'devConfig',
            persist: false,
            mapping: function (data) {
                var devConfig = {};
                devConfig.config = data.deviceConfiguration;
                devConfig.devType = data.deviceType;
                return devConfig
            }
        }
    ]
});







Ext.define('Dsh.store.CommunicationTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.CommunicationTask'
    ],
    model: 'Dsh.model.CommunicationTask',
    proxy: {
        type: 'ajax',
        url: '../../apps/dashboard/app/fakeData/CommunicationTasksFake.json',
        reader: {
            type: 'json',
            root: 'communicationsTasks',
            totalProperty: 'count'
        }
    }
});



Ext.define('Dsh.controller.Communications', {
    extend: 'Ext.app.Controller',

    views: [
        'Dsh.view.Communications',
        'Dsh.view.widget.PreviewCommunication'
    ],

    stores: [
        'Dsh.store.CommunicationTasks'
    ],

    refs: [
        {
            ref: 'communicationPreview',
            selector: '#communicationdetails'
        }
    ],

    init: function () {
        this.control({
            '#communicationslist': {
                selectionchange: this.onSelectionChange
            }
        });
        this.getStore('Dsh.store.CommunicationTasks').load();
        this.callParent(arguments);
    },
    showOverview: function () {
        var widget = Ext.widget('communications-details');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    onSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0],
            preview = me.getCommunicationPreview();
        preview.loadRecord(record);
        preview.setTitle(record.get('name'));
    }
});

Ext.define('Dsh.view.ConnectionOverview', {
    extend: 'Ext.container.Container',
    requires: [
        'Dsh.view.widget.HeaderSection',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown'
    ],
    alias: 'widget.connection-overview',
    itemId: 'connection-overview',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        padding: '15px'
    },
    defaults: {
        style: {
            marginTop: '30px',
            paddingTop: '30px',
            borderTop: '3px dotted grey'
        }
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'header-section',
                wTitle: Uni.I18n.translate('connection.widget.headerSection.title', 'DSH', 'Connection overview'),
                style: 'none'
            },
            {
                layout: 'hbox',
                style: {
                    marginTop: '30px',
                    border: 'none'
                },
                defaults: {
                    style: {
                        paddingRight: '50px'
                    }
                },
                items: [
                    {
                        xtype: 'summary',
                        flex: 3,
                        wTitle: Uni.I18n.translate('connection.widget.summary.title', 'DSH', 'Connection summary'),
                        router: me.router,
                        parent: 'connections',
                        style: {
                            paddingRight: '150px'
                        }
                    },
                    {
                        xtype: 'communication-servers',
                        itemId: 'communication-servers',
                        router: me.router,
                        flex: 1,
                        style: {
                            borderRight: '3px dotted grey'
                        }
                    },
                    {
                        xtype: 'quick-links',
                        itemId: 'quick-links',
                        flex: 1,
                        style: {
                            paddingLeft: '50px'
                        },
                        data: [
                            {
                                link: Uni.I18n.translate('connection.widget.quicklinks.viewAll', 'DSH', 'View all connections'),
                                href: me.router.getRoute('workspace/datacommunication/connections').buildUrl()
                            },
                            {
                                link: Uni.I18n.translate('communication.widget.headerSection.title', 'DSH', 'Communication overview'),
                                href: me.router.getRoute('workspace/datacommunication/communication').buildUrl()
                            }
                        ]
                    }
                ]
            },
//            {
//                xtype: 'read-outs-over-time'
//            },
            {
                xtype: 'overview',
                category: 'Connection',
                parent: 'connections',
                router: me.router
            },
            {
                xtype: 'breakdown',
                parent: 'connections',
                router: me.router
            }
        ];
        this.callParent(arguments);
    }
});

Ext.define('Dsh.model.Counter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'count', type: 'int' },
        { name: 'alias', type: 'string' },
        { name: 'displayName', type: 'string' },
        { name: 'total', type: 'int' }
    ],

    hasMany: {
        model: 'Dsh.model.Counter',
        name: 'counters'
    }
});

Ext.define('Dsh.model.Summary', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.Counter'
    ],
    fields: [
        { name: 'total', type: 'int'}
    ],
    hasMany: {
        model: 'Dsh.model.Counter',
        name: 'counters'
    }
});

Ext.define('Dsh.model.BreakdownCounter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'int' },
        { name: 'displayName', type: 'string' },
        { name: 'successCount', type: 'int' },
        { name: 'failedCount', type: 'int' },
        { name: 'pendingCount', type: 'int' },
        {
            name: 'total',
            type: 'int',
            persist: false,
            convert: function (v, record) {
                return record.get('successCount') + record.get('failedCount') + record.get('pendingCount');
            }
        }
    ]
});

Ext.define('Dsh.model.Breakdown', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.BreakdownCounter'
    ],
    fields: [
        { name: 'displayName', type: 'string' },
        { name: 'alias', type: 'string' },
        { name: 'total', type: 'int'},
        { name: 'totalSuccessCount', type: 'int'},
        { name: 'totalPendingCount', type: 'int'},
        { name: 'totalFailedCount', type: 'int'}
    ],
    hasMany: {
        model: 'Dsh.model.BreakdownCounter',
        name: 'counters'
    }
});

Ext.define('Dsh.model.connection.Overview', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.Summary',
        'Dsh.model.Counter',
        'Dsh.model.Breakdown'
    ],
    hasOne: {
        model: 'Dsh.model.Summary',
        associationKey: 'connectionSummary',
        name: 'summary',
        getterName: 'getSummary',
        setterName: 'setSummary'
    },
    hasMany: [
        {
            model: 'Dsh.model.Counter',
            name: 'overviews'
        },
        {
            model: 'Dsh.model.Breakdown',
            name: 'breakdowns'
        }
    ],
    proxy: {
        type: 'ajax',
        url: '/api/dsr/connectionoverview'
    }
});

Ext.define('Dsh.model.Combine', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'localizedValue', type: 'string' },
        { name: 'breakdown', type: 'string' }
    ]
});

Ext.define('Dsh.store.CombineStore', {
    extend: 'Ext.data.Store',
    storeId: 'CombineStore',
    model: 'Dsh.model.Combine',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/breakdown',
        reader: {
            type: 'json',
            root: 'breakdowns'
        }
    }
});


Ext.define('Dsh.model.ConnectionResults', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'displayValue', type: 'string' },
        { name: 'alias', type: 'string' },
        { name: 'id', type: 'int' },
        'data'
    ],
    hasMany: [
        {
            model: 'Dsh.model.Result',
            name: 'data'
        }
    ]
});

Ext.define('Dsh.store.ConnectionResultsStore', {
    extend: 'Ext.data.Store',
    model: 'Dsh.model.ConnectionResults',
    proxy: {
        type: 'ajax',
//        type: 'rest',
        url: '/api/dsr/connectionheatmap',
//        url: '../../apps/dashboard/app/fakeData/heatMapFake.json',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'heatMap'
        }
    }
});

Ext.define('Dsh.controller.ConnectionOverview', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.connection.Overview'
    ],
    stores: [
        'CommunicationServerInfos',
        'Dsh.store.CombineStore',
        'Dsh.store.ConnectionResultsStore'
    ],
    views: [ 'Dsh.view.ConnectionOverview' ],
    refs: [
        { ref: 'connectionOverview', selector: '#connection-overview' },
        { ref: 'header', selector: '#header-section' },
        { ref: 'summary', selector: '#summary' },
        { ref: 'communicationServers', selector: '#communication-servers' },
        { ref: 'overview', selector: '#overview' },
        { ref: 'breakdown', selector: '#breakdown' }
    ],

    init: function () {
        this.control({
            '#connection-overview #refresh-btn': {
                click: this.loadData
            }
        });
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('connection-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            model = me.getModel('Dsh.model.connection.Overview');
        me.getConnectionOverview().setLoading();
        me.getCommunicationServers().reload();
        model.load(null, {
                success: function (record) {
                    me.getSummary().setRecord(record.getSummary());
                    me.getOverview().bindStore(record.overviews());
                    me.getBreakdown().bindStore(record.breakdowns());
                    me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Ext.util.Format.date(new Date(), 'H:i'));
                },
                callback: function () {
                    me.getConnectionOverview().setLoading(false);
                }
            }
        );
    }
});

Ext.define('Dsh.model.Filter', {
    extend: 'Ext.data.Model',
    proxy: Ext.create('Uni.data.proxy.QueryStringProxy', { root: 'filter' }),
    fields: [
        { name: 'deviceGroup', type: 'auto' },
        { name: 'state', type: 'auto' },
        { name: 'latestStatus', type: 'auto' },
        { name: 'latestResult', type: 'auto' },
        { name: 'comPortPool', type: 'auto' },
        { name: 'connectionType', type: 'auto' },
        { name: 'deviceType', type: 'auto' },
        { name: 'startedBetween', type: 'auto' },
        { name: 'finishedBetween', type: 'auto' }
    ]
});

Ext.define('Dsh.view.widget.ConnectionsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.connections-list',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dsh.view.widget.ActionMenu'
    ],
    itemId: 'connectionslist',
    store: 'Dsh.store.ConnectionTasks',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'Device',
                text: Uni.I18n.translate('connection.widget.details.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return '<a href="#/devices/' + val.id + '">' + val.name + '</a>'
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('connection.widget.details.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val ? val.displayValue : ''
                }
            },
            {
                itemId: 'latestStatus',
                text: Uni.I18n.translate('connection.widget.details.latestStatus', 'DSH', 'Latest status'),
                dataIndex: 'latestStatus',
                flex:1,
                renderer: function (val) {
                    return val ? val.displayValue : ''
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                dataIndex: 'latestResult',
                name: 'latestResult',
                flex: 1,
                renderer: function (val) {
                    return val ? val.displayValue : ''

                }
            },
            {
                dataIndex: 'taskCount',
                itemId: 'taskCount',
                renderer: function (val) {
                    var success = val.numberOfSuccessfulTasks ? '<tpl><span class="fa fa-check fa-lg" style="color: green; position: relative; vertical-align: 0% !important;"></span><span style="position: relative; left: 4px">' + val.numberOfSuccessfulTasks + '</span></tpl>' : '',
                        failed = val.numberOfFailedTasks ? '<tpl><span class="fa fa-times fa-lg" style="color: red; position: relative; left: 26px; vertical-align: 0% !important;"></span><span style="position: relative; left: 30px">' + val.numberOfFailedTasks + '</span></tpl>' : '',
                        notCompleted = val.numberOfIncompleteTasks ? '<tpl><span class="fa fa-ban fa-lg" style="color: #333333; position: relative; left: 52px; vertical-align: 0% !important"></span><span  style="position: relative; left: 56px">' + val.numberOfIncompleteTasks + '</span></tpl>' : ''
                        ;
                    return success + failed + notCompleted
                },
                header: Uni.I18n.translate('connection.widget.details.commTasks', 'DSH', 'Communication tasks'),
                flex: 2
            },
            {
                itemId: 'startDateTime',
                text: Uni.I18n.translate('connection.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startDateTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 1
            },
            {
                itemId: 'endDateTime',
                text: Uni.I18n.translate('connection.widget.details.finishedOn', 'DSH', 'Finished on'),
                dataIndex: 'endDateTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn'
            }
        ]
    },
    dockedItems: [
        {
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
            store: 'Dsh.store.ConnectionTasks',
            displayMsg: Uni.I18n.translate('connection.widget.details.displayMsg', 'DDSH', '{0} - {1} of {2} connections'),
            displayMoreMsg: Uni.I18n.translate('connection.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} connections'),
            emptyMsg: Uni.I18n.translate('connection.widget.details.emptyMsg', 'DSH', 'There are no connections to display')
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            store: 'Dsh.store.ConnectionTasks',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('connection.widget.details.itemsPerPage', 'DSH', 'Connections per page')
        }
    ],

    addTooltip: function () {
        var me = this,
            view = me.getView(),
            tip = Ext.create('Ext.tip.ToolTip', {
                target: view.el,
                delegate: 'span.fa',
                trackMouse: true,
                renderTo: Ext.getBody(),
                listeners: {
                    beforeshow: function updateTipBody(tip) {
                        var res,
                            rowEl = Ext.get(tip.triggerElement).up('tr'),
                            taskCount = view.getRecord(rowEl).get('taskCount'),
                            failed = taskCount.numberOfFailedTasks + ' ' + Uni.I18n.translate('connection.widget.details.comTasksFailed', 'DSH', 'communication tasks failed'),
                            success = taskCount.numberOfSuccessfulTasks + ' ' + Uni.I18n.translate('connection.widget.details.comTasksSuccessful', 'DSH', 'communication tasks successful'),
                            notCompleted = taskCount.numberOfIncompleteTasks + ' ' + Uni.I18n.translate('connection.widget.details.comTasksNotCompleted', 'DSH', 'communication tasks not completed');
                        (tip.triggerElement.className.search('fa-ban') !== -1) && (res = notCompleted);
                        (tip.triggerElement.className.search('fa-check') !== -1) && (res = success);
                        (tip.triggerElement.className.search('fa-time') !== -1) && (res = failed);
                        tip.update(res);
                    }
                }
            }),
            ResultTip = Ext.create('Ext.tip.ToolTip', {
                target: view.el,
                delegate: 'td.x-grid-cell-headerId-latestResult',
                trackMouse: true,
                renderTo: Ext.getBody(),
                listeners: {
                    beforeshow: function () {
                        var rowEl = Ext.get(ResultTip.triggerElement).up('tr'),
                            latestResult = view.getRecord(rowEl).get('latestResult');
                        if (latestResult.retries) {
                            ResultTip.update(latestResult.retries + ' ' + Uni.I18n.translate('connection.widget.details.retries', 'DSH', 'retries'))
                        }
                    },
                    show: function () {
                        var rowEl = Ext.get(ResultTip.triggerElement).up('tr'),
                            latestResult = view.getRecord(rowEl).get('latestResult');
                        if (!latestResult.retries) {
                            ResultTip.hide()
                        }
                    }
                }
            });

    },

    initComponent: function () {
        var me = this;
        me.on('afterrender', me.addTooltip);
        me.callParent(arguments);
    }
});

Ext.define('Dsh.view.widget.common.SideFilterCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.side-filter-combo',
    displayField: 'localizedValue',
    valueField: 'name',
    editable: false,
    multiSelect: true,
    queryMode: 'local',
    triggerAction: 'all',
    listConfig: {
        getInnerTpl: function () {
            return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {localizedValue} </div>';
        }
    },
    initComponent: function () {
        var me = this;
        this.callParent(arguments);
        this.store = Ext.create('Ext.data.Store', {
            autoLoad: true,
            fields: [
                { name: 'name', type: 'string' },
                { name: 'localizedValue', type: 'string' }
            ],
            proxy: {
                type: 'ajax',
                url: me.url,
                reader: {
                    type: 'json',
                    root: 'data'
                }
            }
        });
    }
});

Ext.define('Dsh.view.widget.common.DateTimeField', {
    extend: 'Ext.form.FieldSet',
    alias: 'widget.datetime-field',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 30,
        labelAlign: 'left',
        labelStyle: 'font-weight: normal'
    },
    items: [
        {
            xtype: 'datefield',
            name: 'date',
            editable: false
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: '&nbsp;',
            style: 'padding-left: 30px',
            layout: 'hbox',
            defaultType: 'numberfield',
            defaults: {
                flex: 1,
                value: 0,
                minValue: 0,
                enforceMaxLength: true,
                maxLength: 2,
                enableKeyEvents: true,
                stripCharsRe: /\D/,
                listeners: {
                    blur: function (field) {
                        if (Ext.isEmpty(field.getValue())) {
                            field.setValue(0);
                        }
                    }
                }
            },
            items: [
                {
                    name: 'hours',
                    maxValue: 23,
                    style: {
                        marginRight: '5px'
                    },
                    valueToRaw: function (value) {
                        return value < 10 ? Ext.String.leftPad(value, 2, '0') :
                            value > 23 ? 23 : value;
                    }
                },
                {
                    name: 'minutes',
                    maxValue: 59,
                    style: {
                        marginLeft: '5px'
                    },
                    valueToRaw: function (value) {
                        return value < 10 ? Ext.String.leftPad(value, 2, '0') :
                            value > 59 ? 59 : value;
                    }
                }
            ]
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
        this.down('datefield').setFieldLabel(this.label);
    }
});

Ext.define('Dsh.view.widget.common.SideFilterDateTime', {
    extend: 'Ext.form.FieldSet',
    alias: 'widget.side-filter-date-time',
    requires: [
        'Dsh.view.widget.common.DateTimeField'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        border: 'none',
        padding: 0,
        margin: 0
    },
    defaults: {
        xtype: 'datetime-field',
        style: {
            border: 'none',
            padding: 0,
            margin: 0
        }
    },
    items: [
        { xtype: 'panel', name: 'header', baseCls: 'x-form-item-label', style: 'margin: 15px 0' },
        { label: Uni.I18n.translate('connection.widget.sideFilter.from', 'DSH', 'From'), name: 'from' },
        { label: Uni.I18n.translate('connection.widget.sideFilter.to', 'DSH', 'To'), name: 'to' }
    ],
    initComponent: function () {
        this.callParent(arguments);
        this.down('panel[name=header]').update(this.wTitle);
    }
});

Ext.define('Dsh.view.widget.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dsh-side-filter',
    requires: [
        'Uni.component.filter.view.Filter',
        'Dsh.view.widget.common.SideFilterCombo',
        'Dsh.view.widget.common.SideFilterDateTime'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    items: [
        {
            xtype: 'nested-form',
            ui: 'filter',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                xtype: 'side-filter-combo',
                labelAlign: 'top'
            },
            items: [
                {
                    itemId: 'device-group',
                    name: 'deviceGroup',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceGroup', 'DSH', 'Device group'),
                    url: '/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'current-state',
                    name: 'state',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
                    url: '/apps/dashboard/app/fakeData/CurrentStateFilterFake.json'
                },
                {
                    itemId: 'latest-status',
                    name: 'latestStatus',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestStatus', 'DSH', 'Latest status'),
                    url: '/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'latest-result',
                    name: 'latestResult',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
                    url: '/apps/dashboard/app/fakeData/LatestResultFake.json'
                },
                {
                    itemId: 'comport-pool',
                    name: 'comPortPool',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.comPortPool', 'DSH', 'Communication port pool'),
                    url: '/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'connection-type',
                    name: 'connectionType',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.connectionType', 'DSH', 'Connection type'),
                    url: '/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    itemId: 'device-type',
                    name: 'deviceType',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type'),
                    url: '/apps/dashboard/app/fakeData/BaseFilterFake.json'
                },
                {
                    xtype: 'side-filter-date-time',
                    itemId: 'started-between',
                    name: 'startedBetween',
                    wTitle: Uni.I18n.translate('connection.widget.sideFilter.startedBetween', 'DSH', 'Started between')
                },
                {
                    xtype: 'side-filter-date-time',
                    itemId: 'finished-between',
                    name: 'finishedBetween',
                    wTitle: Uni.I18n.translate('connection.widget.sideFilter.finishedBetween', 'DSH', 'Finished between')
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.apply', 'DSH', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.clearAll', 'DSH', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});

Ext.define('Dsh.view.widget.FilterPanel', {
    extend: 'Ext.panel.Panel',
    alias: "widget.connections-filter-panel",
    border: true,
    mixins: [
        'Uni.component.filter.view.RecordBounded'
    ],
    header: false,
    collapsible: false,
    hidden: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            title: Uni.I18n.translate('', 'MDC', 'Filter'),
            xtype: 'filter-toolbar',

            emptyText: 'None'
        },
        {
            xtype: 'menuseparator'
        }
    ]

});

Ext.define('Dsh.view.Connections', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connections-details',
    itemId: 'connectionsdetails',
    overflowY: 'auto',

    requires: [
        'Dsh.view.widget.ConnectionsList',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.view.widget.SideFilter',
        'Dsh.view.widget.FilterPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.connections.title', 'DSH', 'Connections')
        },
        {
            xtype: 'connections-filter-panel',
            itemId: 'dshconnectionsfilterpanel'
        },
        {
            xtype: 'preview-container',
            grid: {
                xtype: 'connections-list',
                itemId: 'connectionsdetails'
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('workspace.dataCommunication.connections.empty.title', 'DSH', 'No connections found'),
                reasons: [
                    Uni.I18n.translate('workspace.dataCommunication.connections.empty.list.item1', 'DSH', 'No connections in the system.'),
                    Uni.I18n.translate('workspace.dataCommunication.connections.empty.list.item2', 'DSH', 'No connections found due to applied filters.')
                ]
            },
            previewComponent: {
                xtype: 'preview_connection',
                itemId: 'connectionpreview'
            }
        },
        {
            ui: 'medium',
            itemId: 'comtaskstitlepanel',
            title: ''
        },
        {
            xtype: 'container',
            itemId: 'communicationcontainer'
        }
    ],

    side: [
        {
            xtype: 'dsh-side-filter',
            itemId: 'dshconnectionssidefilter'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

Ext.define('Dsh.model.ConnectionTask', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'device', type: 'auto' },
        { name: 'deviceConfiguration', type: 'auto' },
        { name: 'deviceType', type: 'auto' },
        {
            name: 'devConfig',
            persist: false,
            mapping: function (data) {
                var devConfig = {};
                devConfig.config = data.deviceConfiguration;
                devConfig.devType = data.deviceType;
                return devConfig
            }
        },
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                return data.connectionMethod.name + ' on ' + data.device.name
            }
        },
        { name: 'currentState', type: 'auto' },
        { name: 'latestStatus', type: 'auto' },
        { name: 'latestResult', type: 'auto' },
        { name: 'taskCount', type: 'auto' },
        { name: 'startDateTime', type: 'date', dateFormat: 'time'},
        { name: 'endDateTime', type: 'date', dateFormat: 'time'},
        { name: 'duration', type: 'auto' },
        { name: 'comPortPool', type: 'auto' },
        { name: 'direction', type: 'auto' },
        { name: 'connectionType', type: 'auto' },
        { name: 'comServer', type: 'auto' },
        { name: 'connectionMethod', type: 'auto' },
        { name: 'window', type: 'auto' },
        { name: 'connectionStrategy', type: 'auto' },
        { name: 'nextExecution', type: 'date', dateFormat: 'time'},
        'communicationTasks'
    ],
    hasOne: [
        {
            model: 'Dsh.model.CommTasks',
            name: 'communicationTasks'
        }
    ]
});

Ext.define('Dsh.model.CommTasks', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'count', type: 'auto' },
        'communicationTasks'
    ],
    hasMany: [
        {
            model: 'Dsh.model.CommunicationTask',
            name: 'communicationsTasks'
        }
    ]
});

Ext.define('Dsh.store.ConnectionTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.ConnectionTask'
    ],
    model: 'Dsh.model.ConnectionTask',
    proxy: {
//        type: 'ajax',
        type: 'rest',
//        url: '../../apps/dashboard/app/fakeData/ConnectionTasksFake.json',
        url: '/api/dsr/connections',
        reader: {
            type: 'json',
            root: 'connectionTasks',
            totalProperty: 'total'
        }
    }
});


Ext.define('Dsh.controller.Connections', {
    extend: 'Ext.app.Controller',

    models: [
        'Dsh.model.ConnectionTask',
        'Dsh.model.CommTasks',
        'Dsh.model.CommunicationTask',
        'Dsh.model.Filter'
    ],

    stores: [
        'Dsh.store.ConnectionTasks',
        'Dsh.store.CommunicationTasks'
    ],

    views: [
        'Dsh.view.Connections',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.view.widget.CommunicationsList',
        'Dsh.view.widget.PreviewCommunication'
    ],

    refs: [
        {
            ref: 'connectionsList',
            selector: '#connectionsdetails'
        },
        {
            ref: 'connectionPreview',
            selector: '#connectionpreview'
        },
        {
            ref: 'communicationList',
            selector: '#communicationsdetails'
        },
        {
            ref: 'communicationContainer',
            selector: '#communicationcontainer'
        },
        {
            ref: 'communicationPreview',
            selector: '#communicationpreview'
        },
        {
            ref: 'commTasksTitle',
            selector: '#comtaskstitlepanel'
        },
        {
            ref: 'filterPanel',
            selector: '#dshconnectionsfilterpanel'
        },
        {
            ref: 'sideFilterForm',
            selector: '#dshconnectionssidefilter form'
        }
    ],

    init: function () {
        this.control({
            '#connectionsdetails': {
                selectionchange: this.onSelectionChange
            },
            '#communicationsdetails': {
                selectionchange: this.onCommunicationSelectionChange
            },
            '#dshconnectionssidefilter button[action=applyfilter]': {
                click: this.applyFilter
            },
            '#dshconnectionssidefilter': {
                afterrender: this.loadFilterValues
            }
        });
        this.callParent(arguments);
    },

    showOverview: function () {
        var me = this,
            widget = Ext.widget('connections-details');
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    loadFilterValues: function () {
        var me = this;

        // todo: get rid of this delay
        Ext.defer(function () {
            Dsh.model.Filter.load(0, {
                callback: function (record) {
                    !record && (record = new Dsh.model.Filter);
                    me.getSideFilterForm().loadRecord(record);
                    me.getFilterPanel().loadRecord(record);
                }
            });
        }, 3500);
    },

    onCommunicationSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0],
            preview = me.getCommunicationPreview();
        record.data.devConfig = {
            config: record.data.deviceConfiguration,
            devType: record.data.deviceType
        };
        record.data.title = record.data.name + ' on ' + record.data.device.name;
        preview.setTitle(record.data.title);
        preview.loadRecord(record);
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0],
            preview = me.getConnectionPreview(),
            commTasksData = record.get('communicationTasks').communicationsTasks,
            commTasks = Ext.create('Ext.data.Store', {model: 'Dsh.model.CommunicationTask', data: commTasksData});
        preview.loadRecord(record);
        preview.setTitle(record.get('title'));
        me.getCommunicationContainer().removeAll(true);
        me.getCommunicationContainer().add({
            xtype: 'preview-container',
            grid: {
                xtype: 'communications-list',
                itemId: 'communicationsdetails',
                store: commTasks
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('communication.widget.details.empty.title', 'DSH', 'No communications foundю'),
                reasons: [
                    Uni.I18n.translate('communication.widget.details.empty.list.item1', 'DSH', 'No communications in the system.'),
                    Uni.I18n.translate('communication.widget.details.empty.list.item2', 'DSH', 'No communications found due to applied filters.')
                ]
            },
            previewComponent: {
                xtype: 'preview_communication',
                itemId: 'communicationpreview'
            }
        });
        me.getCommTasksTitle().setTitle(Uni.I18n.translate('communication.widget.details.commTasksOf', 'DSH', 'Communication tasks of') + ' ' + record.get('title'));
        me.getCommunicationList().getSelectionModel().select(0);
    },

    applyFilter: function () {
        var me = this;
        me.getSideFilterForm().updateRecord();
        var model = me.getSideFilterForm().getRecord();
        model.save();
    }
});

//TODO: localize all strings
Ext.define('Dsh.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,
    routeConfig: {
        workspace: {
            title: 'Workspace',
            route: 'workspace',
            disabled: true,
            items: {
                datacommunication: {
                    title: 'Data communication',
                    route: 'datacommunication',
                    items: {
                        communication: {
                            title: 'Communication overview',
                            route: 'communication',
                            controller: 'Dsh.controller.CommunicationOverview',
                            action: 'showOverview'
                        },
                        communications: {
                            title: 'Communications',
                            route: 'communications',
                            controller: 'Dsh.controller.Communications',
                            action: 'showOverview'
                        },
                        connection: {
                            title: 'Connection overview',
                            route: 'connection',
                            controller: 'Dsh.controller.ConnectionOverview',
                            action: 'showOverview'
                        },
                        connections: {
                            title: 'Connections',
                            route: 'connections',
                            controller: 'Dsh.controller.Connections',
                            action: 'showOverview'
                        }
                    }
                }
            }
        }
    }
});

Ext.define('Dsh.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.Configuration',
        'Uni.controller.history.EventBus',
        'Uni.model.PortalItem',
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Dsh.controller.history.Workspace',
        'Dsh.controller.CommunicationOverview',
        'Dsh.controller.ConnectionOverview',
        'Dsh.controller.Connections'
    ],

    config: {
        navigationController: null,
        configurationController: null
    },

    init: function () {
        this.initNavigation();
        this.initMenu();
    },

    initNavigation: function () {
        var navigationController = this.getController('Uni.controller.Navigation'),
            configurationController = this.getController('Uni.controller.Configuration');
        this.setNavigationController(navigationController);
        this.setConfigurationController(configurationController);
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            historian = me.getController('Dsh.controller.history.Workspace'); // Forces route registration.

        Uni.store.MenuItems.add(
            Ext.create('Uni.model.MenuItem', {
                text: 'Workspace',
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            })
        );

        Uni.store.PortalItems.add(
            Ext.create('Uni.model.PortalItem', {
                title: 'Data communication',
                portal: 'workspace',
                route: 'datacommunication',
                items: [
                    {
                        text: 'Connections',
                        href: router.getRoute('workspace/datacommunication/connections').buildUrl()
                    },
                    {
                        text: 'Connection overview',
                        href: router.getRoute('workspace/datacommunication/connection').buildUrl()
                    },
                    {
                        text: 'Communications',
                        href: router.getRoute('workspace/datacommunication/communications').buildUrl()
                    },
                    {
                        text: 'Communication overview',
                        href: router.getRoute('workspace/datacommunication/communication').buildUrl()
                    }
                ]
            })
        );
    }
});

Ext.define('Dsh.model.Configuration', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'string' },
        { name: 'title', type: 'string' }
    ]
});


Ext.define('Dsh.model.Result', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'string' },
        { name: 'displayName', type: 'string' },
        { name: 'count', type: 'int' }
    ]
});

Ext.define('Dsh.model.TimeInfo', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'count', type: 'int' },
        { name: 'timeUnit', type: 'int' }
    ]
});

