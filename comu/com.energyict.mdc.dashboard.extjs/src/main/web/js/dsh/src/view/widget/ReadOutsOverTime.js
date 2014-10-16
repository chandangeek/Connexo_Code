Ext.define('Dsh.view.widget.ReadOutsOverTime', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.read-outs-over-time',
    itemId: 'read-outs-over-time',
    hidden: true,
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
                itemId: 'chart',
                height: 400,
                style: {
                    marginTop: '-130px',
                    marginBottom: '180px'
                }
            }
        ];

        this.callParent(arguments);
    },

    bindStore: function (store) {
        var me = this;
        var filter = me.router.filter;

        if (!filter.get('deviceGroup')) {
            me.hide();
        } else {
            me.show();
            me.renderChart(me.down('#chart'));
            // clean up
            me.chart.series.map(function (obj) {
                obj.remove()
            });
            store.each(function (kpi) {
                me.chart.addSeries(kpi.getData())
            });
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
                    gridLineWidth: 0,
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