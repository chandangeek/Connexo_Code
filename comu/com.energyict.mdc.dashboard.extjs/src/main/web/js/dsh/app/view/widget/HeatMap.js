Ext.define('Dsh.view.widget.HeatMap', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.heat-map',
    layout: 'fit',
    minHeight: 450,
    tbar: [ '->',
        {
            xtype: 'fieldcontainer',
            fieldLabel: 'Combine',
            items: {
                xtype: 'combobox',
                itemId: 'combine-combo',
                displayField: 'localizedValue',
                queryMode: 'local',
                valueField: 'breakdown',
                store: 'Dsh.store.CombineStore'
            }
        }, '->'
    ],

    items: {
        xtype: 'box',
        minHeight: 400,
        itemId: 'heatmapchart'
    },

    setChartData: function (data) {
        var me = this;
        me.chart.series[0].setData([], true);
        Ext.defer(me.chart.series[0].setData(data, true), 100);
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

    storeToHighchartData: function (store, fields) {
        var data = [],
            x = 0,
            y = 0;
        store.each(function (rec) {
            Ext.each(fields, function (item) {
                var value = rec.get(item);
                (value == 0) && (value = value.toString());
                data.push([x, y, value]);
                ++x;
            });
            x = 0;
            ++y;
        });
        return data;
    },

    getCombo: function () {
        return this.down('#combine-combo');
    },

    loadChart: function (alias) {


//        var me = this,
//            ycat = ['Success count', 'Failed count', 'Pending count'],
//            xcat = record.counters().collect('displayName')
//            ;
//
//        me.setYAxis(xcat, 'Latest result');
//        me.setXAxis(ycat, record.get('displayName'));
//        me.setChartData(me.storeToHighchartData(record.counters(), [
//            "successCount",
//            "failedCount",
//            "pendingCount"
//        ]));
    },

    initComponent: function () {
        var me = this,
            store = Ext.getStore('Dsh.store.ConnectionResultsStore');
        this.callParent(arguments);

        store.on('load', function (store, records) {
            records && ()
        });

        me.getCombo().on('change', function (combo, newValue) {
            store.proxy.extraparams = {filters: '[{"property":"breakdown","value":"deviceType"}]'}
        });

        var cmp = me.down('#heatmapchart');
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