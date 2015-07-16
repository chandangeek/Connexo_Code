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
                html: Ext.String.htmlEncode(me.wTitle)
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
                html: Ext.String.htmlEncode(me.emptyMsg)
            },
            {
                xtype: 'container',
                flex: 1,
                hidden: true,
                itemId: 'chart',
                height: 400,
                listeners: {
                    resize: {
                        fn: function (container, width, height) {
                            if (me.chart) {
                                me.chart.setSize(width, height, false);
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

        Ext.suspendLayouts();
        if (!filter.get('deviceGroup')) {
            me.hide();
        } else {
            me.show();
            var container = me.down('#chart');
            var empty = me.down('#empty');
            if (record == null) {
                empty.show();
            } else {

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
        }
        Ext.resumeLayouts(true);
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
                            xValue,
                            additionalY;

                        if (point.plotY < 0) {
                            additionalY = 0;
                        } else {
                            additionalY = point.plotY;
                        }

                        if (labelWidth > me.getEl().getWidth() - point.plotX) {
                            xValue = point.plotX - labelWidth/4;
                        } else {
                            xValue = point.plotX;
                        }

                        yValue = point.plotY > labelHeight ? point.plotY - labelHeight: additionalY + labelHeight/2;
                        return {x: xValue, y: yValue}
                    },
                    formatter: function (tooltip) {
                        if (!this.y && this.y !== 0) {
                            return false;
                        } else {
                            return tooltip.defaultFormatter.apply(this, arguments);
                        }
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
                    tickPositioner: function () {
                        if (!this.dataMax && this.dataMax !== 0 && !this.dataMin && this.dataMin !== 0) {
                            return [0,10,20,30,40,50,60,70,80,90,100];
                        }
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