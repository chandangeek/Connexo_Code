Ext.define('Mtr.view.dashboard.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.dashboardBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',

    requires: [
        'Mtr.widget.PortalPanel',
        'Mtr.store.mock.Browsers',
        'Chart.ux.Highcharts',
        'Chart.ux.Highcharts.PieSerie',
        'Chart.ux.Highcharts.LineSerie',
        'Chart.ux.Highcharts.SplineSerie',
        'Chart.ux.Highcharts.ColumnRangeSerie'
    ],

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Dashboard</h1>'
                },
                {
                    xtype: 'container', // TODO Change this to 'portalpanel' when there are no more rendering issues.
                    defaultType: 'portalcolumn',
                    defaults: {
                        columnWidth: 1 / 2,
                        defaultType: 'portlet',
                        margin: 5,
                        layout: 'fit'
                    },
                    layout: {
                        type: 'column'
                    },
                    items: [
                        {
                            itemId: 'col-1',
                            items: [
                                {
                                    title: 'Pie',
                                    items: [
                                        {
                                            xtype: 'highchart',
                                            itemId: 'piechart',
                                            height: 300,
                                            initAnimAfterLoad: false,

                                            series: [
                                                {
                                                    categorieField: 'vendor',
                                                    dataField: 'usage',
                                                    name: 'Browsers',
                                                    size: '60%',
                                                    totalDataField: true,
                                                    colorField: 'color',
                                                    dataLabels: {
                                                        formatter: function () {
                                                            return this.y > 5 ? this.point.name : null;
                                                        },
                                                        color: 'white',
                                                        distance: -30
                                                    }
                                                },
                                                {
                                                    categorieField: 'version',
                                                    dataField: 'usage',
                                                    colorField: 'color',
                                                    name: 'Versions',
                                                    innerSize: '60%',
                                                    dataLabels: {
                                                        formatter: function () {
                                                            return this.y > 1 ? '<b>' + this.point.name + ':</b> ' + this.y + '%' : null;
                                                        }
                                                    }
                                                }
                                            ],

                                            chartConfig: {
                                                chart: {
                                                    type: 'pie'
                                                },
                                                plotOptions: {
                                                    pie: {
                                                        shadow: false,
                                                        center: ['50%', '50%']
                                                    }
                                                },
                                                title: {
                                                    text: 'Highcharts (' + Highcharts.version + ') Donut Pie for ExtJs ' + Ext.versions.core.version
                                                },
                                                subtitle: {
                                                    text: 'Browser market share, April, 2011'
                                                },
                                                tooltip: {
                                                    formatter: function () {
                                                        return '<b>' + this.point.name + '</b>: ' + this.y + ' %';
                                                    }
                                                },
                                                credits: {
                                                    enabled: false
                                                }
                                            }
                                        }
                                    ]
                                },
                                {
                                    title: 'Grid',
                                    html: '<h2>Grid</h2>'
                                },
                                {
                                    title: 'Line',
                                    items: [
                                        {
                                            xtype: 'highchart',
                                            itemId: 'linechart',
                                            height: 300,
                                            initAnimAfterLoad: false,

                                            series: [
                                                {
                                                    dataIndex: 'yesterday',
                                                    name: 'Yesterday'
                                                },
                                                {
                                                    dataIndex: 'today',
                                                    name: 'Today'
                                                }
                                            ],

                                            chartConfig: {
                                                chart: {
                                                    type: 'line',
                                                    step: 'center'
                                                },
                                                title: {
                                                    text: 'Data with null gaps'
                                                },
                                                credits: {
                                                    enabled: false
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            itemId: 'col-2',
                            items: [
                                {
                                    title: 'Text',
                                    html: '<p style="margin: 6px;">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam tellus sapien, ' +
                                        'vulputate et interdum a, scelerisque id libero. Suspendisse libero diam, cursus vitae ' +
                                        'orci ac, <i>aliquam consequat odio</i>. Nulla arcu dui, adipiscing quis quam id, volutpat ' +
                                        'aliquam tellus. Suspendisse nec consequat enim. <b>Aliquam euismod</b> sit amet mi id semper. ' +
                                        'In turpis eros, condimentum quis lectus pulvinar, molestie luctus nibh. Morbi ' +
                                        'ut rhoncus tortor. Vestibulum ac varius purus.Donec accumsan lacus nec ' +
                                        'pharetra malesuada.</p>'
                                },
                                {
                                    title: 'Spline',
                                    items: [
                                        {
                                            xtype: 'highchart',
                                            itemId: 'splinechart',
                                            height: 300,
                                            xField: 'time',
                                            initAnimAfterLoad: false,

                                            series: [
                                                {
                                                    dataIndex: 'yesterday',
                                                    name: 'Yesterday'
                                                },
                                                {
                                                    dataIndex: 'today',
                                                    name: 'Today'
                                                }
                                            ],

                                            chartConfig: {
                                                chart: {
                                                    type: 'spline'
                                                },
                                                title: {
                                                    text: 'Temperature over time'
                                                },
                                                xAxis: [
                                                    {
                                                        title: {
                                                            text: 'Time',
                                                            margin: 30
                                                        },
                                                        labels: {
                                                            rotation: 270,
                                                            y: 35,
                                                            formatter: function () {
                                                                var dt = Ext.Date.parse(parseInt(this.value) / 1000, "U");
                                                                if (dt) {
                                                                    return Ext.Date.format(dt, "H:i");
                                                                }
                                                                return this.value;
                                                            }

                                                        }
                                                    }
                                                ],
                                                yAxis: {
                                                    title: {
                                                        text: 'Temperature'
                                                    },
                                                    plotLines: [
                                                        {
                                                            value: 0,
                                                            width: 1,
                                                            color: '#808080'
                                                        }
                                                    ]
                                                },
                                                tooltip: {
                                                    formatter: function () {
                                                        var dt = Ext.Date.parse(parseInt(this.x) / 1000, "U");
                                                        return 'At <b>' + this.series.name + '</b> ' + Ext.Date.format(dt, "H:i") + ',<br/>temperature is: ' + this.y;
                                                    }

                                                },
                                                legend: {
                                                    layout: 'vertical',
                                                    align: 'right',
                                                    verticalAlign: 'top',
                                                    x: -10,
                                                    y: 100,
                                                    borderWidth: 0
                                                },
                                                credits: {
                                                    enabled: false
                                                }
                                            }
                                        }
                                    ]
                                },
                                {
                                    title: 'Column',
                                    items: [
                                        {
                                            xtype: 'highchart',
                                            itemId: 'columnchart',
                                            height: 300,
                                            xField: 'date',

                                            series: [
                                                {
                                                    name: 'Open & Close',
                                                    dataIndex: [ 'open', 'close' ]
                                                },
                                                {
                                                    name: 'High & Low',
                                                    minDataIndex: 'low',
                                                    maxDataIndex: 'high'
                                                }
                                            ],

                                            chartConfig: {
                                                chart: {
                                                    type: 'columnrange'
                                                },
                                                title: {
                                                    text: 'Stock price 2012 - Apple (AAPL)'
                                                },
                                                subtitle: {
                                                    text: 'Open & Close and High & Low ranges'
                                                },
                                                xAxis: {
                                                    title: {
                                                        text: 'Date',
                                                        align: 'high'
                                                    },
                                                    labels: {
                                                        rotation: -45,
                                                        align: 'center',
                                                        y: 40,
                                                        x: -20
                                                    }
                                                },
                                                yAxis: {
                                                    title: {
                                                        text: 'Price ($)'
                                                    }
                                                },
                                                credits: {
                                                    enabled: false
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
})
;