/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.view.RegisteredDevicesGraph', {
    extend: 'Ext.container.Container',
    alias: 'widget.registered-devices-graph',

    chart: undefined,

    items: [
        {
            xtype: 'container',
            itemId: 'mdc-registered-devices-graphContainer',
            style: {
                width: '100%'
            },
            listeners: {
                resize: function(container) {
                    var chart = container.up('registered-devices-graph').chart;
                    if (chart) {
                        chart.reflow();
                    }
                }
            }
        }
    ],

    totalColor: '#00755E',
    targetColor: '#2887C8',
    columnColor: '#33CC99',
    columnBorderColor: '#FFFFFF',
    gridLineColor: '#D2D2D2',
    labelColor: '#686868',
    data: [],
    frequency: undefined, // as a string
    period: undefined,

    initComponent: function () {
        this.callParent(arguments);
        this.down('#mdc-registered-devices-graphContainer').on('boxready', this.drawGraph, this);
    },

    drawGraph: function() {
        var me = this;

        me.chart = new Highcharts.StockChart({

            chart: {
                type: 'column',
                height: 750,
                renderTo: me.down('#mdc-registered-devices-graphContainer').el.dom
            },
            title: {
                text: null
            },
            credits: {
                enabled: false
            },

            xAxis: {
                labels: {
                    style: {
                        color: me.labelColor,
                        fontWeight: 'normal',
                        fontSize: '13px',
                        fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                    },
                    align: 'center',
                    useHTML: true,
                    step: 1,
                    formatter: function() {
                        var date = new Date(this.value);
                        return '<table><tr><td align="center">'+Uni.DateTime.formatTimeShort(date)+'</td></tr>'
                                + '<tr><td align="center">'+Uni.DateTime.formatDateShort(date)+'</td></tr></table>';
                    }
                },
                type: 'datetime',
                lineWidth: 1,
                tickWidth: 1,
                tickLength: 5
            },

            yAxis: {
                title: {
                    text: 'Number of registered devices'
                },
                opposite: false, // show the axis on the left
                gridLineColor: me.gridLineColor,
                labels: {
                    style: {
                        color: me.labelColor,
                        fontWeight: 'normal',
                        fontSize: '13px',
                        fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                    }
                }
            },

            navigation: {
                buttonOptions: {
                    enabled: false
                }
            },

            navigator: {
                xAxis: {
                    type: 'datetime',
                    labels: {
                        align: 'center',
                        useHTML: true,
                        step: 1,
                        formatter: function() {
                            var date = new Date(this.value);
                            return '<table><tr><td align="center">'+Uni.DateTime.formatTimeShort(date)+'</td></tr>'
                                + '<tr><td align="center">'+Uni.DateTime.formatDateShort(date)+'</td></tr></table>';
                        }
                    }
                }
            },

            rangeSelector: {
                enabled: false
                // buttons: [{
                //     type: 'day',
                //     count: 1,
                //     text: '1d'
                // }, {
                //     type: 'all',
                //     text: 'All'
                // }]
            },

            exporting: {
                enabled: false
            },

            tooltip: {
                shape: 'square',
                followPointer: true,
                headerFormat: '',
                useHTML: true,
                shared: false,
                style: {
                    color: me.labelColor,
                    fontSize: '13px',
                    fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                },
                borderWidth: 0,
                formatter: function () {
                    var html = '';
                    html = Uni.DateTime.formatDateTime(new Date(this.x), Uni.DateTime.LONG, Uni.DateTime.SHORT);
                    html += '</br>';
                    if (this.series.name === 'registeredDevices') {
                        html += Uni.I18n.translate('general.xRegisteredDevices', 'MDC', '{0} registered devices', this.y);
                    } else if (this.series.name === Uni.I18n.translate('general.totalAmountOfDevices', 'MDC', 'Total amount of devices')) {
                        html += Uni.I18n.translate('general.totalAmountOfDevices', 'MDC', 'Total amount of devices') + ': ' + this.y;
                    } else if (this.series.name === Uni.I18n.translate('general.target', 'MDC', 'Target')) {
                        html += Uni.I18n.translate('general.target', 'MDC', 'Target') + ': ' + this.y;
                    }
                    return html;
                }
            },

            legend: {
                enabled: false
            },

            plotOptions: {
                column: {
                    color: me.columnColor,
                    stacking: 'normal',
                    pointPadding: 0,
                    borderWidth: 1,
                    borderColor: me.columnBorderColor,
                    groupPadding: 0,
                    shadow: false,
                    // animation: false
                    // pointStart: Date.UTC(2017, 8, 1, 10), // = 12:00 GMT
                    // pointInterval: 3600 * 250, // 15 min
                    // pointRange: 3600 * 250 // make the columns as broad as the interval
                },
                // line: {
                //     pointStart: Date.UTC(2017, 8, 1, 10), // = 12:00 GMT
                //     pointInterval: 3600 * 250 // 15 min
                // },
                series: {
                    showInNavigator: true
                }
            },

            series: me.createDataSeries(me.data)
        });
    },

    createDataSeries: function(records) {
        if (Ext.isEmpty(records)) {
            return;
        }
        
        var me = this,
            seriesRegistered = {},
            seriesTotal = {},
            seriesTarget = {},
            series = [];

        seriesRegistered['name'] = 'registeredDevices';
        seriesRegistered['data'] = [];

        seriesTotal['name'] = Uni.I18n.translate('general.totalAmountOfDevices', 'MDC', 'Total amount of devices');
        seriesTotal['color'] = this.totalColor;
        seriesTotal['type'] = 'line';
        seriesTotal['data'] = [];

        seriesTarget['name'] = Uni.I18n.translate('general.target', 'MDC', 'Target');
        seriesTarget['color'] = this.targetColor;
        seriesTarget['type'] = 'line';
        seriesTarget['data'] = [];

        // Convert the records into graph data
        var start = undefined;
        if (records.length > 0) {
            if (me.frequency === '12h' && records.length < 12) {
                start = moment(records[records.length - 1].get('timestamp')).subtract(7, 'days').valueOf();
            } else if (me.frequency === '1d' && records.length < 30) {
                start = moment(records[records.length - 1].get('timestamp')).subtract(30, 'days').valueOf();
            } else {
                start = moment(records[records.length - 1].get('timestamp')).subtract(1, 'days').valueOf();
            }
            if (me.period) {
                start = parseInt(me.period.split('-')[0]);
            }
        }

        var frequencyInMillis = 0;
        switch (this.frequency) {
            case '15m':
                frequencyInMillis = 15*60*1000;
                break;
            case '4h':
                frequencyInMillis = 4*3600*1000;
                break;
            case '12h':
                frequencyInMillis = 12*3600*1000;
                break;
            case '1d':
                frequencyInMillis = 24*3600*1000;
                break;
        }

        Ext.Array.forEach(records, function(record) {
            var timestamp = record.get('timestamp');
            if (timestamp != start) {
                while (start < timestamp) {
                    seriesRegistered['data'].push([start, 0]);
                    start += frequencyInMillis;
                }
                if (start === timestamp) {
                    seriesRegistered['data'].push([timestamp, record.get('registered')]);
                    seriesTotal['data'].push([timestamp, record.get('total')]);
                    seriesTarget['data'].push([timestamp, record.get('target')]);
                    start += frequencyInMillis;
                }
            } else {
                seriesRegistered['data'].push([timestamp, record.get('registered')]);
                seriesTotal['data'].push([timestamp, record.get('total')]);
                seriesTarget['data'].push([timestamp, record.get('target')]);
                start += frequencyInMillis;
            }
        });

        series.push(seriesRegistered);
        series.push(seriesTotal);
        series.push(seriesTarget);
        return series;
    }
});
