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
                    scope: this,
                    afterrender: function (container) {
                        var me = this;
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
                                        data: me.getFakeData()
                                    },
                                    {
                                        name: 'Pending',
                                        dashStyle: 'longdash',
                                        data: me.getFakeData()
                                    },
                                    {
                                        name: 'Waiting',
                                        data: me.getFakeData()
                                    },
                                    {
                                        name: 'Failed',
                                        data: me.getFakeData()
                                    },
                                    {
                                        name: 'On hold',
                                        data: me.getFakeData()
                                    }
                                ]
                            });
                        }, 100);
                    }
                }
            }
        ];
        this.callParent(arguments);
    },
    getFakeData: function () {
        var fakeDataArray = [];
        for (var i = 0; i < 24; i++) {
            fakeDataArray.push(Math.floor((Math.random() * 100)));
        }
        return fakeDataArray;
    }
});