Ext.define('Dsh.view.widget.HeatMap', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.heat-map',
    layout: 'fit',
    items: {
        xtype: 'panel',
        ui: 'tile',
        minHeight: '400',
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

    storeToHighchartData: function (store) {
        var data = [],
            x = 0,
            y = 0;

        store.each(function (rec) {
            Ext.each(rec.data.data, function (item) {
                var count = item.count,
                    value = (count == 0 ? count.toString() : count);
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
        var me = this,
            xTitle = '';

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
                    autoSelect: true
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
            store = me.store;

        me.setLoading();
        store.on('load', function () {
            var cmp = me.down('#heatmapchart');
            if (store.count() && cmp) {
                me.show();
                cmp.setHeight(store.count() * 100);
                me.renderChart(cmp.getEl().down('.x-panel-body').dom);
                me.loadChart(store, me.getCombo() ? me.getCombo().getDisplayValue() : 'Device types');
                cmp.doLayout();
            } else {
                me.hide();
            }
            me.setLoading(false);
        });

        store.load();
    },

    renderChart: function (container) {
        var me = this;
        var width = container.offsetWidth;
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
                min: 0,
                minColor: '#FFFFFF',
                maxColor: Highcharts.getOptions().colors[0]
            },
            legend: {
                align: 'right',
                layout: 'vertical',
                verticalAlign: 'top',
                symbolHeight: 250
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