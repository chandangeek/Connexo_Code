Ext.define('Dsh.view.widget.ReadOutsOverTime', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.read-outs-over-time',
    itemId: 'read-outs-over-time',
    wTitle: Uni.I18n.translate('overview.widget.readOutsOverTime.title', 'DSH', 'Read-outs over time'),
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
                height: 400,
                style: {
                    marginTop: '-130px',
                    marginBottom: '180px'
                },
                listeners: {
                    afterrender: function (container) {
                        Ext.defer(function () {
                            new Highcharts.Chart({
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
                                    series: {
                                        animation: false,
                                        pointInterval: 3600 * 1000
                                    },
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
                                    },
                                    tickInterval: 3600 * 1000
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
                                },
                                series: [
                                    {
                                        name: 'Target',
                                        data: [90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 88, 86, 84, 82, 80, 78, 76, 74, 72]
                                    },
                                    {
                                        name: 'Pending',
                                        dashStyle: 'longdash',
                                        data: [19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19]
                                    },
                                    {
                                        name: 'Waiting',
                                        data: [28, 44, 39, 35, 24, 27, 32, 41, 37, 34, 32, 30, 28, 27, 25, 23, 18, 17, 14, 8, 18, 17, 14, 8]
                                    },
                                    {
                                        name: 'Failed',
                                        data: [71, 70, 65, 57, 53, 44, 30, 84, 27, 15, 2, 6, 10, 8, 7, 12, 8, 7, 14, 31, 8, 7, 14, 31]
                                    },
                                    {
                                        name: 'On hold',
                                        data: [42, 34, 36, 38, 44, 48, 51, 56, 12, 72, 4, 64, 64, 64, 68, 62, 61, 60, 50, 41, 61, 60, 50, 41]
                                    }
                                ]
                            });
                        }, 100);
                    }
                }
            }
        ];
        this.callParent(arguments);
    }
});