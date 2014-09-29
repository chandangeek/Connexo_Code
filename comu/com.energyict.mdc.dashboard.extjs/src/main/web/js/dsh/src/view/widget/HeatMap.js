Ext.define('Dsh.view.widget.HeatMap', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.heat-map',
    layout: 'fit',
    minHeight: 450,
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
        if (me.parent == 'connections') {
            me.tbar = [
                '->',
                {
                    xtype: 'combobox',
                    fieldLabel: Uni.I18n.translate('overview.widget.connections.heatmap.combineLabel', 'DSH', 'Combine latest result and'),
                    labelWidth: 200,
                    itemId: 'combine-combo',
                    displayField: 'localizedValue',
                    queryMode: 'local',
                    valueField: 'breakdown',
                    store: 'Dsh.store.CombineStore',
                    autoSelect: true
                },
                '->'
            ];
        } else if (me.parent == 'communications') {
            me.tbar = [
                '->',
                {
                    xtype: 'label',
                    text: Uni.I18n.translate('overview.widget.communications.heatmap.combineLabel', 'DSH', 'Combine latest result and device type')
                },
                '->'
            ];
            xTitle = 'Device types'
        }
        this.callParent(arguments);
        var cmp = me.down('#heatmapchart');
        store.on('load', function () {
            me.loadChart(store, xTitle)
        });
        if (me.parent == 'connections') {
            var combo = me.getCombo();
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
        } else if (me.parent == 'communications') {
            store.load();
        }
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