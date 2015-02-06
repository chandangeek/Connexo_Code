Ext.define('Dsh.view.widget.ReadOutsOverTime', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.read-outs-over-time',
    itemId: 'read-outs-over-time',
    hidden: true,
    layout: 'fit',
    yLabel: '',

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
                flex: 1,
                hidden: true,
                itemId: 'chart',
                height: 400,
                listeners: {
                    resize: {
                        fn: function(el) {
                            me.chart.setSize(Ext.getBody().getViewSize().width-100, 400);
                            me.doLayout();
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
                    var series = kpi.getData(),
                        timezoneOffset = new Date(record.get('time')[0]).getTimezoneOffset(),
                        timeArray = _.map(record.get('time'), function (item) {
                            return item - timezoneOffset * 60000;
                        });
                    series.color = me.colorMap[idx];
                    series.data = _.zip(timeArray, series.data);
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
                    reflow: false,
                    width: Ext.getBody().getViewSize().width-100,
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
                    positioner: function (labelWidth, labelHeight, point){
                        var yValue,
                            additionalY;

                        if (point.plotY < 0) {
                            additionalY = 0;
                        } else {
                            additionalY = point.plotY;
                        }

                        yValue = point.plotY > labelHeight ? point.plotY - labelHeight: additionalY + labelHeight/2;
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
                    lineWidth: 2,
                    type: 'datetime',
                    dateTimeLabelFormats: {
                        day: '%H:%M'
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