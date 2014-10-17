Ext.define('Dsh.view.widget.ReadOutsOverTime', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.read-outs-over-time',
    itemId: 'read-outs-over-time',
    hidden: true,
    layout: 'fit',

    initComponent: function () {
        var me = this;
        this.tbar =  [
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
                hidden: true,
                itemId: 'chart',
                height: 400
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

        if (!filter.get('deviceGroup')) {
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
        this.chart = new Highcharts.Chart({
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
                    lineWidth: 2,
                    type: 'datetime',
                    dateTimeLabelFormats: {
                        day: '%H:%M'
                    }
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
//                    gridLineWidth: 0,
                    floor: 0,
                    ceiling: 100,
                    tickInterval: 10
                }
            }, function () {
                me.doLayout();
            }
        );
    },

    getFakeData: function () {
        var fakeDataArray = [];
        for (var i = 0; i < 24; i++) {
            fakeDataArray.push(Math.floor((Math.random() * 100)));
        }
        return fakeDataArray;
    }
});