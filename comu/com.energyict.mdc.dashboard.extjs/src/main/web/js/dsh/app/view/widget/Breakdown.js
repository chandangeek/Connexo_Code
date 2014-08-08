Ext.define('Dsh.view.widget.Breakdown', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.breakdown',
    height: 450,
    itemId: 'breakdown',
    title: 'Breakdown', // TODO: localize
    ui: 'medium',
    colspan: 4,
    layout: {
        type: 'vbox',
        align: 'center'
    },
    style: {
        paddingTop: 0
    },
    items: [
        {
            xtype: 'fieldcontainer',
            fieldLabel: 'Combine',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: {
                xtype: 'combobox',
                itemId: 'brakdownchartcombinecombobox',
                margin: '0 20 0 0',
                displayField: 'displayValue',
                valueField: 'value'
            }
        },
        {
            xtype: 'container',
            width: 800,
            itemId: 'heatmapchart',
            listeners: {
                afterrender: function (container) {
                    var me = container.up('#breakdown');
                    $(function () {
                        $('#' + container.getId()).highcharts({
                            chart: {
                                type: 'heatmap'
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
                            legend: {
                                align: 'right',
                                layout: 'vertical',
                                margin: 0,
                                verticalAlign: 'top',
                                symbolHeight: 300
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

                        });
                    });
                    me.chart = $('#' + container.getId()).highcharts();
                }
            }
        }
    ],

    setChartData: function (data) {
        var me = this;
        me.chart.series[0].setData([], true);
        Ext.defer(me.chart.series[0].setData(data, true), 500);
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
                ++y;
            });
            y = 0;
            ++x;
        });
        return data;
    },

    initComponent: function () {
        var me = this;
        me.on('afterrender', function (cont) {
            me.combineCombo = cont.down('#brakdownchartcombinecombobox')
        });
        me.callParent(arguments)
    }
})
;