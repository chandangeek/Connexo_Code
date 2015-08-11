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
        if (title && categories) {
            title = title[0].toUpperCase() + title.slice(1);
            me.chart.series[0].xAxis.update({title: {text: title}}, false);
            me.chart.series[0].xAxis.update({categories: categories}, false);
        }
    },

    setYAxis: function (categories, title) {
        var me = this;
        if (title && categories) {
            title = title[0].toUpperCase() + title.slice(1);
            for(var i=0;i<categories.length;i++){
                if (categories[i].search(/</) != -1) {
                    categories[i] = categories[i].replace(/</g, '&lt;');
                }
            }
            me.chart.series[0].yAxis.update({title: {text: title}}, false);
            me.chart.series[0].yAxis.update({categories: categories}, false);
        }
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
        return { max: max, total: totalCount};
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
        }
    },

    reload: function () {
        var me = this,
            store = me.store,
            chartHeight;


        if (me.parent != 'connections' || (me.getCombo() && me.getCombo().getValue())) {
            me.setLoading();
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
                    me.setLoading(false);
                }
            });
        }
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