Ext.define('Dsh.controller.BaseController', {
    extend: 'Ext.app.Controller',
    prefix: '',

    init: function () {
        if (this.prefix) {
            var control = {};

            control[this.prefix + ' filter-top-panel'] = {
                removeFilter: this.removeFilter,
                clearAllFilters: this.clearFilter
            };
            control[this.prefix + ' #filter-form side-filter-combo'] = {
                updateTopFilterPanelTagButtons: this.onFilterChange
            };
            control[this.prefix + ' button[action=applyfilter]'] = {
                click: this.applyFilter
            };
            control[this.prefix + ' button[action=clearfilter]'] = {
                click: this.clearFilter
            };
            this.control(control);
        }

        this.callParent(arguments);
    },

    initFilter: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getSideFilterForm().loadRecord(router.filter);

        // todo: refactor this
        this.setFilterTimeInterval(router.filter.startedBetween, 'started', 'startedBetween');
        this.setFilterTimeInterval(router.filter.finishedBetween, 'finished', 'finishedBetween');
    },

    // todo: refactor this also
    setFilterTimeInterval: function (interval, prefix, propName) {
        var value = '',
            intervalFrom = interval ? interval.get('from') : undefined,
            intervalTo = interval ? interval.get('to') : undefined,
            defaultLabel = prefix.charAt(0).toUpperCase() + prefix.slice(1),
            label = Uni.I18n.translate('connection.widget.' + prefix, 'DSH', defaultLabel),
            between = Uni.I18n.translate('connection.widget.between', 'DSH', 'between'),
            after = Uni.I18n.translate('connection.widget.after', 'DSH', 'after'),
            before = Uni.I18n.translate('connection.widget.before', 'DSH', 'before'),
            and = Uni.I18n.translate('connection.widget.and', 'DSH', 'and');

        if (interval && (intervalFrom || intervalTo)) {
            if (intervalFrom && intervalTo) {
                value += ' ' + between + ' ';
                value += Uni.DateTime.formatDateTimeShort(intervalFrom);
                value += ' ' + and + ' ';
                value += Uni.DateTime.formatDateTimeShort(intervalTo);
            }
            if (intervalFrom && !intervalTo) {
                value += Uni.DateTime.formatDateTimeShort(intervalFrom);
            }
            if (!intervalFrom && intervalTo) {
                value += Uni.DateTime.formatDateTimeShort(intervalTo);
            }
            this.getFilterPanel().setFilter(propName, label, value);
        }
    },

    onFilterChange: function (combo) {
        if (!_.isEmpty(combo.getRawValue())) {
            this.getFilterPanel().setFilter(combo.getName(), combo.getFieldLabel(), combo.getRawValue());
        }
    },

    applyFilter: function () {
        this.getSideFilterForm().updateRecord();
        this.getSideFilterForm().getRecord().save();
    },

    clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        switch (key) {
            case 'startedBetween':
                delete record.startedBetween;
                break;
            case 'finishedBetween':
                delete record.finishedBetween;
                break;
            default:
                record.set(key, null);
        }

        record.save();
    }
});

Ext.define('Dsh.view.widget.HeaderSection', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.header-section',
    itemId: 'header-section',
    layout: 'fit',
    router: null,
    ui: 'large',

    initComponent: function () {
        var me = this;
        me.title = me.router.getRoute().title;
        this.items = [
            {
                xtype: 'toolbar',
                items: [
                    {
                        xtype: 'device-group-filter',
                        router: me.router
                    },
                    '->',
                    {
                        xtype: 'displayfield',
                        itemId: 'last-updated-field',
                        style: 'margin-right: 10px'
                    },
                    {
                        xtype: 'button',
                        itemId: 'refresh-btn',
                        style: {
                            'background-color': '#71adc7'
                        },
                        text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                        icon: '/apps/sky/resources/images/form/restore.png'
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
        '<tpl if="threshold">',
        '<div class="threshold" style="left: {threshold}%;">',
        '</tpl>',
        '<div>',
        '</tpl>'
    ],

    prepareData: function () {
        var me = this;
        return {
            limit: !me.total ? 0 : Math.round(me.limit * 100 / me.total),
            label: me.label,
            count: !me.limit ? 0 : Math.round(me.count * 100 / me.limit),
            threshold: me.threshold
        }
    },

    initRenderData: function () {
        var me = this;

        var track = new Ext.XTemplate(me.trackTpl);
        var data = Ext.apply(me.callParent(), me.prepareData());
        return Ext.apply(data, {track: track.apply(data)});
    }
});

Ext.define('Dsh.view.widget.Summary', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    requires: ['Dsh.view.widget.common.Bar'],
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
                '<a id="label-{displayName}" href="{href}">{displayName}</a><tpl else>{displayName}</tpl>' +
                '</td>' +
                '<td width="100%" id="bar-{[parentIndex]}" class="bar-{[parentIndex]} bar-{name}"></td>' +
                '</tr>' +
                '<tpl for="counters">' +
                '<tr class="child">' +
                '<td class="label">{displayName}</td>' +
                '<td width="100%" id="bar-{[parentIndex]}-{#}" class="bar-{[parentIndex]}-{#} bar-{name}"></td>' +
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
                                    bar.render(node.querySelector('.bar-' + pos + '-' + (idx + 1)));
                                });
                            }

                            var bar = Ext.widget('bar', {
                                limit: view.total,
                                total: view.total,
                                count: record.get('count'),
                                label: Math.round(!view.total ? 0 : record.get('count') * 100 / view.total) + '% (' + record.get('count') + ')'
                            });
                            bar.render(node.querySelector('.bar-' + pos));
                        });
                        view.updateLayout();
                        Ext.resumeLayouts(true);
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
        me.setTitle('<h3>' + Uni.I18n.translatePlural('overview.widget.' + me.parent + '.header', total, 'DSH', me.wTitle + ' ({0}))' + '</h3>');
    },

    initKpi: function (record) {
        var me = this,
            targetContainer = me.down('#target-container'),
            total = record.get('total'),
            target = record.get('target'),
            counters = record.counters();

        var success = counters.getAt(counters.findBy(function (r) {
            return r.get('name') === 'success'
        }));
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
        Ext.resumeLayouts(true);
    }
});

Ext.define('Dsh.view.widget.CommunicationServers', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communication-servers',
    store: 'CommunicationServerInfos',
    ui: 'tile',

    tbar: {
        xtype: 'container',
        itemId: 'connection-summary-title-panel'
    },

    items: [
        {
            xtype: 'dataview',
            itemId: 'servers-dataview',
            itemSelector: 'tbody.comserver',
            emptyText: Uni.I18n.translate('overview.widget.communicationServers.noServersFound', 'DSH', 'No communication servers found'),
            tpl: new Ext.XTemplate(
                '<table  style="margin: 5px 0 10px 0">',
                '<tpl for=".">',
                '<tbody class="comserver">',
                '<tpl if="!values.expand">',
                '<tpl if="children">',
                '<tr>',
                '<td style="padding-right: 5px;"><img src="/apps/dsh/resources/images/widget/{name}.png" /></td>',
                '<td>{children.length} {title}</td>',
                '<td style="padding-left: 15px;"><img data-qtitle="{children.length} {title}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" src="/apps/sky/resources/images/shared/icon-info-small.png" /></td>',
                '</tr>',
                '<tpl else>',
                '<tr>',
                '<td style="padding-right: 5px;"><img src="/apps/dsh/resources/images/widget/{name}.png" /></td>',
                '<td>{title}</td>',
                '<td style="padding-left: 15px;"></td>',
                '</tr>',
                '</tpl>',
                '<tpl else>',
                '<tpl for="values.children">',
                '<tr id="{comServerId}">',
                '<td style="padding-right: 5px;"><img src="/apps/dsh/resources/images/widget/{[parent.name]}.png" /></td>',
                '<td><a href="{href}">{title}</a></td>',
                '<td style="padding-left: 15px;"><img data-qtitle="{title}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" src="/apps/sky/resources/images/shared/icon-info-small.png" /></td>',
                '</tr>',
                '</tpl>',
                '</tpl>',
                '</tbody>',
                '</tpl>',
                '</table>'
            )
        },
        {
            xtype: 'container',
            itemId: 'target-container',
            layout: 'vbox',
            style: {
                marginRight: '20px'
            },
            items: []
        }
    ],

    serverTpl: new Ext.XTemplate(
        '<table>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.server.name', 'DSH', 'Name') + '</td>',
        '<td>{comServerName}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.server.type', 'DSH', 'Type') + '</td>',
        '<td>{comServerType}</td>',
        '</tr>',
        '<tpl if="blockedSince">',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.communicationServers.tt.downSince', 'DSH', 'Not responding since') + '</td>',
        '<td>{[Ext.util.Format.date(new Date(values.blockedSince), "D M j, Y G:i")]}</td>',
        '</tr>',
        '</tpl>',
        '</table>'
    ),

    reload: function () {
        var me = this,
            targetContainer = me.down('#target-container'),
            elm = me.down('#servers-dataview'),
            store = Ext.getStore(me.store);

        me.setLoading();
        targetContainer.removeAll();
        store.load(function () {
            var title = '<h3>' + Uni.I18n.translatePlural('overview.widget.communicationServers.header', store.count(), 'DSH', 'Active communication servers ({0})') + '</h3>';
            if (me.down('#connection-summary-title-panel')) {
                me.down('#connection-summary-title-panel').update(title);
            }

            var groups = store.getGroups().map(function (item) {
                item.title = Uni.I18n.translate('overview.widget.communicationServers.title.' + item.name, 'DSH', item.name);
                item.expand = (item.name === 'blocked' && item.children && item.children.length < 5);
                var html = '';
                if (item.children) {
                    item.children = item.children.map(function (server) {
                        var data = server.getData();
                        data.title = data.comServerName + ' ' + Uni.I18n.translate('overview.widget.communicationServers.status.' + item.name, 'DSH', item.name);
                        data.href = me.router.getRoute('administration/comservers/detail/overview').buildUrl({id: data.comServerId});
                        data.tooltip = me.serverTpl.apply(data);
                        html += data.tooltip;
                        return data;
                    });
                }
                item.tooltip = html;

                return item;
            });

            elm.bindStore(Ext.create('Ext.data.Store', {
                fields: ['children', 'name', 'title', 'tooltip', 'expand'],
                data: groups
            }));

            targetContainer.add(
                {
                    xtype: 'button',
                    itemId: 'lnk-view-all-communication-servers',
                    ui: 'link',
                    text: Uni.I18n.translate('overview.widget.communicationServers.viewAll', 'DSH', 'View all'),
                    href: typeof me.router.getRoute('administration/comservers') !== 'undefined'
                        ? me.router.getRoute('administration/comservers').buildUrl() : ''
                }
            );

            me.setLoading(false);
        });
    }
});

Ext.define('Dsh.view.widget.QuickLinks', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.quick-links',
    data: [],
    items: [
        {
            itemId: 'quicklinksTplPanel',
            tpl: new Ext.XTemplate(
                '<div class="quick-links">',
                '<h3>' + Uni.I18n.translate('overview.widget.quicklinks.title', 'DSH', 'Quick links') + '</h3>',
                '<ul>',
                '<tpl for=".">',
                '<tpl if="href">',
                '<li><a href="{href}"',
                '<tpl if="target">', // yellowfin reports are shown in a new tab.
                ' target="{target}"',
                '</tpl>',
                '>{link}</a></li>',
                '</tpl>',
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
    hidden: true,
    layout: 'fit',
    yLabel: '',

    initComponent: function () {
        var me = this;
        this.tbar = [
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
                height: 30,
                html: '<svg id="read-outs-legend-container" style="width: 100%"></svg>'
            }
        ];

        this.items = [
            {
                hidden: true,
                xtype: 'container',
                itemId: 'empty',
                html: 'Connections over time are not measured for the selected group'
            },
            {
                xtype: 'container',
                flex: 1,
                hidden: true,
                itemId: 'chart',
                height: 400,
                listeners: {
                    resize: {
                        fn: function () {
                            if (me.chart) {
                                me.chart.setSize(Ext.getBody().getViewSize().width - 100, 400);
                                me.doLayout();
                            }
                        }
                    }
                }
            }
        ];

        this.callParent(arguments);
    },

    colorMap: {
        0: '#70BB52', // success
        1: '#71ADC6', // ongoing
        2: '#EB5642', // failed
        3: '#a9a9a9'  // target
    },

    setRecord: function (record) {
        var me = this;
        var filter = me.router.filter;

        if (!filter.get('deviceGroup') || !record) {
            me.hide();
        } else {
            me.show();
            var container = me.down('#chart');
            var empty = me.down('#empty');

            if (record.get('time')) {
                container.show();
                empty.hide();
                me.renderChart(container);
                // clean up
                me.chart.series.map(function (obj) {
                    obj.remove()
                });
                record.series().each(function (kpi, idx) {
                    var series = kpi.getData();
                    series.color = me.colorMap[idx];
                    series.data = _.zip(record.get('time'), series.data);
                    me.chart.addSeries(series);
                });
            } else {
                container.hide();
                empty.show();
            }
        }
    },

    renderChart: function (container) {
        var me = this;
        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });
        this.chart = new Highcharts.Chart({
                chart: {
                    type: 'spline',
                    zoomType: 'x',
                    renderTo: container.el.dom,
                    reflow: false,
                    width: Ext.getBody().getViewSize().width - 100,
                    height: 400,
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
                    positioner: function (labelWidth, labelHeight, point) {
                        var yValue,
                            additionalY;

                        if (point.plotY < 0) {
                            additionalY = 0;
                        } else {
                            additionalY = point.plotY;
                        }

                        yValue = point.plotY > labelHeight ? point.plotY - labelHeight : additionalY + labelHeight / 2;
                        return {x: point.plotX, y: yValue}
                    },
                    valueSuffix: '%'
                },
                plotOptions: {
                    series: {animation: false},
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
                    type: 'datetime',
                    gridLineDashStyle: 'Dot',
                    gridLineWidth: 1,
                    dateTimeLabelFormats: {
                        second: '%H:%M<br/>%a %e %b',
                        minute: '%H:%M<br/>%a %e %b',
                        hour: '%H:%M<br/>%a %e %b',
                        day: '%H:%M<br/>%a %e %b',
                        week: '%a %e<br/>%b %Y',
                        month: '%b<br/>%Y',
                        year: '%Y'
                    }
                },

                yAxis: {
                    title: {
                        text: me.yLabel
                    },
                    labels: {
                        format: '{value}%'
                    },
                    lineWidth: 2,
                    tickWidth: 1,
                    floor: 0,
                    ceiling: 100,
                    tickInterval: 10
                }
            }, function () {
                me.doLayout();
            }
        );
    }
});

Ext.define('Dsh.view.widget.Overview', {
    extend: 'Ext.panel.Panel',
    requires: ['Dsh.view.widget.common.Bar'],
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
                    var href = me.router.getRoute('workspace/' + me.parent + '/details').buildUrl(null, {filter: filter});
                    record.set('href', href);
                }
            });

            var panel = Ext.create('Ext.panel.Panel', {
                style: {padding: '20px', marginRight: !(idx % 2) ? '20px' : 0},
                ui: 'tile',
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

Ext.define('Dsh.view.widget.common.StackedBar', {
    alias: 'widget.stacked-bar',
    extend: 'Dsh.view.widget.common.Bar',

    tooltipTpl: '<table><tpl foreach="."><tr><td>{[Uni.I18n.translate("overview.widget.breakdown." + xkey, "DSH", xkey)]}</td><td>{.}</td></tr></tpl></table>',
    trackTpl: [
        '<div data-qtip="{tooltip}">',
        '<tpl foreach="count">',
        '<div class="{parent.baseCls}-track {parent.baseCls}-track-stacked {[xkey]}" style="width: {.}%;"></div>',
        '</tpl>',
        '</div>'
    ],

    prepareData: function () {
        var me = this;
        var counts = _.object(_.map(me.count, function (value, key) {
            return [key, !me.limit ? 0 : value * 100 / me.limit];
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
    items: {
        xtype: 'panel',
        ui: 'tile',
        minHeight: '300',
        itemId: 'heatmapchart'
    },

    setChartData: function (data) {
        var me = this;
        me.chart.series[0].setData([], true);
        me.chart.series[0].setData(data, true);
    },

    setXAxis: function (categories, title) {
        var me = this;
        title = title[0].toUpperCase() + title.slice(1);
        me.chart.series[0].xAxis.update({title: {text: title}}, false);
        me.chart.series[0].xAxis.update({categories: categories}, false);
    },

    setYAxis: function (categories, title) {
        var me = this;
        title = title[0].toUpperCase() + title.slice(1);
        me.chart.series[0].yAxis.update({title: {text: title}}, false);
        me.chart.series[0].yAxis.update({categories: categories}, false);
    },

    findBorders: function (store) {
        var x = 0,
            y = 0,
            max = 0,
            totalCount = 0;


        store.each(function (rec) {
            Ext.each(rec.data.data, function (item) {
                var count = item.count,
                    value = (count == 0 ? count.toString() : count);

                totalCount += parseInt(value);
                max = max < parseInt(value) ? parseInt(value) : max;

                ++y;
            });
            y = 0;
            ++x;
        });
        return {max: max, total: totalCount};
    },

    storeToHighchartData: function (store) {
        var data = [],
            x = 0,
            y = 0;

        store.each(function (rec) {
            Ext.each(rec.data.data, function (item) {
                var count = item.count,
                    value = (count == 0 ? count.toString() : count);

                // color of first column is green
                if (y === 0) {
                    value = -value;
                }

                data.push([y, x, value]);
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
            store.sort([
                {property: 'displayValue', direction: 'DESC'}
            ]);
            var me = this,
                ycat = [],
                xcat = store.collect('displayValue');

            Ext.each(store.getAt(0).data.data, function (item) {
                ycat.push(item.displayName);
            });
            me.setXAxis(ycat, 'Latest result');
            me.setYAxis(xcat, xTitle);
            me.setChartData(me.storeToHighchartData(store));
        }
    },

    initComponent: function () {
        var me = this;

        me.store = Ext.getStore(me.store || 'ext-empty-store');

        if (me.parent == 'connections') {
            var combineStore = Ext.create('Dsh.store.CombineStore');
            combineStore.load();
            me.tbar = [
                {
                    xtype: 'container',
                    itemId: 'title',
                    html: '<h2>' + Uni.I18n.translate('overview.widget.connections.heatmap.combineLabel', 'DSH', 'Combine latest result and') + '&nbsp;</h2>'
                },
                {
                    xtype: 'combobox',
                    labelWidth: 200,
                    itemId: 'combine-combo',
                    displayField: 'localizedValue',
                    queryMode: 'local',
                    valueField: 'breakdown',
                    store: combineStore,
                    autoSelect: true,
                    editable: false
                }
            ]
        } else if (me.parent == 'communications') {
            me.tbar = [
                {
                    xtype: 'container',
                    itemId: 'title',
                    html: '<h2>' + Uni.I18n.translate('overview.widget.communications.heatmap.combineLabel', 'DSH', 'Combine latest result and device type') + '</h2>'
                }
            ];
        }
        this.callParent(arguments);

        if (me.parent == 'connections') {
            var combo = me.getCombo();
            combo.on('change', function (combo, newValue) {
                me.store.addFilter({property: 'breakdown', value: newValue}, false);
                me.reload();
            });

            combo.getStore().load(function () {
                if (combo.getStore().getCount() > 0) {
                    combo.select(combo.getStore().getAt(1));
                }
            })
        } else if (me.parent == 'communications') {
            me.reload();
        }
    },

    reload: function () {
        var me = this,
            store = me.store,
            chartHeight;

        store.load({
            callback: function () {
                var cmp = me.down('#heatmapchart');
                if (store.count() && cmp) {
                    chartHeight = 80 + store.count() * 50
                    cmp.setHeight(chartHeight);
                    me.renderChart(cmp.getEl().down('.x-panel-body').dom, me.findBorders(store), chartHeight);
                    me.loadChart(store, me.getCombo() ? me.getCombo().getDisplayValue() : 'Device type');
                    me.show();
                    me.doLayout();
                } else {
                    me.hide();
                }
            }
        });
    },

    renderChart: function (container, borders, chartHeight) {
        var me = this;
        this.chart = new Highcharts.Chart({
            chart: {
                type: 'heatmap',
                renderTo: container,
                reflow: false,
                height: chartHeight
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
                },
                opposite: true
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
                stops: [
                    [0, '#70BB51'],
                    [0.5, '#ffffff'],
                    [1, '#EB5642']
                ],
                min: -borders.max,
                max: borders.max
            },
            legend: {
                enabled: false
            },
            tooltip: {
                useHTML: true,

                formatter: function () {
                    var label,
                        s;

                    switch (me.parent) {
                        case 'connections':
                            label = Uni.I18n.translate('overview.widget.summary.connections', 'DSH', 'connections');
                            break;
                        case 'communications':
                            label = Uni.I18n.translate('overview.widget.summary.communications', 'DSH', 'communications');
                            break;
                    }

                    s = '<table><tbody>'
                    s += '<tr>'
                    s += '<td style="padding-right: 10px; text-align: right">' + Uni.I18n.translate('overview.widget.summary.numberOf', 'DSH', 'Number of') + ' ' + label + '</td>';
                    s += '<td style="padding-right: 1px; text-align: left"><b>' + Math.abs(this.point.value) + '</b></td>';
                    s += '</tr>'
                    s += '<tr>'
                    s += '<td style="padding-right: 10px; text-align: right">' + me.chart.options.yAxis[0].title.text + '</td>';
                    s += '<td style="padding-right: 1px; text-align: left"><b>' + this.series.yAxis.categories[this.point.y] + '</b></td>';
                    s += '</tr>'
                    s += '<tr>'
                    s += '<td style="padding-right: 10px; text-align: right">' + me.chart.options.xAxis[0].title.text + '</td>';
                    s += '<td style="padding-right: 1px; text-align: left"><b>' + this.series.xAxis.categories[this.point.x] + '</b></td>';
                    s += '</tr>'
                    s += '</tbody></table>';

                    return s
                }
            },
            series: [
                {
                    name: 'Latest Result',
                    borderWidth: 1,
                    dataLabels: {
                        enabled: true,
                        style: {
                            color: 'black',
                            fontWeight: 'normal',
                            fontSize: 12,
                            HcTextStroke: '0px rgba(0, 0, 0, 0.5)'

                        },
                        formatter: function () {
                            if (this.point.value > 0) {
                                return '<b>' + ((this.point.value / borders.total) * 100).toFixed(1) + '%</b>';
                            } else if (this.point.value < 0) {
                                return ((Math.abs(this.point.value) / borders.total) * 100).toFixed(1) + '%';
                            } else {
                                return '0%';
                            }
                        }
                    },
                    states: {
                        hover: {
                            color: '#CBCBCB',
                            borderColor: '#CBCBCB'
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
                    html: '<h3>' + item.get('displayName') + '</h3>'
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
                    '<div style="overflow: hidden; text-overflow: ellipsis; padding-right: 20px">{displayName}</div>' +
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
                                view.getEl().down('.item-' + pos + ' a').set({href: href});
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
        padding: '0 20px'
    },
    defaults: {
        style: {
            marginBottom: '20px',
            padding: 0
        }
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'header-section',
                router: me.router,
                style: 'none'
            },
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    },
                    flex: 1
                },
                items: [
                    {
                        xtype: 'summary',
                        flex: 2,
                        wTitle: Uni.I18n.translate('communication.widget.summary.title', 'DSH', 'Communications summary'),
                        parent: 'communications',
                        router: me.router
                    },
                    {
                        xtype: 'communication-servers',
                        itemId: 'communication-servers',
                        router: me.router
                    },
                    {
                        xtype: 'quick-links',
                        itemId: 'quick-links',
                        maxHeight: 256,
                        overflowY: 'auto',
                        style: {
                            marginRight: '0',
                            padding: '20px'
                        },
                        data: [
                            {
                                link: Uni.I18n.translate('communication.widget.quicklinks.viewAll', 'DSH', 'View all communications'),
                                href: me.router.getRoute('workspace/communications/details').buildUrl(null, me.router.queryParams)
                            },
                            {
                                link: me.router.getRoute('workspace/connections').title,
                                href: me.router.getRoute('workspace/connections').buildUrl(null, me.router.queryParams)
                            },
                            {
                                link: Uni.I18n.translate('communication.widget.quicklinks.myIssues', 'DSH', 'My open issues'),
                                href: typeof me.router.getRoute('workspace/datacollectionissues') !== 'undefined'
                                    ? me.router.getRoute('workspace/datacollectionissues').buildUrl(null, me.router.queryParams) + '?myopenissues=true' : null
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'read-outs-over-time',
                wTitle: Uni.I18n.translate('communications.widget.readOutsOverTime.title', 'DSH', 'Communications over time'),
                yLabel: Uni.I18n.translate('communications.widget.readOutsOverTime.yLabel', 'DSH', 'Number of communications'),
                router: me.router,
                parent: 'communications'
            },
            {
                xtype: 'overview',
                category: 'Communication',
                parent: 'communications',
                router: me.router
            },
            {
                xtype: 'breakdown',
                parent: 'communications',
                router: me.router
            },
            {
                xtype: 'heat-map',
                itemId: 'heatmap',
                store: 'Dsh.store.CommunicationResultsStore',
                router: me.router,
                parent: 'communications'
            }
        ];
        this.callParent(arguments);
    }
});

Ext.define('Dsh.model.Filterable', {
    extend: 'Ext.data.Model',
    requires: ['Ext.data.writer.Json'],

    inheritableStatics: {
        setFilter: function (model) {
            var proxy = this.getProxy();
            var writer = Ext.create('Ext.data.writer.Json', {
                writeRecordId: false
            });
            var data = _.map(writer.getRecordData(model), function (value, key) {
                return {property: key, value: value};
            });

            proxy.setExtraParam('filter', Ext.encode(_.filter(data, function (item) {
                return !!item.value
            })));
        }
    }
});

Ext.define('Dsh.model.Counter', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'count', type: 'int'},
        {name: 'alias', type: 'string'},
        {name: 'displayName', type: 'string'},
        {
            name: 'name', type: 'string', mapping: function (data) {
            return Ext.isString(data.name) ? data.name.toLowerCase() : ''
        }
        },
        {name: 'total', type: 'int'}
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
        {name: 'total', type: 'int'},
        {name: 'target', type: 'int'},
        {name: 'alias', type: 'string'}
    ],
    hasMany: {
        model: 'Dsh.model.Counter',
        name: 'counters'
    }
});

Ext.define('Dsh.model.BreakdownCounter', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'displayName', type: 'string'},
        {name: 'successCount', type: 'int'},
        {name: 'failedCount', type: 'int'},
        {name: 'pendingCount', type: 'int'},
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
        {name: 'displayName', type: 'string'},
        {name: 'alias', type: 'string'},
        {name: 'total', type: 'int'},
        {name: 'totalSuccessCount', type: 'int'},
        {name: 'totalPendingCount', type: 'int'},
        {name: 'totalFailedCount', type: 'int'}
    ],
    hasMany: {
        model: 'Dsh.model.BreakdownCounter',
        name: 'counters'
    }
});

Ext.define('Dsh.model.Series', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'data'}
    ]
});

Ext.define('Dsh.model.Kpi', {
    extend: 'Ext.data.Model',
    requires: ['Dsh.model.Series'],
    fields: [
        {name: 'time'}
    ],
    hasMany: {
        model: 'Dsh.model.Series',
        name: 'series'
    }
});

Ext.define('Dsh.model.communication.Overview', {
    extend: 'Dsh.model.Filterable',
    requires: [
        'Dsh.model.Summary',
        'Dsh.model.Counter',
        'Dsh.model.Breakdown',
        'Dsh.model.Kpi'
    ],
    hasOne: [
        {
            model: 'Dsh.model.Summary',
            associationKey: 'communicationSummary',
            name: 'summary',
            getterName: 'getSummary',
            setterName: 'setSummary'
        },
        {
            model: 'Dsh.model.Kpi',
            associationKey: 'kpi',
            name: 'kpi',
            getterName: 'getKpi',
            setterName: 'setKpi'
        }
    ],
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
        url: '/api/dsr/communicationoverview'
    }
});

Ext.define('Dsh.model.CommunicationServerInfo', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'comServerId', type: 'int'},
        {name: 'comServerName', type: 'string'},
        {name: 'comServerType', type: 'string'},
        {name: 'running', type: 'boolean'},
        {name: 'blocked', type: 'boolean'},
        {name: 'blockTime', type: 'auto'},
        {
            name: 'status', type: 'string', convert: function (v, record) {
            //Blocked: All communication servers with attributes "running:true" and "blocked:true"
            //Stopped: All communication servers with attribute "running:false"
            //Running: All communication servers with attributes "running:true" and "blocked:false"
            if (record.get('running')) {
                return record.get('blocked') ? 'blocked' : 'running';
            } else {
                return 'stopped';
            }
        }
        }
    ],
    associations: [
        {name: 'blockTime', type: 'hasOne', model: 'Dsh.model.TimeInfo', associationKey: 'blockTime'}
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
    autoLoad: false,

    groupers: [
        {
            direction: 'ASC',
            property: 'status'
        }
    ]
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
        {ref: 'communicationOverview', selector: '#communication-overview'},
        {ref: 'header', selector: '#header-section'},
        {ref: 'summary', selector: '#summary'},
        {ref: 'communicationServers', selector: '#communication-servers'},
        {ref: 'overview', selector: '#overview'},
        {ref: 'breakdown', selector: '#breakdown'},
        {ref: 'kpi', selector: '#communication-overview read-outs-over-time'},
        {ref: 'quickLinks', selector: '#communication-overview #quick-links'}
    ],

    init: function () {
        this.control({
            '#communication-overview #refresh-btn': {
                click: this.loadData
            },
            '#communication-overview #device-group': {
                change: this.updateQuickLinks
            }
        });
    },

    showOverview: function () {
        var me = this;
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('communication-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            model = me.getModel('Dsh.model.communication.Overview'),
            router = this.getController('Uni.controller.history.Router');

        model.setFilter(router.filter);
        me.getCommunicationOverview().setLoading();
        me.getCommunicationServers().reload();

        model.load(null, {
                success: function (record) {
                    me.getSummary().setRecord(record.getSummary());
                    me.getOverview().bindStore(record.overviews());
                    me.getBreakdown().bindStore(record.breakdowns());
                    if (record.raw.kpi) {
                        me.getKpi().setRecord(record.getKpi());
                    }
                    me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Uni.DateTime.formatTimeShort(new Date()));


                },
                callback: function () {
                    me.getCommunicationOverview().setLoading(false);
                }
            }
        );
    },

    updateQuickLinks: function () {
        if (Uni.Auth.hasAnyPrivilege(['privilege.view.reports'])) {
            var me = this;
            var deviceGroupField = me.getHeader().down('#device-group');
            var deviceGroupName = deviceGroupField.groupName;
            var filter = false;
            if (deviceGroupName && deviceGroupName.length) {
                filter = encodeURIComponent(Ext.JSON.encode({
                    'GROUPNAME': deviceGroupName
                }))
            }
            var reportsStore = Ext.getStore('ReportInfos');
            if (reportsStore) {
                var proxy = reportsStore.getProxy();
                proxy.setExtraParam('category', 'MDC');
                proxy.setExtraParam('subCategory', 'Device Communication');

                reportsStore.load(function (records) {
                    var quickLinks = Ext.isArray(me.getQuickLinks().data) ? me.getQuickLinks().data : [];
                    Ext.each(records, function (record) {
                        var reportName = record.get('name');
                        var reportUUID = record.get('reportUUID');

                        quickLinks.push({
                            link: reportName,
                            href: '#/administration/generatereport?reportUUID=' + reportUUID + '&subCategory=Device%20Communication' + (filter ? '&filter=' + filter : '')
                        });
                    });

                    var quicklinksTplPanel = me.getQuickLinks().down('#quicklinksTplPanel');
                    quicklinksTplPanel.update(quickLinks);
                });
            }
        }
    }
});

Ext.define('Dsh.view.widget.CommunicationsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communications-list',
    store: 'Dsh.store.CommunicationTasks',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('communication.widget.details.commmunication', 'DSH', 'Communication'),
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
                text: Uni.I18n.translate('communication.widget.details.nextCommunication', 'DSH', 'Next communication'),
                dataIndex: 'nextCommunication',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'successfulFinishTime',
                text: Uni.I18n.translate('communication.widget.details.finishedOn', 'DSH', 'Finished successfully on'),
                dataIndex: 'successfulFinishTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'communicationsGridActionMenu',
                xtype: 'uni-actioncolumn',
                menu: {
                    //xtype: 'communications-action-menu'
                }
            }
        ]
    },
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('communication.widget.details.displayMsg', 'DDSH', '{0} - {1} of {2} communications'),
                displayMoreMsg: Uni.I18n.translate('communication.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} communications'),
                emptyMsg: Uni.I18n.translate('communication.widget.details.emptyMsg', 'DSH', 'There are no communications to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'generate-report',
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.reports']),
                        text: Uni.I18n.translate('generatereport.generateReportButton', 'DSH', 'Generate report')
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('communication.widget.details.itemsPerPage', 'DSH', 'Communications per page')
            }
        ];
        me.callParent(arguments);
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
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'DSH', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            itemId: 'communicationPreviewActionMenu',
            menu: {
//                xtype: 'communications-action-menu'
            }
        }
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
                    fieldLabel: Uni.I18n.translate('communication.widget.details.commTaskName', 'DSH', 'Name'),
                    name: 'name'
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.commTasks', 'DSH', 'Communication task(s)'),
                    name: 'comTasks',
                    renderer: function (value) {
                        if (value !== '') {
                            var result = '';
                            Ext.each(value, function (item) {
                                result = result + '<li>' + item.name + '</li>'
                            });
                            return result;
                        } else {
                            return '';
                        }
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.device', 'DSH', 'Device'),
                    name: 'device',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            Uni.Auth.hasAnyPrivilege(['privilege.view.device', 'privilege.administrate.deviceData'])
                                ? res = '<a href="#/devices/' + val.id + '">' + val.name + '</a>' : res = val.name;
                        }
                        return res;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceType', 'privilege.view.deviceType'])
                                ? res = '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>' : res = val.name;
                        }
                        return res;
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
                        if (res !== '' && !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceType', 'privilege.view.deviceType'])) {
                            res = val.config.name;
                        }
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.frequency', 'DSH', 'Frequency'),
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
                        if (!_.isUndefined(val)) {
                            return val ? 'Yes' : 'No'
                        } else {
                            return ''
                        }
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
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.finishedOn', 'DSH', 'Finished successfully on'),
                    name: 'successfulFinishTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.nextComm', 'DSH', 'Next communication'),
                    name: 'nextCommunication',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
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

Ext.define('Dsh.view.widget.common.SideFilterCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.side-filter-combo',
    editable: false,
    multiSelect: true,
    queryMode: 'local',
    triggerAction: 'all',

    initComponent: function () {
        var me = this;
        me.listConfig = {
            getInnerTpl: function () {
                return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {' + me.displayField + '}</div>';
            }
        };

        me.callParent(arguments);

        me.store.load({
            callback: function () {
                me.select(me.getValue());
                me.fireEvent('updateTopFilterPanelTagButtons', me);
            }
        });
    },

    getValue: function () {
        var me = this;
        me.callParent(arguments);
        if (_.isArray(me.value)) {
            me.value = _.compact(me.value)
        }
        return me.value
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
            editable: false,
            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
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
        {xtype: 'panel', name: 'header', baseCls: 'x-form-item-label', style: 'margin: 15px 0'},
        {label: Uni.I18n.translate('connection.widget.sideFilter.from', 'DSH', 'From'), name: 'from'},
        {label: Uni.I18n.translate('connection.widget.sideFilter.to', 'DSH', 'To'), name: 'to'}
    ],
    initComponent: function () {
        this.callParent(arguments);
        this.down('panel[name=header]').update(this.wTitle);
    }
});

Ext.define('Dsh.util.FilterHydrator', {
    extract: function (record) {
        var data = {},
            startedBetween = record.startedBetween,
            finishedBetween = record.finishedBetween;
        Ext.merge(data, record.getData());
        if (!_.isEmpty(startedBetween)) {
            data.startedBetween = {};

            if (startedBetween.get('from')) {
                data.startedBetween.from = {
                    date: startedBetween.get('from'),
                    hours: startedBetween.get('from').getHours(),
                    minutes: startedBetween.get('from').getMinutes()
                };
            }

            if (startedBetween.get('to')) {
                data.startedBetween.to = {
                    date: startedBetween.get('to'),
                    hours: startedBetween.get('to').getHours(),
                    minutes: startedBetween.get('to').getMinutes()
                };
            }
        }

        if (!_.isEmpty(finishedBetween)) {
            data.finishedBetween = {};
            if (finishedBetween.get('from')) {
                data.finishedBetween.from = {
                    date: finishedBetween.get('from'),
                    hours: finishedBetween.get('from').getHours(),
                    minutes: finishedBetween.get('from').getMinutes()
                }
            }
            if (finishedBetween.get('to')) {
                data.finishedBetween.to = {
                    date: finishedBetween.get('to'),
                    hours: finishedBetween.get('to').getHours(),
                    minutes: finishedBetween.get('to').getMinutes()
                }
            }
        }

        return data;
    },

    hydrate: function (data, record) {
        var startedBetween = data.startedBetween,
            finishedBetween = data.finishedBetween,
            startedBetweenFromDate = this.parseDate(startedBetween.from.date, startedBetween.from.hours, startedBetween.from.minutes),
            startedBetweenToDate = this.parseDate(startedBetween.to.date, startedBetween.to.hours, startedBetween.to.minutes),
            finishedBetweenFromDate = this.parseDate(finishedBetween.from.date, finishedBetween.from.hours, finishedBetween.from.minutes),
            finishedBetweenToDate = this.parseDate(finishedBetween.to.date, finishedBetween.to.hours, finishedBetween.to.minutes);
        delete data.startedBetween;
        delete data.finishedBetween;
        record.set(data);
        record.setStartedBetween(Ext.create('Dsh.model.DateRange', {
            from: startedBetweenFromDate,
            to: startedBetweenToDate
        }));
        record.setFinishedBetween(Ext.create('Dsh.model.DateRange', {
            from: finishedBetweenFromDate,
            to: finishedBetweenToDate
        }));
    },

    parseDate: function (date, hours, minutes) {
        return new Date(new Date(date).getTime() + hours * 3600000 + minutes * 60000) || null;
    }
});

Ext.define('Dsh.view.widget.CommunicationSideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dsh-comm-side-filter',
    requires: [
        'Uni.component.filter.view.Filter',
        'Dsh.view.widget.common.SideFilterCombo',
        'Dsh.view.widget.common.SideFilterDateTime',
        'Dsh.util.FilterHydrator'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    items: [
        {
            xtype: 'nested-form',
            itemId: 'filter-form',
            hydrator: 'Dsh.util.FilterHydrator',
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
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.DeviceGroup'
                },
                {
                    itemId: 'current-state',
                    name: 'currentStates',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
                    displayField: 'localizedValue',
                    valueField: 'taskStatus',
                    store: 'Dsh.store.filter.CurrentState'
                },
                {
                    itemId: 'latest-result',
                    name: 'latestResults',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
                    displayField: 'localizedValue',
                    valueField: 'completionCode',
                    store: 'Dsh.store.filter.CompletionCodes'
                },
                {
                    itemId: 'communication-task',
                    name: 'comTasks',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.commTask', 'DSH', 'Communication task'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.CommunicationTask'
                },
                {
                    itemId: 'communication-schedule',
                    name: 'comSchedules',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.communicationSchedule', 'DSH', 'Communication schedule'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.CommunicationSchedule'
                },
                {
                    itemId: 'device-type',
                    name: 'deviceTypes',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.DeviceType'
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
                    wTitle: Uni.I18n.translate('connection.widget.sideFilter.finishedBetween', 'DSH', 'Finished successfully between')
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
                            action: 'applyfilter',
                            itemId: 'btn-apply-filter'
                        },
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.clearAll', 'DSH', 'Clear all'),
                            action: 'clearfilter',
                            itemId: 'btn-clear-filter'
                        }
                    ]
                }
            ]
        }
    ]
});


Ext.define('Dsh.view.widget.ConnectionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.connection-action-menu',
    items: [
        {
            text: 'Run now',
            hidden: Uni.Auth.hasNoPrivilege('privilege.operate.deviceCommunication'),
            action: 'run'
        },
        {
            text: 'View history',
            action: 'viewHistory'
        },
        {
            text: 'View log',
            action: 'viewLog'
        }
    ]
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
        {
            xtype: 'button',
            itemId: 'connectionsPreviewActionBtn',
            text: Uni.I18n.translate('general.actions', 'DSH', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'connection-action-menu',
                itemId: 'connectionsActionMenu'
            }
        }
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
                        var res = '';
                        if (val) {
                            Uni.Auth.hasAnyPrivilege(['privilege.view.device', 'privilege.administrate.deviceData'])
                                ? res = '<a href="#/devices/' + val.id + '">' + val.name + '</a>' : res = val.name;
                        }
                        return res;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceType', 'privilege.view.deviceType'])
                                ? res = '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>' : res = val.name;
                        }
                        return res;
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
                        if (res !== '' && !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceType', 'privilege.view.deviceType'])) {
                            res = val.config.name;
                        }
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connMethod', 'DSH', 'Connection method'),
                    name: 'connectionMethod',
                    renderer: function (val) {
                        return val ? val.name : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connType', 'DSH', 'Connection type'),
                    name: 'connectionType'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.direction', 'DSH', 'Direction'),
                    name: 'direction'
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
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commPortPool', 'DSH', 'Communication port pool'),
                    name: 'comPortPool',
                    renderer: function (val) {
                        return val ? val.name : ''
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
                    height: 60,
                    cls: 'communication-tasks-status',
                    renderer: function (val) {
                        var failed = val.numberOfFailedTasks ? val.numberOfFailedTasks : 0,
                            success = val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : 0,
                            notCompleted = val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : 0;
                        if (failed === 0 && success === 0 && notCompleted === 0) {
                            return '';
                        } else {
                            return '<tpl><span class="icon-checkmark"></span>' + success + '<br></tpl>' +
                                '<tpl><span class="icon-close"></span>' + failed + '<br></tpl>' +
                                '<tpl><span class="icon-stop2"></span>' + notCompleted + '</tpl>'
                                ;
                        }
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.startedOn', 'DSH', 'Started on'),
                    name: 'startDateTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.finishedOn', 'DSH', 'Finished on'),
                    name: 'endDateTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
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
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commPort', 'DSH', 'Communication port'),
                    name: 'comPort',
                    renderer: function (val) {
                        return val ? val.name : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.nextConnection', 'DSH', 'Next connection'),
                    name: 'nextExecution',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
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
        "connectionTask",
        "sessionId",
        "comTasks",
        {name: 'startTime', type: 'date', dateFormat: 'time'},
        {name: 'successfulFinishTime', type: 'date', dateFormat: 'time'},
        {name: 'nextCommunication', type: 'date', dateFormat: 'time'},
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
    ],
    hasOne: {
        model: 'Dsh.model.ConnectionTask',
        associationKey: 'connectionTask',
        name: 'connectionTask',
        getterName: 'getConnectionTask'
    },

    run: function (callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/run'.replace('{id}', this.getId()),
            success: callback
        });
    },

    runNow: function (callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/runnow'.replace('{id}', this.getId()),
            success: callback
        });
    },

    proxy: {
        type: 'ajax',
        url: '/api/dsr/communications',
        reader: {
            type: 'json',
            root: 'communicationTasks',
            totalProperty: 'total'
        }
    }


});


Ext.define('Dsh.util.FilterStoreHydrator', {
    extract: function (filter) {
        var data = filter.getData();
        data.deviceGroups = data.deviceGroup;
        delete data.deviceGroup;

        // transform all single items int array
        _.map(data, function (item, key) {
            if (item) {
                if (!_.isArray(item)) {
                    data[key] = [item]
                }
            }
            return item;
        });
        if (filter.startedBetween) {
            var start = filter.getStartedBetween();
            if (start.get('from')) {
                data.startIntervalFrom = start.get('from').getTime();
            }

            if (start.get('to')) {
                data.startIntervalTo = start.get('to').getTime();
            }
        }

        if (filter.finishedBetween) {
            var end = filter.getFinishedBetween();
            if (end.get('from')) {
                data.finishIntervalFrom = end.get('from').getTime();
            }

            if (end.get('to')) {
                data.finishIntervalTo = end.get('to').getTime();
            }
        }

        return data;
    }
});

Ext.define('Dsh.store.CommunicationTasks', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Dsh.model.CommunicationTask',
        'Dsh.util.FilterStoreHydrator'
    ],
    model: 'Dsh.model.CommunicationTask',
    hydrator: 'Dsh.util.FilterStoreHydrator',
    autoLoad: false,
    remoteFilter: true,

    proxy: {
        type: 'ajax',
        url: '/api/dsr/communications',
        reader: {
            type: 'json',
            root: 'communicationTasks',
            totalProperty: 'total'
        }
    }
});


Ext.define('Dsh.view.Communications', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communications-details',
    itemId: 'communicationsdetails',
    requires: [
        'Dsh.view.widget.CommunicationsList',
        'Dsh.view.widget.PreviewCommunication',
        'Dsh.view.widget.CommunicationSideFilter',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.store.CommunicationTasks'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.communication.title', 'DSH', 'Communications')
        },
        {
            xtype: 'filter-top-panel',
            itemId: 'dshcommunicationsfilterpanel'
        },
        {
            xtype: 'preview-container',
            grid: {
                xtype: 'communications-list',
                itemId: 'communicationslist'
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('workspace.dataCommunication.communication.empty.title', 'DSH', 'No communications found'),
                reasons: [
                    Uni.I18n.translate('workspace.dataCommunication.communication.empty.list.item1', 'DSH', 'No communications in the system.'),
                    Uni.I18n.translate('workspace.dataCommunication.communication.empty.list.item2', 'DSH', 'No communications found due to applied filters.')
                ]
            },
            previewComponent: {
                hidden: true,
                items: [
                    {
                        xtype: 'preview_communication',
                        itemId: 'communicationdetails'
                    },
                    {
                        style: {
                            'margin-top': '32px'
                        },
                        xtype: 'preview_connection',
                        itemId: 'connectiondetails'
                    }
                ]
            }
        }
    ],

    side: [
        {
            xtype: 'dsh-comm-side-filter',
            itemId: 'dshcommunicationssidefilter'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});


Ext.define('Dsh.model.ConnectionTask', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'auto'},
        {name: 'device', type: 'auto'},
        {name: 'deviceConfiguration', type: 'auto'},
        {name: 'deviceType', type: 'auto'},
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
        {name: 'currentState', type: 'auto'},
        {name: 'latestStatus', type: 'auto'},
        {name: 'latestResult', type: 'auto'},
        {name: 'taskCount', type: 'auto'},
        {name: 'startDateTime', type: 'date', dateFormat: 'time'},
        {name: 'endDateTime', type: 'date', dateFormat: 'time'},
        {name: 'duration', type: 'auto'},
        {name: 'comPortPool', type: 'auto'},
        {name: 'direction', type: 'auto'},
        {name: 'connectionType', type: 'auto'},
        {name: 'comServer', type: 'auto'},
        {name: 'connectionMethod', type: 'auto'},
        {name: 'window', type: 'auto'},
        {name: 'connectionStrategy', type: 'auto'},
        {name: 'nextExecution', type: 'date', dateFormat: 'time'},
        {name: 'comPort', type: 'auto'},
        {name: 'comSessionId', type: 'auto'}
    ],

    run: function (callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/run'.replace('{id}', this.getId()),
            success: callback
        });
    },

    proxy: {
        type: 'rest',
        url: '/api/dsr/connections',
        reader: {
            type: 'json',
            root: 'connectionTasks',
            totalProperty: 'total'
        }
    }

});

Ext.define('Dsh.model.CommTasks', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'count', type: 'auto'},
        'communicationTasks'
    ],
    hasMany: [
        {
            model: 'Dsh.model.CommunicationTask',
            name: 'communicationsTasks'
        }
    ]
});

Ext.define('Dsh.model.DateRange', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        {name: 'from', type: 'date', dateFormat: 'Y-m-dTH:i:s'},
        {name: 'to', type: 'date', dateFormat: 'Y-m-dTH:i:s'}
    ]
});

Ext.define('Dsh.model.Filter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy',
        'Dsh.model.DateRange'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        {name: 'currentStates', type: 'auto'},
        {name: 'latestStates', type: 'auto'},
        {name: 'latestResults', type: 'auto'},
        {name: 'comPortPools', type: 'auto'},
        {name: 'comSchedules', type: 'auto'},
        {name: 'comTasks', type: 'auto'},
        {name: 'connectionTypes', type: 'auto'},
        {name: 'deviceGroup', type: 'auto'},
        {name: 'deviceTypes', type: 'auto'}
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'startedBetween',
            instanceName: 'startedBetween',
            associationKey: 'startedBetween',
            getterName: 'getStartedBetween',
            setterName: 'setStartedBetween'
        },
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'finishedBetween',
            instanceName: 'finishedBetween',
            associationKey: 'finishedBetween',
            getterName: 'getFinishedBetween',
            setterName: 'setFinishedBetween'
        }
    ]
});

Ext.define('Dsh.store.filter.CommunicationSchedule', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comschedules',
        reader: {
            type: 'json',
            root: 'comSchedules'
        }
    }
});


Ext.define('Dsh.store.filter.CommunicationTask', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comtasks',
        reader: {
            type: 'json',
            root: 'comTasks'
        }
    }
});

Ext.define('Dsh.store.filter.CurrentState', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'taskStatus'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/taskstatus',
        reader: {
            type: 'json',
            root: 'taskStatuses'
        }
    }
});


Ext.define('Dsh.store.filter.LatestResult', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'successIndicator'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comsessionsuccessindicators',
        reader: {
            type: 'json',
            root: 'successIndicators'
        }
    }
});


Ext.define('Dsh.store.filter.ConnectionType', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/connectiontypepluggableclasses',
        reader: {
            type: 'json',
            root: 'connectiontypepluggableclasses'
        }
    }
});

Ext.define('Dsh.store.filter.DeviceType', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/devicetypes',
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});


Ext.define('Dsh.controller.Communications', {
    extend: 'Dsh.controller.BaseController',

    models: [
        'Dsh.model.ConnectionTask',
        'Dsh.model.CommTasks',
        'Dsh.model.CommunicationTask',
        'Dsh.model.Filter'
    ],

    views: [
        'Dsh.view.Communications',
        'Dsh.view.widget.PreviewCommunication',
        'Dsh.view.widget.PreviewConnection'
    ],

    stores: [
        'Dsh.store.CommunicationTasks',
        'Dsh.store.filter.CommunicationSchedule',
        'Dsh.store.filter.CommunicationTask',
        'Dsh.store.filter.CurrentState',
        'Dsh.store.filter.LatestResult',
        'Dsh.store.filter.ConnectionType',
        'Dsh.store.filter.DeviceType'
    ],

    refs: [
        {
            ref: 'communicationPreview',
            selector: '#communicationsdetails #communicationdetails'
        },
        {
            ref: 'connectionPreview',
            selector: '#communicationsdetails #connectiondetails'
        },
        {
            ref: 'filterPanel',
            selector: '#communicationsdetails filter-top-panel'
        },
        {
            ref: 'sideFilterForm',
            selector: '#communicationsdetails #filter-form'
        },
        {
            ref: 'communicationsGrid',
            selector: 'communications-list'
        },
        {
            ref: 'communicationsGridActionMenu',
            selector: '#communicationsGridActionMenu'
        },
        {
            ref: 'communicationPreviewActionMenu',
            selector: '#communicationPreviewActionMenu'
        },
        {
            ref: 'connectionsPreviewActionBtn',
            selector: '#connectionsPreviewActionBtn'
        }
    ],

    prefix: '#communicationsdetails',

    init: function () {
        this.control({
            '#communicationsdetails #communicationslist': {
                selectionchange: this.onSelectionChange
            },
            'communications-list #generate-report': {
                click: this.onGenerateReport
            }
        });

        this.callParent(arguments);
    },

    showOverview: function () {
        var widget = Ext.widget('communications-details'),
            store = this.getStore('Dsh.store.CommunicationTasks');

        this.getApplication().fireEvent('changecontentevent', widget);
        this.initFilter();
        store.load();
    },

    initMenu: function (record, menuItems, me) {

        this.getCommunicationsGridActionMenu().menu.removeAll();
        this.getCommunicationPreviewActionMenu().menu.removeAll();
        this.getConnectionsPreviewActionBtn().menu.removeAll();

        Ext.suspendLayouts();

        Ext.each(record.get('comTasks'), function (item) {
            if (record.get('sessionId') !== 0) {
                menuItems.push({
                    text: Ext.String.format(Uni.I18n.translate('connection.widget.details.menuItem', 'DSH', 'View \'{0}\' log'), item.name),
                    action: {
                        action: 'viewlog',
                        comTask: {
                            mRID: record.get('device').id,
                            sessionId: record.get('sessionId'),
                            comTaskId: item.id
                        }
                    },
                    listeners: {
                        click: me.viewCommunicationLog
                    }
                });
            }
        });

        if (record.get('connectionTask').connectionStrategy && record.get('connectionTask').connectionStrategy.id) {
            if (record.get('connectionTask').connectionStrategy.id === 'minimizeConnections') {
                menuItems.push({
                    text: Uni.I18n.translate('connection.widget.details.menuItem.run', 'DSH', 'Run'),
                    action: {
                        action: 'run',
                        record: record,
                        me: me
                    },
                    listeners: {
                        click: me.communicationRun
                    }
                });
            }

            menuItems.push({
                text: Uni.I18n.translate('connection.widget.details.menuItem.runNow', 'DSH', 'Run now'),
                action: {
                    action: 'runNow',
                    record: record,
                    me: me
                },
                listeners: {
                    click: me.communicationRunNow
                }
            });

        }

        var connectionMenuItem = {
            text: Uni.I18n.translate('connection.widget.details.connectionMenuItem', 'DSH', 'View connection log'),
            action: {
                action: 'viewlog',
                connection: {
                    mRID: record.get('device').id,
                    connectionMethodId: record.get('connectionTask').id,
                    sessionId: record.get('connectionTask').comSessionId

                }
            },
            listeners: {
                click: me.viewConnectionLog
            }
        };

        this.getCommunicationsGridActionMenu().menu.add(menuItems);
        this.getCommunicationPreviewActionMenu().menu.add(menuItems);

        if (record.get('connectionTask').comSessionId !== 0) {
            this.getConnectionsPreviewActionBtn().menu.add(connectionMenuItem);
        }

        Ext.resumeLayouts(true);
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            preview = me.getCommunicationPreview(),
            connPreview = me.getConnectionPreview(),
            record = selected[0],
            menuItems = [];
        if (record) {
            this.initMenu(record, menuItems, me);
            preview.loadRecord(record);
            preview.setTitle(record.get('name') + ' on ' + record.get('device').name);
            if (record.getData().connectionTask) {
                var conTask = record.getConnectionTask();
                connPreview.setTitle(conTask.get('connectionMethod').name + ' on ' + conTask.get('device').name);
                connPreview.show();
                connPreview.loadRecord(conTask);
            } else {
                connPreview.hide()
            }
        }
    },

    viewCommunicationLog: function (item) {
        location.href = '#/devices/' + item.action.comTask.mRID
        + '/communicationtasks/' + item.action.comTask.comTaskId
        + '/history/' + item.action.comTask.sessionId
        + '/viewlog' +
        '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D';
    },

    viewConnectionLog: function (item) {
        location.href = '#/devices/' + item.action.connection.mRID + '/connectionmethods/' + item.action.connection.connectionMethodId + '/history/' + item.action.connection.sessionId + '/viewlog' +
        '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22Connections%22%2C%22Communications%22%5D%7D'
    },

    onGenerateReport: function () {
        var me = this;
        var router = this.getController('Uni.controller.history.Router');
        var fieldsToFilterNameMap = {};
        fieldsToFilterNameMap['deviceGroup'] = 'GROUPNAME';
        fieldsToFilterNameMap['currentStates'] = 'STATUS';
        fieldsToFilterNameMap['latestResults'] = null;
        fieldsToFilterNameMap['comSchedules'] = 'SCHEDULENAME';
        fieldsToFilterNameMap['deviceTypes'] = null;
        fieldsToFilterNameMap['comTasks'] = 'COMTASKNAME';

        var reportFilter = false;

        var fields = me.getSideFilterForm().getForm().getFields();
        fields.each(function (field) {
            reportFilter = reportFilter || {};
            var filterName = fieldsToFilterNameMap[field.getName()];
            if (filterName) {
                var fieldValue = field.getRawValue();
                if (field.getXType() == 'side-filter-combo') {
                    fieldValue = Ext.isString(fieldValue) && fieldValue.split(', ') || fieldValue;
                    fieldValue = _.isArray(fieldValue) && _.compact(fieldValue) || fieldValue;
                }
            }
            reportFilter[filterName] = fieldValue;
        });

        //handle special startBetween and finishBetween;
        //router.filter.startedBetween
        //router.filter.finishBetween

        if (router.filter && router.filter.startedBetween) {
            var from = router.filter.startedBetween.get('from');
            var to = router.filter.startedBetween.get('to');
            reportFilter['CONNECTIONDATE'] = {
                'from': from && Ext.Date.format(from, "Y-m-d H:i:s"),
                'to': to && Ext.Date.format(to, "Y-m-d H:i:s")
            };
        }

        router.getRoute('generatereport').forward(null, {
            category: 'MDC',
            subCategory: 'Device Communication',
            filter: reportFilter
        });
    },

    communicationRun: function (item) {
        var me = item.action.me;
        var record = item.action.record;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.wait', 'DSH', 'Run succeeded')
            );
            record.set('plannedDate', new Date());
            me.showOverview();
        });
    },

    communicationRunNow: function (item) {
        var me = item.action.me;
        var record = item.action.record;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.now', 'DSH', 'Run now succeeded')
            );
            record.set('plannedDate', new Date());
            me.showOverview();
        });
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
        padding: '0 20px'
    },
    defaults: {
        style: {
            marginBottom: '20px',
            padding: 0
        }
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'header-section',
                router: me.router,
                style: 'none'
            },
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    },
                    flex: 1
                },
                items: [
                    {
                        xtype: 'summary',
                        flex: 2,
                        wTitle: Uni.I18n.translate('connection.widget.summary.title', 'DSH', 'Connections summary'),
                        router: me.router,
                        parent: 'connections'
                    },
                    {
                        xtype: 'communication-servers',
                        itemId: 'communication-servers',
                        router: me.router
                    },
                    {
                        xtype: 'quick-links',
                        itemId: 'quick-links',
                        maxHeight: 256,
                        overflowY: 'auto',
                        style: {
                            marginRight: '0',
                            padding: '20px'
                        },
                        data: [
                            {
                                link: Uni.I18n.translate('connection.widget.quicklinks.viewAll', 'DSH', 'View all connections'),
                                href: me.router.getRoute('workspace/connections/details').buildUrl(null, me.router.queryParams)
                            },
                            {
                                link: me.router.getRoute('workspace/communications').title,
                                href: me.router.getRoute('workspace/communications').buildUrl(null, me.router.queryParams)
                            },
                            {
                                link: Uni.I18n.translate('communication.widget.quicklinks.myIssues', 'DSH', 'My open issues'),
                                href: typeof me.router.getRoute('workspace/datacollectionissues') !== 'undefined'
                                    ? me.router.getRoute('workspace/datacollectionissues').buildUrl(null, me.router.queryParams) + '?myopenissues=true' : null
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'read-outs-over-time',
                wTitle: Uni.I18n.translate('connection.widget.readOutsOverTime.title', 'DSH', 'Connections over time'),
                yLabel: Uni.I18n.translate('connection.widget.readOutsOverTime.yLabel', 'DSH', 'Number of connections'),
                router: me.router,
                parent: 'connections'
            },
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
            },
            {
                xtype: 'heat-map',
                itemId: 'heatmap',
                store: 'Dsh.store.ConnectionResultsStore',
                router: me.router,
                parent: 'connections'
            }
        ];
        this.callParent(arguments);
    }
});

Ext.define('Dsh.model.connection.Overview', {
    extend: 'Dsh.model.Filterable',
    requires: [
        'Dsh.model.Summary',
        'Dsh.model.Counter',
        'Dsh.model.Breakdown',
        'Dsh.model.Kpi'
    ],
    hasOne: [
        {
            model: 'Dsh.model.Summary',
            associationKey: 'connectionSummary',
            name: 'summary',
            getterName: 'getSummary',
            setterName: 'setSummary'
        },
        {
            model: 'Dsh.model.Kpi',
            associationKey: 'kpi',
            name: 'kpi',
            getterName: 'getKpi',
            setterName: 'setKpi'
        }
    ],
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
        {name: 'localizedValue', type: 'string'},
        {name: 'breakdown', type: 'string'}
    ]
});

Ext.define('Dsh.store.CombineStore', {
    extend: 'Ext.data.Store',
    storeId: 'CombineStore',
    model: 'Dsh.model.Combine',
    autoLoad: false,
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
        {name: 'displayValue', type: 'string'},
        {name: 'alias', type: 'string'},
        {name: 'id', type: 'int'},
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
    extend: 'Uni.data.store.Filterable',
    model: 'Dsh.model.ConnectionResults',
    autoLoad: false,
    remoteFilter: true,
    proxy: {
        type: 'ajax',
        url: '/api/dsr/connectionheatmap',
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
    views: ['Dsh.view.ConnectionOverview'],
    refs: [
        {ref: 'connectionOverview', selector: '#connection-overview'},
        {ref: 'header', selector: '#header-section'},
        {ref: 'summary', selector: '#summary'},
        {ref: 'communicationServers', selector: '#communication-servers'},
        {ref: 'overview', selector: '#overview'},
        {ref: 'breakdown', selector: '#breakdown'},
        {ref: 'kpi', selector: '#connection-overview read-outs-over-time'},
        {ref: 'quickLinks', selector: '#connection-overview #quick-links'}
    ],

    init: function () {
        this.control({
            '#connection-overview #refresh-btn': {
                click: this.loadData
            },
            '#connection-overview #device-group': {
                change: this.updateQuickLinks
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
            model = me.getModel('Dsh.model.connection.Overview'),
            router = this.getController('Uni.controller.history.Router');

        model.setFilter(router.filter);
        me.getConnectionOverview().setLoading();
        me.getCommunicationServers().reload();
        model.load(null, {
                success: function (record) {
                    me.getSummary().setRecord(record.getSummary());
                    me.getOverview().bindStore(record.overviews());
                    me.getBreakdown().bindStore(record.breakdowns());
                    if (record.raw.kpi) {
                        me.getKpi().setRecord(record.getKpi());
                    }
                    me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Uni.DateTime.formatTimeShort(new Date()));
                },
                callback: function () {
                    me.getConnectionOverview().setLoading(false);
                }
            }
        );
    },

    updateQuickLinks: function () {
        if (Uni.Auth.hasAnyPrivilege(['privilege.view.reports'])) {
            var me = this;
            var deviceGroupField = me.getHeader().down('#device-group');
            var deviceGroupName = deviceGroupField.groupName;

            var filter = false;
            if (deviceGroupName && deviceGroupName.length) {
                filter = encodeURIComponent(Ext.JSON.encode({
                    'GROUPNAME': deviceGroupName
                }))
            }
            var reportsStore = Ext.getStore('ReportInfos');
            if (reportsStore) {
                var proxy = reportsStore.getProxy();
                proxy.setExtraParam('category', 'MDC');
                proxy.setExtraParam('subCategory', 'Device Connections');
                reportsStore.load(function (records) {
                    var quickLinks = Ext.isArray(me.getQuickLinks().data) ? me.getQuickLinks().data : [];
                    Ext.each(records, function (record) {
                        var reportName = record.get('name');
                        var reportUUID = record.get('reportUUID');
                        quickLinks.push({
                            link: reportName,
                            href: '#/administration/generatereport?reportUUID=' + '&subCategory=Device%Connections' + reportUUID + (filter ? '&filter=' + filter : ''),
                            target: '_blank'
                        });
                    });

                    var quicklinksTplPanel = me.getQuickLinks().down('#quicklinksTplPanel');
                    quicklinksTplPanel.update(quickLinks);
                });
            }
        }
    }
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
        'Dsh.view.widget.ConnectionActionMenu'
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
                    return Uni.Auth.hasAnyPrivilege(['privilege.view.device', 'privilege.administrate.deviceData'])
                        ? '<a href="#/devices/' + val.id + '">' + val.name + '</a>' : val.name
                }
            },
            {
                itemId: 'connectionMethod',
                text: Uni.I18n.translate('connection.widget.details.connectionMethod', 'DSH', 'Connection method'),
                dataIndex: 'connectionMethod',
                flex: 1,
                renderer: function (val) {
                    return val ? val.name : ''
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
                flex: 1,
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
                renderer: function (val, metaData) {
                    metaData.tdCls = 'communication-tasks-status';
                    var template = '';
                    if (val.numberOfSuccessfulTasks || val.numberOfFailedTasks || val.numberOfIncompleteTasks) {
                        template += '<tpl><span class="icon-checkmark"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</tpl>';
                        template += '<tpl><span class="icon-close"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</tpl>';
                        template += '<tpl><span  class="icon-stop2"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</tpl>';
                    }
                    return template;
                },
                header: Uni.I18n.translate('connection.widget.details.commTasks', 'DSH', 'Communication tasks'),
                flex: 2
            },
            {
                itemId: 'startDateTime',
                text: Uni.I18n.translate('connection.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startDateTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 1
            },
            {
                itemId: 'connectionsActionMenu',
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'connection-action-menu',
                    itemId: 'connectionsActionMenu'
                }
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
            emptyMsg: Uni.I18n.translate('connection.widget.details.emptyMsg', 'DSH', 'There are no connections to display'),
            items: [
                {
                    xtype: 'button',
                    itemId: 'generate-report',
                    hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.reports']),
                    text: Uni.I18n.translate('generatereport.generateReportButton', 'DSH', 'Generate report')
                }
            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            store: 'Dsh.store.ConnectionTasks',
            dock: 'bottom',
            deferLoading: true,
            itemsPerPageMsg: Uni.I18n.translate('connection.widget.details.itemsPerPage', 'DSH', 'Connections per page')
        }
    ],

    addTooltip: function () {
        var me = this,
            view = me.getView(),
            tip = Ext.create('Ext.tip.ToolTip', {
                target: view.el,
                delegate: 'img.ct-result',
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
                        (tip.triggerElement.className.search('ct-success') !== -1) && (res = success);
                        (tip.triggerElement.className.search('ct-failure') !== -1) && (res = failed);
                        (tip.triggerElement.className.search('ct-incomplete') !== -1) && (res = notCompleted);
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
                    show: function () {
                        var rowEl = Ext.get(ResultTip.triggerElement).up('tr'),
                            latestResult = view.getRecord(rowEl).get('latestResult');
                        if (latestResult.retries) {
                            ResultTip.update(latestResult.retries + ' ' + Uni.I18n.translate('connection.widget.details.retries', 'DSH', 'retries'));
                        } else {
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

Ext.define('Dsh.store.filter.LatestStatus', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'successIndicator'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/connectiontasksuccessindicators',
        reader: {
            type: 'json',
            root: 'successIndicators'
        }
    }
});


Ext.define('Dsh.store.filter.CommPortPool', {
    extend: 'Ext.data.Store',
    fields: ['name', 'id'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comportpools',
        reader: {
            type: 'json',
            root: 'comPortPools'
        }
    }
});

Ext.define('Dsh.view.widget.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dsh-side-filter',
    requires: [
        'Uni.component.filter.view.Filter',
        'Dsh.view.widget.common.SideFilterCombo',
        'Dsh.view.widget.common.SideFilterDateTime',
        'Dsh.store.filter.CurrentState',
        'Dsh.store.filter.LatestStatus',
        'Dsh.store.filter.LatestResult',
        'Dsh.store.filter.CommPortPool',
        'Dsh.store.filter.ConnectionType',
        'Dsh.store.filter.DeviceType'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    items: [
        {
            xtype: 'nested-form',
            itemId: 'filter-form',
            hydrator: 'Dsh.util.FilterHydrator',
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
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.DeviceGroup'
                },
                {
                    itemId: 'current-state',
                    name: 'currentStates',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
                    displayField: 'localizedValue',
                    valueField: 'taskStatus',
                    store: 'Dsh.store.filter.CurrentState'
                },
                {
                    itemId: 'latest-status',
                    name: 'latestStates',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestStatus', 'DSH', 'Latest status'),
                    displayField: 'localizedValue',
                    valueField: 'successIndicator',
                    store: 'Dsh.store.filter.LatestStatus'
                },
                {
                    itemId: 'latest-result',
                    name: 'latestResults',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result'),
                    displayField: 'localizedValue',
                    valueField: 'successIndicator',
                    store: 'Dsh.store.filter.LatestResult'
                },
                {
                    itemId: 'comport-pool',
                    name: 'comPortPools',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.comPortPool', 'DSH', 'Communication port pool'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.CommPortPool'
                },
                {
                    itemId: 'connection-type',
                    name: 'connectionTypes',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.connectionType', 'DSH', 'Connection type'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.ConnectionType'
                },
                {
                    itemId: 'device-type',
                    name: 'deviceTypes',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type'),
                    displayField: 'name',
                    valueField: 'id',
                    store: 'Dsh.store.filter.DeviceType'
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
                    wTitle: Uni.I18n.translate('connection.widget.sideFilter.finishedBetween', 'DSH', 'Finished successfully between')
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
                            action: 'applyfilter',
                            itemId: 'btn-apply-filter'
                        },
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.clearAll', 'DSH', 'Clear all'),
                            action: 'clearfilter',
                            itemId: 'btn-clear-filter'
                        }
                    ]
                }
            ]
        }
    ]
});

Ext.define('Dsh.view.widget.connection.CommunicationsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.connection-communications-list',
    store: 'Dsh.store.CommunicationTasks',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('connection.communication.widget.details.commmunication', 'DSH', 'Communication task'),
                dataIndex: 'comTask',
                renderer: function (val) {
                    return val.name;
                },
                flex: 2
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('connection.communication.widget.details.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return val.name ? val.name : '';
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('connection.communication.widget.details.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? val.displayValue : '';
                }
            },
            {
                itemId: 'Result',
                text: Uni.I18n.translate('connection.communication.widget.details.result', 'DSH', 'Result'),
                dataIndex: 'result',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? val.displayValue : '';
                }
            },
            {
                itemId: 'nextCommunication',
                text: Uni.I18n.translate('connection.communication.widget.details.nextCommunication', 'DSH', 'Next communication'),
                dataIndex: 'nextCommunication',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('connection.communication.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'stopTime',
                text: Uni.I18n.translate('connection.communication.widget.details.finishedOn', 'DSH', 'Finished on'),
                dataIndex: 'stopTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'communicationsGridActionMenu',
                xtype: 'uni-actioncolumn',
                menu: {
                    //xtype: 'communications-action-menu'
                }
            }
        ]
    },
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('connection.communication.widget.details.displayMsg', 'DDSH', '{0} - {1} of {2} communication tasks'),
                displayMoreMsg: Uni.I18n.translate('connection.communication.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} communication tasks'),
                emptyMsg: Uni.I18n.translate('connection.communication.widget.details.emptyMsg', 'DSH', 'There are no communication task to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'generate-report',
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.reports']),
                        text: Uni.I18n.translate('generatereport.generateReportButton', 'DSH', 'Generate report')
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('connection.communication.widget.details.itemsPerPage', 'DSH', 'Communication tasks per page')
            }
        ];
        me.callParent(arguments);
    }
});


Ext.define('Dsh.view.widget.connection.PreviewCommunication', {
    extend: 'Ext.form.Panel',
    alias: 'widget.preview-connection-communication',
    title: '',
    frame: true,
    layout: {
        type: 'column'
    },
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'DSH', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            itemId: 'communicationPreviewActionMenu',
            menu: {
//                xtype: 'communications-action-menu'
            }
        }
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
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.commTaskName', 'DSH', 'Name'),
                    name: 'comTask',
                    renderer: function (val) {
                        return val.name;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.device', 'DSH', 'Device'),
                    name: 'device',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            Uni.Auth.hasAnyPrivilege(['privilege.view.device', 'privilege.administrate.deviceData'])
                                ? res = '<a href="#/devices/' + val.id + '">' + val.name + '</a>' : res = val.name;
                        }
                        return res;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceType', 'privilege.view.deviceType'])
                                ? res = '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>' : res = val.name;
                        }
                        return res;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.deviceConfig', 'DSH', 'Device configuration'),
                    name: 'devConfig',
                    renderer: function (val) {
                        var res = '';
                        val && (res = '<a href="#/administration/devicetypes/' +
                        val.devType.id + '/deviceconfigurations/' +
                        val.config.id +
                        '">' +
                        val.config.name +
                        '</a>');
                        if (res !== '' && !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceType', 'privilege.view.deviceType'])) {
                            res = val.config.name;
                        }
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.frequency', 'DSH', 'Frequency'),
                    name: 'comScheduleFrequency',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            res = Uni.I18n.translate('connection.communication.widget.details.every', 'DSH', 'Every')
                            + ' '
                            + val.every.count
                            + ' '
                            + val.every.timeUnit;
                        }
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.urgency', 'DSH', 'Urgency'),
                    name: 'urgency'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.executeOnInbound', 'DSH', 'Always execute on inbound'),
                    name: 'alwaysExecuteOnInbound',
                    renderer: function (val) {
                        if (!_.isUndefined(val)) {
                            return val ? 'Yes' : 'No'
                        } else {
                            return ''
                        }
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
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.currentState', 'DSH', 'Current state'),
                    name: 'currentState',
                    renderer: function (val) {
                        return val.displayValue ? val.displayValue : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.latestResult', 'DSH', 'Result'),
                    name: 'result',
                    renderer: function (val) {
                        return val.displayValue ? val.displayValue : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.startedOn', 'DSH', 'Started on'),
                    name: 'startTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.finishedOn', 'DSH', 'Finished on'),
                    name: 'stopTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.communication.widget.details.nextComm', 'DSH', 'Next communication'),
                    name: 'nextCommunication',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
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

Ext.define('Dsh.store.ConnectionTasks', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Dsh.model.ConnectionTask',
        'Dsh.util.FilterStoreHydrator'
    ],
    model: 'Dsh.model.ConnectionTask',
    hydrator: 'Dsh.util.FilterStoreHydrator',
    autoLoad: false,
    remoteFilter: true,
    proxy: {
        type: 'rest',
        url: '/api/dsr/connections',
        reader: {
            type: 'json',
            root: 'connectionTasks',
            totalProperty: 'total'
        }
    }
});


Ext.define('Dsh.view.Connections', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connections-details',
    itemId: 'connectionsdetails',

    requires: [
        'Dsh.view.widget.ConnectionsList',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.view.widget.SideFilter',
        'Dsh.view.widget.connection.CommunicationsList',
        'Dsh.view.widget.connection.PreviewCommunication',
        'Dsh.store.ConnectionTasks'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.dataCommunication.connections.title', 'DSH', 'Connections')
        },
        {
            xtype: 'filter-top-panel',
            itemId: 'dshconnectionsfilterpanel'
        },
        {
            xtype: 'panel',
            items: {
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
                    itemId: 'connectionpreview',
                    hidden: true
                }
            }
        },
        {
            ui: 'medium',
            itemId: 'communicationspanel',
            padding: 0,
            margin: '16 0 0 0',
            hidden: true,
            title: '',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'connection-communications-list',
                        itemId: 'communicationsdetails',
                        store: 'Dsh.store.Communications'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('communication.widget.details.empty.title', 'DSH', 'No communications found'),
                        reasons: [
                            Uni.I18n.translate('communication.widget.details.empty.list.item1', 'DSH', 'No communications in the system.'),
                            Uni.I18n.translate('communication.widget.details.empty.list.item2', 'DSH', 'No communications found due to applied filters.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'preview-connection-communication',
                        itemId: 'communicationpreview'
                    }
                }
            ]
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

Ext.define('Dsh.model.connection.CommunicationTask', {
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
        "result",
        "connectionTask",
        "sessionId",
        "comTask",
        {name: 'startTime', type: 'date', dateFormat: 'time'},
        {name: 'stopTime', type: 'date', dateFormat: 'time'},
        {name: 'nextCommunication', type: 'date', dateFormat: 'time'},
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                return data.comTask.name + ' on ' + data.device.name;
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
    ],
    hasOne: {
        model: 'Dsh.model.ConnectionTask',
        associationKey: 'connectionTask',
        name: 'connectionTask',
        getterName: 'getConnectionTask'
    },

    run: function (callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/run'.replace('{id}', this.getId()),
            success: callback
        });
    },

    runNow: function (callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/runnow'.replace('{id}', this.getId()),
            success: callback
        });
    },

    proxy: {
        type: 'ajax',
        url: '/api/dsr/communications',
        reader: {
            type: 'json',
            root: 'communicationTasks',
            totalProperty: 'total'
        }
    }


});


Ext.define('Dsh.store.Communications', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.connection.CommunicationTask'
    ],
    model: 'Dsh.model.connection.CommunicationTask',
    autoLoad: false,
    remoteFilter: true,
    url: '/api/dsr/connections/',
    communicationsPostfix: '/latestcommunications',
    proxy: {
        type: 'ajax',
        reader: {
            type: 'json',
            root: 'communications',
            totalProperty: 'total'
        }
    },
    setConnectionId: function (id) {
        this.getProxy().url = this.url + id + this.communicationsPostfix
    }
});


Ext.define('Dsh.controller.Connections', {
    extend: 'Dsh.controller.BaseController',

    models: [
        'Dsh.model.ConnectionTask',
        'Dsh.model.CommTasks',
        'Dsh.model.CommunicationTask',
        'Dsh.model.Filter'
    ],

    stores: [
        'Dsh.store.ConnectionTasks',
        'Dsh.store.Communications'
    ],

    views: [
        'Dsh.view.Connections',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.view.widget.connection.CommunicationsList',
        'Dsh.view.widget.connection.PreviewCommunication'
    ],

    refs: [
        {
            ref: 'connectionsList',
            selector: '#connectionsdetails'
        },
        {
            ref: 'connectionPreview',
            selector: '#connectionsdetails #connectionpreview'
        },
        {
            ref: 'communicationList',
            selector: '#connectionsdetails #communicationsdetails'
        },
        {
            ref: 'communicationPreview',
            selector: '#connectionsdetails #communicationpreview'
        },
        {
            ref: 'communicationsPanel',
            selector: '#connectionsdetails #communicationspanel'
        },
        {
            ref: 'filterPanel',
            selector: '#connectionsdetails filter-top-panel'
        },
        {
            ref: 'sideFilterForm',
            selector: '#connectionsdetails #filter-form'
        },
        {
            ref: 'connectionsActionMenu',
            selector: '#connectionsActionMenu'
        },
        {
            ref: 'connectionsPreviewActionMenu',
            selector: '#connectionsPreviewActionBtn #connectionsActionMenu'
        },
        {
            ref: 'communicationsGridActionMenu',
            selector: '#communicationsGridActionMenu'
        },
        {
            ref: 'communicationPreviewActionMenu',
            selector: '#communicationPreviewActionMenu'
        }
    ],

    prefix: '#connectionsdetails',

    init: function () {
        this.control({
            'connections-details #connectionsdetails': {
                selectionchange: this.onSelectionChange
            },
            'connections-details #communicationsdetails': {
                selectionchange: this.onCommunicationSelectionChange
            },
            '#connectionsActionMenu': {
                show: this.initConnectionMenu
            },
            'connections-list #generate-report': {
                click: this.onGenerateReport
            },
            'connections-details uni-actioncolumn': {
                run: this.connectionRun,
                viewLog: this.viewLog,
                viewHistory: this.viewHistory
            }

        });

        this.callParent(arguments);
    },

    showOverview: function () {
        var widget = Ext.widget('connections-details'),
            store = this.getStore('Dsh.store.ConnectionTasks');

        this.getApplication().fireEvent('changecontentevent', widget);
        this.initFilter();
        store.load();
    },

    onCommunicationSelectionChange: function (grid, selected) {
        var me = this,
            commPanel = me.getCommunicationsPanel(),
            record = selected[0],
            preview = me.getCommunicationPreview(),
            menuItems = [];

        commPanel.show();
        record.data.devConfig = {
            config: record.data.deviceConfiguration,
            devType: record.data.deviceType
        };

        record.data.title = record.data.comTask.name + ' on ' + record.data.device.name;
        preview.setTitle(record.data.title);
        preview.loadRecord(record);
        this.initMenu(record, menuItems);
    },

    initMenu: function (record, menuItems) {
        var me = this,
            gridActionMenu = this.getCommunicationsGridActionMenu().menu,
            previewActionMenu = this.getCommunicationPreviewActionMenu().menu;

        Ext.suspendLayouts();

        gridActionMenu.removeAll();
        previewActionMenu.removeAll();

        if (record.get('sessionId') !== 0) {
            menuItems.push({
                text: Ext.String.format(Uni.I18n.translate('connection.widget.details.menuItem', 'DSH', 'View \'{0}\' log'), record.get('comTask').name),
                action: {
                    action: 'viewlog',
                    comTask: {
                        mRID: record.get('device').id,
                        sessionId: record.get('id'),
                        comTaskId: record.get('comTask').id
                    }
                },
                listeners: {
                    click: me.viewCommunicationLog
                }
            });
        }

        gridActionMenu.add(menuItems);
        previewActionMenu.add(menuItems);

        Ext.resumeLayouts(true);
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            preview = me.getConnectionPreview(),
            commPanel = me.getCommunicationsPanel(),
            commStore = me.getStore('Dsh.store.Communications'),
            record = selected[0];

        if (!_.isEmpty(record)) {
            me.getConnectionsPreviewActionMenu().record = record;
            var id = record.get('id'),
                title = ' ' + record.get('title');

            preview.loadRecord(record);
            preview.setTitle(title);
            commPanel.setTitle(Uni.I18n.translate('connection.widget.details.communicationTasksOf', 'DSH', 'Communication tasks of') + title);

            if (id) {
                commStore.setConnectionId(id);
                commStore.load();
                commPanel.hide()
            }
        }
    },

    initConnectionMenu: function (menu) {
        if (menu && menu.record) {
            if (menu.record.get('comSessionId') !== 0) {
                menu.down('menuitem[action=viewLog]').show()
            } else {
                menu.down('menuitem[action=viewLog]').hide()
            }
        }
    },

    viewCommunicationLog: function (item) {
        location.href = '#/devices/' + item.action.comTask.mRID
        + '/communicationtasks/' + item.action.comTask.comTaskId
        + '/history/' + item.action.comTask.sessionId
        + '/viewlog' +
        '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D';
    },
    onGenerateReport: function () {
        var me = this;
        var router = this.getController('Uni.controller.history.Router');
        var fieldsToFilterNameMap = {};
        fieldsToFilterNameMap['deviceGroup'] = 'GROUPNAME';
        fieldsToFilterNameMap['currentStates'] = 'STATUS';
        fieldsToFilterNameMap['latestResults'] = null;
        fieldsToFilterNameMap['comSchedules'] = 'SCHEDULENAME';
        fieldsToFilterNameMap['deviceTypes'] = null;
        fieldsToFilterNameMap['comTasks'] = 'COMTASKNAME';
        fieldsToFilterNameMap['comPortPools'] = 'PORTPOOLNAME';
        fieldsToFilterNameMap['connectionTypes'] = 'CONNECTIONTYPE';

        var reportFilter = false;

        var fields = me.getSideFilterForm().getForm().getFields();
        fields.each(function (field) {
            reportFilter = reportFilter || {};
            var filterName = fieldsToFilterNameMap[field.getName()];
            if (filterName) {
                var fieldValue = field.getRawValue();
                if (field.getXType() == 'side-filter-combo') {
                    fieldValue = Ext.isString(fieldValue) && fieldValue.split(', ') || fieldValue;
                    fieldValue = _.isArray(fieldValue) && _.compact(fieldValue) || fieldValue;
                }
            }
            reportFilter[filterName] = fieldValue;
        });

        //handle special startBetween and finishBetween;

        if (router.filter && router.filter.startedBetween) {
            var from = router.filter.startedBetween.get('from');
            var to = router.filter.startedBetween.get('to');
            reportFilter['CONNECTIONDATE'] = {
                'from': from && Ext.Date.format(from, "Y-m-d H:i:s"),
                'to': to && Ext.Date.format(to, "Y-m-d H:i:s")
            };
        }


        //handle special startBetween and finishBetween;
        //router.filter.startedBetween
        //router.filter.finishBetween

        router.getRoute('generatereport').forward(null, {
            category: 'MDC',
            subCategory: 'Device Connections',
            filter: reportFilter
        });
    },

    connectionRun: function (record) {
        var me = this;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('connection.run.now', 'DSH', 'Run succeeded')
            );
            record.set('nextExecution', new Date());
            me.showOverview();
        });

    },

    viewLog: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/connectionmethods/history/viewlog').forward(
            {
                mRID: record.get('device').id,
                connectionMethodId: record.get('id'),
                historyId: record.get('comSessionId')
            });
    },

    viewHistory: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/connectionmethods/history').forward(
            {
                mRID: record.get('device').id,
                connectionMethodId: record.get('id')
            }
        );
    }


});

Ext.define('Dsh.view.widget.OpenDataCollectionIssues', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.open-data-collection-issues',
    buttonAlign: 'left',
    layout: 'fit',
    router: null,
    header: {
        ui: 'small'
    },

    setRecord: function (record) {
        var me = this,
            elm = me.down('#issues-dataview'),
            countContainer = me.down('#open-data-collection-issues-count-container'),
            dockedLinksContainer = me.down('#open-data-collection-issues-docked-links'),
            assigned = record.getAssignedToMeIssues(),
            unassigned = record.getUnassignedIssues(),
            store = assigned.topMyIssues(),
            issuesCount = store.getCount();


        var title = Uni.I18n.translatePlural('overview.widget.openDataCollectionIssues.header', assigned.get('total'), 'DSH', '<h3>' + 'Open data collection issues ({0})' + '</h3>');
        me.setTitle(title);

        store.each(function (item) {
            var dueDate = item.get('dueDate');
            item.set('href', me.router.getRoute('workspace/datacollectionissues/view').buildUrl({issueId: item.get('id')}));

            if (dueDate) {
                if (moment().isAfter(moment(dueDate))) {
                    item.set('tooltip', Uni.I18n.translate('overview.widget.openDataCollectionIssues.overdue', 'DSH', 'Overdue'));
                    item.set('icon', '/apps/dsh/resources/images/widget/blocked.png');
                } else {
                    if (moment().endOf('day').isAfter(moment(dueDate))) {
                        item.set('tooltip', Uni.I18n.translate('overview.widget.openDataCollectionIssues.dueToday', 'DSH', 'Due today'));
                        item.set('icon', '/apps/dsh/resources/images/widget/blocked.png');
                    } else {
                        if (moment().add(1, 'day').endOf('day').isAfter(moment(dueDate))) {
                            item.set('tooltip', Uni.I18n.translate('overview.widget.openDataCollectionIssues.dueTomorrow', 'DSH', 'Due tomorrow'));
                            item.set('icon', '/apps/dsh/resources/images/widget/inactive.png');
                        }
                    }
                }
            }
        });

        elm.bindStore(store);

        Ext.suspendLayouts();

        countContainer.removeAll();
        dockedLinksContainer.removeAll();

        if (issuesCount === 0) {
            countContainer.add({
                xtype: 'label',
                text: Uni.I18n.translate('operator.dashboard.issuesEmptyMsg', 'DSH', 'No open issues assigned to you.')
            });
        }
        if (issuesCount) {
            countContainer.add({
                xtype: 'container',
                html: Ext.String.format(Uni.I18n.translate('overview.widget.openDataCollectionIssues.topIssues', 'DSH', 'Top {0} most urgent issues assigned to me'), issuesCount)
            });
        }

        var assignedFilter = Ext.apply(assigned.get('filter'), {
            assignee: assigned.get('filter').assigneeId + ':' + assigned.get('filter').assigneeType,
            status: ['status.open']
        });

        var unassignedFilter = {
            assignee: unassigned.get('filter').assigneeId + ':' + unassigned.get('filter').assigneeType,
            status: ['status.open']
        };

        dockedLinksContainer.add([
            {
                xtype: 'button',
                itemId: 'lnk-assigned-issues-link',
                text: Ext.String.format(Uni.I18n.translate('overview.widget.openDataCollectionIssues.assignedToMe', 'DSH', 'Assigned to me ({0})'), assigned.get('total')),
                ui: 'link',
                href: me.router.getRoute('workspace/datacollectionissues').buildUrl(null, {filter: assignedFilter})
            },
            {
                xtype: 'button',
                itemId: 'lnk-unassigned-issues-link',
                text: Ext.String.format(Uni.I18n.translate('overview.widget.openDataCollectionIssues.unassigned', 'DSH', 'Unassigned ({0})'), unassigned.get('total')),
                ui: 'link',
                href: me.router.getRoute('workspace/datacollectionissues').buildUrl(null, {filter: unassignedFilter})
            }
        ]);
        Ext.resumeLayouts(true);
    },

    tbar: {
        xtype: 'container',
        itemId: 'open-data-collection-issues-count-container'
    },

    items: [
        {
            xtype: 'dataview',
            itemId: 'issues-dataview',
            overflowY: 'auto',
            itemSelector: 'a.x-btn.flag-toggle',

            tpl: new Ext.XTemplate(
                '<table  style="margin: 5px 0 10px 0">',
                '<tpl for=".">',
                '<tr id="{id}" class="issue">',
                '<td height="26" width="40" data-qtip="{tooltip}"><img style="margin: 5px 5px 0 0" src="{icon}" /></td>',
                '<td width="100%"><a href="{href}">{title}</a></td>',
                '</tr>',
                '</tpl>',
                '</table>'
            )
        }
    ],

    bbar: {
        xtype: 'container',
        itemId: 'open-data-collection-issues-docked-links'
    }
});

Ext.define('Dsh.view.widget.DeviceGroupFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-group-filter',
    layout: 'hbox',

    initComponent: function () {
        var me = this;
        var store = Ext.getStore('Dsh.store.filter.DeviceGroup' || 'ext-empty-store');

        this.items = [
            {
                xtype: 'container',
                html: Uni.I18n.translate('overview.widget.headerSection.filter', 'DSH', 'Filter'),
                cls: 'x-form-display-field',
                style: {
                    paddingRight: '10px'
                }
            },
            {
                xtype: 'button',
                style: {
                    'background-color': '#71adc7'
                },
                itemId: 'device-group',
                label: Uni.I18n.translate('overview.widget.headerSection.deviceGroupLabel', 'DSH', 'Device group') + ': ',
                arrowAlign: 'right',
                groupName: null, // yellowfin reports use names instead of id
                menuAlign: 'tl-bl',
                menu: {
                    enableScrolling: true,
                    maxHeight: 350,
                    itemId: 'mnu-device-group-filter',
                    router: me.router,
                    listeners: {
                        click: function (cmp, item) {
                            this.router.filter.set('deviceGroup', item.value);
                            this.router.filter.save();
                            this.groupName = Ext.isNumber(item.value) ? item.text : null;
                        }
                    }
                },
                setValue: function (value) {
                    var item = this.menu.items.findBy(function (item) {
                        return item.value == value
                    });
                    if (item) {
                        item.setActive();
                        this.setText(this.label + item.text);
                        this.groupName = Ext.isNumber(item.value) ? item.text : null;
                        this.fireEvent('change', this); // the event is handled by CommOverview and ConnOverview controllers to update quick links.
                    }

                }
            }
        ];

        this.callParent(arguments);

        var button = me.down('#device-group');
        Ext.suspendLayouts();

        store.load(function () {
            var menu = button.menu;
            menu.removeAll();
            menu.add({
                text: Uni.I18n.translate('overview.widget.headerSection.none', 'DSH', 'None'),
                value: ''
            });

            store.each(function (item) {
                menu.add({
                    text: item.get('name'),
                    value: item.get('id')
                })
            });

            button.setValue(me.router.filter.get('deviceGroup'));
        });

        Ext.resumeLayouts(true);
    }
});

Ext.define('Dsh.view.widget.FavoriteDeviceGroups', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.favorite-device-groups',
    ui: 'tile',
    store: 'Dsh.store.FavoriteDeviceGroups',
    initComponent: function () {
        this.callParent(arguments);
        var me = this,
            store = Ext.getStore(me.store);

        store.load({
            callback: function () {
                me.add([
                    {
                        xtype: 'container',
                        style: {
                            margin: '0 0 10px 0'
                        },
                        html: store.count() > 0 ?
                        '<h3>' + Ext.String.format(Uni.I18n.translate('overview.widget.favoriteDeviceGroups.header', 'DSH', 'My favorite device groups ({0})'), store.count()) + '</h3>' :
                        '<h3>' + Uni.I18n.translate('overview.widget.favoriteDeviceGroups.headerNoItemsFound', 'DSH', 'My favorite device groups') + '</h3>'
                    },
                    {
                        xtype: 'dataview',
                        store: me.store,
                        itemSelector: 'p a',
                        emptyText: Uni.I18n.translate('overview.widget.favoriteDeviceGroups.notFound', 'DSH', 'No favorite device groups found'),
                        overflowY: 'auto',
                        style: 'max-height: 120px',
                        tpl: new Ext.XTemplate(
                            '<table style="margin-top: 5px">',
                            '<tpl for=".">',
                            '<tr>',
                            '<td style="height: 20px">',
                            Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceGroup', 'privilege.view.deviceGroupDetail'])
                                ? '<a href="#/devices/devicegroups/{id}">{name}</a>' :
                                (
                                    Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceOfEnumeratedGroup']) ?
                                        (
                                            '<tpl if="dynamic==true"{dynamic}>{name}<tpl else><a href="#/devices/devicegroups/{id}">{name}</a></tpl>'
                                        )
                                        :
                                        '{name}'
                                ),
                            '</td>',
                            '</tr>',
                            '</tpl>',
                            '</table>'
                        )
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('overview.widget.favoriteDeviceGroups.selectBtn', 'DSH', 'Select'),
                        //hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceGroup'),
                        style: 'margin-top: 15px',
                        href: '#/dashboard/selectfavoritedevicegroups'
                    }
                ]);
            }
        });
    }
});


Ext.define('Dsh.view.widget.FlaggedDevices', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.flagged-devices',
    buttonAlign: 'left',
    layout: 'fit',
    title: '<h3>' + Uni.I18n.translatePlural('overview.widget.flaggedDevices.header', 0, 'DSH', 'My flagged devices ({0})') + '</h3>',
    router: null,
    header: {
        ui: 'small'
    },

    tooltipTpl: new Ext.XTemplate(
        '<table>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.mrid', 'DSH', 'MRID') + '</td>',
        '<td>{mRID}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.serialNumber', 'DSH', 'Serial number') + '</td>',
        '<td>{serialNumber}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.deviceTypeName', 'DSH', 'Device Type') + '</td>',
        '<td>{deviceTypeName}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.creationDate', 'DSH', 'Flagged date') + '</td>',
        '<td>{[Uni.DateTime.formatDateTimeLong(values.deviceLabelInfo.creationDate)]}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.comment', 'DSH', 'Comment') + '</td>',
        '<td>{[values.deviceLabelInfo.comment]}</td>',
        '</tr>',
        '</table>'
    ),

    items: {
        xtype: 'dataview',
        store: 'Dsh.store.FlaggedDevices',
        itemId: 'devices-dataview',
        style: 'max-height: 160px',
        overflowY: 'auto',
        itemSelector: 'a.x-btn.flag-toggle',
        emptyText: Uni.I18n.translate('overview.widget.flaggedDevices.noDevicesFound', 'DSH', 'No flagged devices found'),

        tpl: new Ext.XTemplate(
            '<table  style="margin: 5px 0 10px 0">',
            '<tpl for=".">',
            '<tr id="{mRID}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" class="device">',
            '<td width="100%"><a href="{href}">{mRID}</a></td>',
            '<td>',
            '<a data-qtip="' +
            Uni.I18n.translate('overview.widget.flaggedDevices.unflag', 'DSH', 'Click to remove from the list of flagged devices') +
            '" class="flag-toggle x-btn x-btn-plain-small">',
            '<span style="width: 16px; height: 16px; font-size: 16px" class="x-btn-button"><span class="x-btn-icon-el icon-star6"></span></span></a>',
            '</td>',
            '</tr>',
            '</tpl>',
            '</table>'
        ),

        listeners: {
            'itemclick': function (view, record, item) {
                var elm = new Ext.dom.Element(item);
                var icon = elm.down('.x-btn-icon-el');
                var pressed = icon.hasCls('icon-star6');
                var flag = record.getLabel();
                flag.proxy.setUrl(record.getId());

                var callback = function () {
                    icon.toggleCls('icon-star6');
                    icon.toggleCls('icon-star4');
                    elm.set({
                        'data-qtip': pressed
                            ? Uni.I18n.translate('overview.widget.flaggedDevices.flag', 'DSH', 'Click to flag the device')
                            : Uni.I18n.translate('overview.widget.flaggedDevices.unflag', 'DSH', 'Click to remove from the list of flagged devices')
                    });
                };

                pressed ? view.unflag(flag, callback) : view.flag(flag, callback);
            }
        },

        flag: function (record, callback) {
            var clone = new record.self();
            var data = record.getWriteData(false, true);
            clone.set(data);
            clone.save({callback: callback});
        },

        unflag: function (record, callback) {
            record.destroy({callback: callback});
        }
    },

    reload: function () {
        var me = this,
            elm = me.down('#devices-dataview'),
            store = elm.getStore();

        store.load(function () {
            var title = '<h3>' + Uni.I18n.translatePlural('overview.widget.flaggedDevices.header', store.count(), 'DSH', 'My flagged devices ({0})') + '</h3>';
            me.setTitle(title);

            store.each(function (item) {
                item.set('href', me.router.getRoute('devices/device').buildUrl({mRID: item.getId()}));
                item.set('tooltip', me.tooltipTpl.apply(item.getData(true)));
            });

            elm.bindStore(store);
        });
    }
});

Ext.define('Dsh.view.MyFavoriteDeviceGroups', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.my-favorite-device-groups',
    itemId: 'my-favorite-device-groups',
    title: Uni.I18n.translate('myFavoriteDeviceGroups.pageTitle', 'DSH', 'Select favorite device groups'),
    ui: 'large',
    margin: '0 20',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.add({
            xtype: 'emptygridcontainer',
            grid: {
                xtype: 'grid',
                itemId: 'my-favorite-device-groups-grid',
                extend: 'Uni.view.grid.SelectionGrid',
                store: 'Dsh.store.FavoriteDeviceGroups',
                margin: '0 40 0 0',
                disableSelection: true,
                overflowY: 'auto',
                maxHeight: 450,
                viewConfig: {
                    markDirty: false
                },
                tbar: [
                    {
                        xtype: 'panel',
                        layout: 'hbox',
                        defaults: {
                            style: {
                                marginRight: '20px'
                            }
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                itemId: 'selected-groups-summary',
                                margin: '0 20 0 0'
                            },
                            {
                                xtype: 'button',
                                text: 'Uncheck all',
                                action: 'uncheckall'
                            }
                        ]
                    }

                ],
                columns: [
                    {
                        xtype: 'checkcolumn',
                        dataIndex: 'favorite',
                        width: 35
                    },
                    {
                        header: Uni.I18n.translate('myFavoriteDeviceGroups.grid.column.name', 'DSH', 'Name'),
                        dataIndex: 'name',
                        flex: 1
                    },
                    {
                        header: Uni.I18n.translate('myFavoriteDeviceGroups.grid.column.type', 'DSH', 'Type'),
                        dataIndex: 'dynamic',
                        flex: 1,
                        renderer: function (value) {
                            return value ?
                                Uni.I18n.translate('myFavoriteDeviceGroups.grid.type.dynamic', 'DSH', 'Dynamic') :
                                Uni.I18n.translate('myFavoriteDeviceGroups.grid.type.static', 'DSH', 'Static');
                        }
                    }
                ],
                buttonAlign: 'left',
                buttons: [
                    {
                        xtype: 'button',
                        text: 'Save',
                        action: 'save',
                        ui: 'action'
                    },
                    {
                        xtype: 'button',
                        text: 'Cancel',
                        href: '#/dashboard',
                        ui: 'link'
                    }
                ]
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('workspace.myFavoriteDeviceGroups.empty.title', 'DSH', 'No device groups found'),
                reasons: [
                    Uni.I18n.translate('workspace.myFavoriteDeviceGroups.empty.list.reason1', 'DSH', 'No device groups have been defined yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('workspace.myFavoriteDeviceGroups.empty.list.action1', 'DSH', 'Add device group'),
                        action: 'addItem'
                    }
                ]
            }
        });
    }
});

Ext.define('Dsh.view.OperatorDashboard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Dsh.view.widget.HeaderSection',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.OpenDataCollectionIssues',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown',
        'Dsh.view.widget.DeviceGroupFilter',
        'Dsh.view.widget.FavoriteDeviceGroups',
        'Dsh.view.widget.FlaggedDevices',
        'Dsh.view.MyFavoriteDeviceGroups'
    ],
    alias: 'widget.operator-dashboard',
    itemId: 'operator-dashboard',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        'padding-left': '20px'
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'panel',
                ui: 'large',
                title: me.router.getRoute().title,
                tools: [
                    {
                        xtype: 'toolbar',
                        items: [
                            '->',
                            {
                                xtype: 'component',
                                itemId: 'last-updated-field',
                                width: 150,
                                style: {
                                    'font': 'normal 13px/17px Lato',
                                    'color': '#686868',
                                    'margin-right': '10px'
                                }
                            },
                            {
                                xtype: 'button',
                                itemId: 'refresh-btn',
                                style: {
                                    'background-color': '#71adc7'
                                },
                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                                icon: '/apps/sky/resources/images/form/restore.png'
                            }
                        ]
                    }
                ],
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    },
                    flex: 1
                },
                items: []
            },
            {
                xtype: 'toolbar',
                margin: '50 0 0 0',
                items: {
                    xtype: 'device-group-filter',
                    hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication']),
                    router: me.router
                }
            },
            {
                xtype: 'panel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                height: 500,
                defaults: {
                    flex: 1,
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    }
                },
                style: {'margin-right': '20px'},
                items: [],
                dockedItems: [{
                    xtype: 'communication-servers',
                    width: 300,
                    dock: 'right',
                    hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication']),
                    itemId: 'communication-servers',
                    router: me.router,
                    style: 'border-width: 1px !important'   // Andrea: Should be fixed with CSS
                }]
            }
        ];

        if (Uni.Auth.hasAnyPrivilege(['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'])) {
            me.items[0].items.push(
                {
                    xtype: 'open-data-collection-issues',
                    itemId: 'open-data-collection-issues',
                    router: me.router
                });
        }
        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceData', 'privilege.view.device', 'privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication'])) {
            me.items[0].items.push(
                {
                    xtype: 'flagged-devices',
                    itemId: 'flagged-devices',
                    router: me.router
                });
        }
        //if(Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceGroup','privilege.administrate.deviceOfEnumeratedGroup','privilege.view.deviceGroupDetail'])) {
        me.items[0].items.push(
            {
                xtype: 'favorite-device-groups',
                itemId: 'favorite-device-groups'
            });
        //}
        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication'])) {
            me.items[2].items.push(
                {
                    xtype: 'summary',
                    itemId: 'connection-summary',
                    wTitle: Uni.I18n.translate('dashboard.widget.connections.title', 'DSH', 'Active connections'),
                    router: me.router,
                    parent: 'connections',
                    buttonAlign: 'left',
                    buttons: [{
                        text: Uni.I18n.translate('dashboard.widget.connections.link', 'DSH', 'View connections overview'),
                        itemId: 'lnk-connections-overview',
                        ui: 'link',
                        href: typeof me.router.getRoute('workspace/connections') !== 'undefined'
                            ? me.router.getRoute('workspace/connections').buildUrl(null, me.router.queryParams) : ''
                    }]
                },
                {
                    xtype: 'summary',
                    itemId: 'communication-summary',
                    wTitle: Uni.I18n.translate('dashboard.widget.communications.title', 'DSH', 'Active communications'),
                    parent: 'communications',
                    router: me.router,
                    buttonAlign: 'left',
                    buttons: [{
                        text: Uni.I18n.translate('dashboard.widget.communications.link', 'DSH', 'View communications overview'),
                        itemId: 'lnk-communications-overview',
                        ui: 'link',
                        href: typeof me.router.getRoute('workspace/communications') !== 'undefined'
                            ? me.router.getRoute('workspace/communications').buildUrl(null, me.router.queryParams) : ''
                    }]
                });
        }
        this.callParent(arguments);
    }
});

Ext.define('Dsh.model.connection.OverviewDashboard', {
    extend: 'Dsh.model.connection.Overview',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/connectionoverview/widget'
    }
});

Ext.define('Dsh.model.communication.OverviewDashboard', {
    extend: 'Dsh.model.communication.Overview',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/communicationoverview/widget'
    }
});

Ext.define('Dsh.model.TopMyIssue', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'title', type: 'string'},
        {name: 'dueDate', type: 'auto'}
    ]
});

Ext.define('Dsh.model.AssignedToMeIssues', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.TopMyIssue'
    ],

    fields: [
        {name: 'total', type: 'int'},
        {name: 'filter', type: 'auto'}
    ],
    hasMany: [
        {
            model: 'Dsh.model.TopMyIssue',
            name: 'topMyIssues'
        }
    ]
});

Ext.define('Dsh.model.UnassignedIssues', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'total', type: 'int'},
        {name: 'filter', type: 'auto'}
    ]
});

Ext.define('Dsh.model.opendatacollectionissues.Overview', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.AssignedToMeIssues',
        'Dsh.model.UnassignedIssues'
    ],
    hasOne: [
        {
            model: 'Dsh.model.UnassignedIssues',
            associationKey: 'unassignedIssues',
            name: 'unassignedIssues',
            getterName: 'getUnassignedIssues',
            setterName: 'setUnassignedIssues'
        },
        {
            model: 'Dsh.model.AssignedToMeIssues',
            associationKey: 'assignedToMeIssues',
            name: 'assignedToMeIssues',
            getterName: 'getAssignedToMeIssues',
            setterName: 'setAssignedToMeIssues'
        }
    ],

    proxy: {
        type: 'ajax',
        url: '/api/dsr/myopenissuesoverview'
    }
});

Ext.define('Dsh.model.DeviceGroup', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'dynamic', type: 'boolean'},
        {name: 'favorite', type: 'boolean'},
        {name: 'criteria'}
    ]
});

Ext.define('Dsh.store.FavoriteDeviceGroups', {
    extend: 'Ext.data.Store',
    storeId: 'FavoriteDeviceGroups',
    requires: ['Dsh.model.DeviceGroup'],
    model: 'Dsh.model.DeviceGroup',
    proxy: {
        type: 'ajax',
        url: '../../api/dsr/favoritedevicegroups',
        reader: {
            type: 'json',
            root: 'favoriteDeviceGroups'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});

Ext.define('Dsh.model.FlaggedDevice', {
    extend: 'Ext.data.Model',
    idProperty: 'mRID',

    requires: ['Mdc.model.DeviceLabel'],

    fields: [
        {name: 'mRID', type: 'string'},
        {name: 'serialNumber', type: 'string'},
        {name: 'deviceTypeName', type: 'string'}
    ],

    hasOne: {
        model: 'Mdc.model.DeviceLabel',
        name: 'deviceLabelInfo',
        associationKey: 'deviceLabelInfo',
        getterName: 'getLabel'
    }
});

Ext.define('Dsh.store.FlaggedDevices', {
    extend: 'Ext.data.Store',
    storeId: 'FlaggedDevices',
    requires: ['Dsh.model.FlaggedDevice'],
    model: 'Dsh.model.FlaggedDevice',
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/dsr/mylabeleddevices?category=mdc.label.category.favorites',
        pageParam: false,
        limitParam: false,

        reader: {
            type: 'json',
            root: 'myLabeledDevices'
        }
    }
});

Ext.define('Dsh.controller.OperatorDashboard', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.connection.Overview',
        'Dsh.model.connection.OverviewDashboard',
        'Dsh.model.communication.Overview',
        'Dsh.model.communication.OverviewDashboard',
        'Dsh.model.opendatacollectionissues.Overview'
    ],
    stores: [
        'CommunicationServerInfos',
        'Dsh.store.CombineStore',
        'Dsh.store.ConnectionResultsStore',
        'Dsh.store.FavoriteDeviceGroups',
        'Dsh.store.FlaggedDevices'
    ],
    views: ['Dsh.view.OperatorDashboard'],

    refs: [
        {ref: 'dashboard', selector: '#operator-dashboard'},
        {ref: 'header', selector: 'operator-dashboard #header-section'},
        {ref: 'connectionSummary', selector: 'operator-dashboard #connection-summary'},
        {ref: 'communicationSummary', selector: 'operator-dashboard #communication-summary'},
        {ref: 'summary', selector: ' operator-dashboard #summary'},
        {ref: 'communicationServers', selector: 'operator-dashboard #communication-servers'},
        {ref: 'flaggedDevices', selector: 'operator-dashboard #flagged-devices'},
        {ref: 'issuesWidget', selector: 'operator-dashboard #open-data-collection-issues'},
        {ref: 'favoriteDeviceGroupsView', selector: 'operator-dashboard #favorite-device-groups dataview'},
        {ref: 'favoriteDeviceGroups', selector: '#my-favorite-device-groups'},
        {ref: 'favoriteDeviceGroupsGrid', selector: '#my-favorite-device-groups-grid'},
        {ref: 'summaryOfSelected', selector: '#selected-groups-summary'},
        {ref: 'uncheckAllBtn', selector: '#my-favorite-device-groups button[action=uncheckall]'}
    ],

    init: function () {
        this.control({
            '#operator-dashboard #refresh-btn': {
                click: this.loadData
            },
            '#my-favorite-device-groups-grid': {
                render: this.afterFavoriteDeviceGroupsGridRender
            },
            '#my-favorite-device-groups-grid checkcolumn': {
                checkchange: this.onFavoriteGroupsGridSelectionChange
            },
            '#my-favorite-device-groups button[action=uncheckall]': {
                click: this.uncheckAllSelectedGroups
            },
            '#my-favorite-device-groups button[action=save]': {
                click: this.saveFavoriteGroups
            },
            '#my-favorite-device-groups [action=addItem]': {
                click: function () {
                    var router = this.getController('Uni.controller.history.Router');
                    router.getRoute('devices/devicegroups/add').forward();
                }
            }
        });
    },

    showMyFavorieDeviceGroups: function () {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('my-favorite-device-groups'));
    },

    afterFavoriteDeviceGroupsGridRender: function () {
        var me = this;
        me.getFavoriteDeviceGroupsGrid().getStore().load({
            params: {
                includeAllGroups: true
            },
            callback: function () {
                me.onFavoriteGroupsGridSelectionChange();
            }
        });
    },

    onFavoriteGroupsGridSelectionChange: function () {
        var summaryOfSelectedField = this.getSummaryOfSelected(),
            store = this.getFavoriteDeviceGroupsGrid().getStore(),
            selectedGroups = store.queryBy(function (record) {
                return record.get('favorite') === true;
            }), selectedGroupsQty = selectedGroups.items.length;

        summaryOfSelectedField.setValue(
            selectedGroupsQty > 0 ?
                Uni.I18n.translatePlural('myFavoriteDeviceGroups.summarySelectedTpl', selectedGroupsQty, 'DSH', '{0} device groups selected') :
                Uni.I18n.translate('myFavoriteDeviceGroups.summaryTplNoSelected', 'DSH', 'No device groups selected')
        );

        this.getUncheckAllBtn().setDisabled(selectedGroupsQty < 1);
    },

    uncheckAllSelectedGroups: function () {
        this.getFavoriteDeviceGroupsGrid().getStore().each(function (record) {
            record.set('favorite', false);
        });

        this.onFavoriteGroupsGridSelectionChange();
    },

    saveFavoriteGroups: function () {
        var me = this, ids = [],
            router = me.getController('Uni.controller.history.Router'),
            store = me.getFavoriteDeviceGroupsGrid().getStore(),
            selectedGroups = store.queryBy(function (record) {
                return record.get('favorite') === true;
            });

        selectedGroups.each(function (group) {
            ids.push(group.get('id'));
        });

        me.getFavoriteDeviceGroups().setLoading(true);
        Ext.Ajax.request({
            url: store.proxy.url,
            method: 'PUT',
            jsonData: {ids: ids},
            success: function () {
                router.getRoute('dashboard').forward();
            },
            callback: function () {
                me.getFavoriteDeviceGroups().setLoading(false);
            }
        });
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('operator-dashboard', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            dashboard = me.getDashboard(),
            lastUpdateField = dashboard.down('#last-updated-field');

        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication', 'privilege.view.device',
                'privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'])) {
            var connectionModel = me.getModel('Dsh.model.connection.OverviewDashboard'),
                communicationModel = me.getModel('Dsh.model.communication.OverviewDashboard'),
                myOpenIssuesModel = me.getModel('Dsh.model.opendatacollectionissues.Overview'),
                issuesWidget = me.getIssuesWidget(),
                router = this.getController('Uni.controller.history.Router');
            if (Uni.Auth.hasAnyPrivilege(['privilege.view.device'])) {
                me.getFlaggedDevices().reload();
            }

            communicationModel.getProxy().url = '/api/dsr/communicationoverview/widget';
            connectionModel.getProxy().url = '/api/dsr/connectionoverview/widget';

            if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication'])) {
                connectionModel.setFilter(router.filter);
                communicationModel.setFilter(router.filter);
                dashboard.setLoading();
                me.getCommunicationServers().reload();
                connectionModel.load(null, {
                    success: function (connections) {
                        if (me.getConnectionSummary()) {
                            me.getConnectionSummary().setRecord(connections.getSummary());
                        }
                    },
                    callback: function () {
                        communicationModel.load(null, {
                            success: function (communications) {
                                if (me.getCommunicationSummary()) {
                                    me.getCommunicationSummary().setRecord(communications.getSummary());
                                }
                            },
                            callback: function () {
                                if (lastUpdateField) {
                                    lastUpdateField.update('Last updated at ' + Uni.DateTime.formatTimeShort(new Date()));
                                }
                                dashboard.setLoading(false);
                            }
                        });
                    }
                });
            }

            if (Uni.Auth.hasAnyPrivilege(['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'])) {
                issuesWidget.setLoading();
                myOpenIssuesModel.load(null, {
                    success: function (issues) {
                        issuesWidget.setRecord(issues);
                        issuesWidget.setLoading(false);
                    }
                });
            }
        }
    }
});

Ext.define('Dsh.model.OverviewFilter', {
    extend: 'Ext.data.Model',
    requires: ['Uni.data.proxy.QueryStringProxy'],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        {name: 'id', persist: false},
        {name: 'deviceGroup', type: 'auto'}
    ]
});

//TODO: localize all strings
Ext.define('Dsh.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    requires: [
        'Dsh.model.OverviewFilter',
        'Dsh.model.Filter'
    ],

    routeConfig: {
        workspace: {
            title: 'Workspace',
            route: 'workspace',
            disabled: true,
            items: {
                connections: {
                    title: Uni.I18n.translate('title.connections.overview', 'DSH', 'Connections overview'),
                    route: 'connections',
                    controller: 'Dsh.controller.ConnectionOverview',
                    privileges: ['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication', 'privilege.view.device'],
                    action: 'showOverview',
                    filter: 'Dsh.model.OverviewFilter',
                    items: {
                        details: {
                            title: Uni.I18n.translate('title.connections', 'DSH', 'Connections'),
                            route: 'details',
                            controller: 'Dsh.controller.Connections',
                            privileges: ['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication', 'privilege.view.device'],
                            action: 'showOverview',
                            filter: 'Dsh.model.Filter'
                        }
                    }
                },
                communications: {
                    title: Uni.I18n.translate('title.communications.overview', 'DSH', 'Communications overview'),
                    route: 'communications',
                    controller: 'Dsh.controller.CommunicationOverview',
                    privileges: ['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication', 'privilege.view.device'],
                    action: 'showOverview',
                    filter: 'Dsh.model.OverviewFilter',
                    items: {
                        details: {
                            title: Uni.I18n.translate('title.communications', 'DSH', 'Communications'),
                            route: 'details',
                            controller: 'Dsh.controller.Communications',
                            privileges: ['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication', 'privilege.view.device'],
                            action: 'showOverview',
                            filter: 'Dsh.model.Filter'
                        }
                    }
                }
            }
        },

        dashboard: {
            title: Uni.I18n.translate('title.dashboard', 'DSH', 'Dashboard'),
            route: 'dashboard',
            controller: 'Dsh.controller.OperatorDashboard',
            action: 'showOverview',
            filter: 'Dsh.model.OverviewFilter',
            items: {
                selectfavoritedevicegroups: {
                    title: Uni.I18n.translate('title.selectFavoriteDeviceGroups', 'DSH', 'Select favorite device groups'),
                    route: 'selectfavoritedevicegroups',
                    controller: 'Dsh.controller.OperatorDashboard',
                    //privileges: ['privilege.administrate.deviceGroup'],
                    action: 'showMyFavorieDeviceGroups'
                }
            }
        }
    }
});

Ext.define('Dsh.store.filter.CompletionCodes', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'completionCode'],
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/completioncodes',
        reader: {
            type: 'json',
            root: 'completionCodes'
        }
    }
});

Ext.define('Dsh.store.filter.DeviceGroup', {
    extend: 'Ext.data.Store',
    requires: ['Dsh.model.DeviceGroup'],
    model: 'Dsh.model.DeviceGroup',

    proxy: {
        type: 'rest',
        url: '/api/ddr/devicegroups',
        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,

        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});


Ext.define('Dsh.store.CommunicationResultsStore', {
    extend: 'Uni.data.store.Filterable',
    model: 'Dsh.model.ConnectionResults',
    autoLoad: false,
    remoteFilter: true,
    proxy: {
        type: 'ajax',
        url: '/api/dsr/communicationheatmap',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'heatMap'
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
        'Uni.store.MenuItems',
        'Dsh.util.FilterStoreHydrator',
        'Dsh.model.Filterable',
        'Dsh.model.Kpi',
        'Dsh.model.Series',
        'Dsh.controller.OperatorDashboard'
    ],

    controllers: [
        'Dsh.controller.history.Workspace',
        'Dsh.controller.BaseController',
        'Dsh.controller.CommunicationOverview',
        'Dsh.controller.ConnectionOverview',
        'Dsh.controller.OperatorDashboard',
        'Dsh.controller.Connections',
        'Dsh.controller.Communications'
    ],

    stores: [
        'Dsh.store.ConnectionTasks',
        'Dsh.store.CommunicationTasks',
        'Dsh.store.filter.CurrentState',
        'Dsh.store.filter.LatestStatus',
        'Dsh.store.filter.LatestResult',
        'Dsh.store.filter.CommPortPool',
        'Dsh.store.filter.ConnectionType',
        'Dsh.store.filter.DeviceType',
        'Dsh.store.filter.CompletionCodes',
        'Dsh.store.filter.DeviceGroup',
        'Dsh.store.ConnectionResultsStore',
        'Dsh.store.CommunicationResultsStore',
        'Dsh.store.CombineStore'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            historian = me.getController('Dsh.controller.history.Workspace'); // Forces route registration.

        var route = router.getRoute('dashboard');
        Uni.store.MenuItems.add(
            Ext.create('Uni.model.MenuItem', {
                text: route.title,
                glyph: 'home',
                portal: 'dashboard',
                index: 0
            })
        );

        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication', 'privilege.view.device'])) {
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
                            text: Uni.I18n.translate('title.connections', 'DSH', 'Connections'),
                            href: router.getRoute('workspace/connections/details').buildUrl()
                        },
                        {
                            text: Uni.I18n.translate('title.connections.overview', 'DSH', 'Connections overview'),
                            href: router.getRoute('workspace/connections').buildUrl()
                        },
                        {
                            text: Uni.I18n.translate('title.communications', 'DSH', 'Communications'),
                            href: router.getRoute('workspace/communications/details').buildUrl()
                        },
                        {
                            text: Uni.I18n.translate('title.communications.overview', 'DSH', 'Communications overview'),
                            href: router.getRoute('workspace/communications').buildUrl()
                        }
                    ]
                })
            );
        }
    }
});

Ext.define('Dsh.model.Configuration', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'string'},
        {name: 'title', type: 'string'}
    ]
});


Ext.define('Dsh.model.Result', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'string'},
        {name: 'displayName', type: 'string'},
        {name: 'count', type: 'int'}
    ]
});

Ext.define('Dsh.model.TimeInfo', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'count', type: 'int'},
        {name: 'timeUnit', type: 'int'}
    ]
});

