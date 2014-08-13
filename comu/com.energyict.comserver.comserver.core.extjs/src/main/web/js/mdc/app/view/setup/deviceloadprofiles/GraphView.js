Ext.define('Mdc.view.setup.deviceloadprofiles.GraphView', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceLoadProfilesGraphView',
    itemId: 'deviceLoadProfilesGraphView',
    loadProfileRecord: null,
    graphTitle: null,
    yAxis: null,
    series: null,
    channels: null,
    intervalLength: null,

    drawGraph: function () {
        var me = this;
        new Highcharts.StockChart({

            title: {
                text: me.graphTitle
            },

            chart: {
                height: 600,
                renderTo: me.el.dom
            },

            credits: {
                enabled: false
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

            yAxis: me.yAxis,

            navigation: {
                buttonOptions: {
                    enabled: false
                }
            },

            navigator: {
                xAxis: {
                    type: 'datetime',
                    dateTimeLabelFormats: {
                        second: '%H:%M<br/>%a %e %b',
                        minute: '%H:%M<br/>%a %e %b',
                        hour: '%H:%M<br/>%a %e %b',
                        day: '%a %e %b',
                        week: '%a %e<br/>%b %Y',
                        month: '%b %Y',
                        year: '%Y'
                    }
                }
            },

            rangeSelector: {
                selected: 0,
                inputEnabled: true,
                buttons: [
                    {
                        type: 'hour',
                        count: 4,
                        text: '4h'
                    },
                    {
                        type: 'day',
                        count: 1,
                        text: '1d'
                    },
                    {
                        type: 'day',
                        count: 2,
                        text: '2d'
                    },
                    {
                        type: 'week',
                        count: 1,
                        text: '1w'
                    }
                ]
            },

            tooltip: {
                useHTML: true,
                formatter: function () {
                    var s = '<b>' + Highcharts.dateFormat('%A, %e %B %Y', this.x) + '</b>';
                    s += '<br/>Interval ' + Highcharts.dateFormat('%H:%M', this.x);
                    s += ' - ' + Highcharts.dateFormat('%H:%M', this.x + me.intervalLength) + '<br>';
                    s += '<table style="margin-top: 10px"><tbody>';
                    $.each(this.points, function (i, points) {
                        s += '<tr>'
                        s += '<td style="padding-right: 10px; text-align: right"><b>' + me.channels[i].name + '</b></td>';
                        s += '<td style="padding-right: 1px; text-align: right">' + points.y + '</td>';
                        s += '<td style="padding-left: 1px; text-align: left">' + me.channels[i].unitOfMeasure + '</td>';
                        s += '</tr>'
                    });
                    s += '</tbody></table>';

                    return s;
                }
            },

            legend: {
                enabled: true
            },

            plotOptions: {
                series: {
                    events: {
                        hide: function (event) {

                            var chart = this.chart,
                                index = this.index,
                                legend = $('.highcharts-legend')[0],
                                visible_count = 0;

                            $.each(chart.series, function (i, serie) {
                                if ((serie.visible) && (serie.index != index) && (serie.name != 'Navigator')) {
                                    visible_count += 1;
                                }
                            });

                            if (visible_count < 1) {
                                $('.highcharts-grid').hide();
                                $('.highcharts-input-group').hide();
                                $('.highcharts-axis').hide();
                                $('.highcharts-navigator').hide();
                                $('.highcharts-axis-labels').hide();
                                $('.highcharts-series-group').hide();
                                $('.highcharts-navigator-handle-right').hide();
                                $('.highcharts-navigator-handle-left').hide();
                                $('.highcharts-scrollbar').hide();
                                $('.highcharts-button').hide();
                                $('.highcharts-background').hide();
                                $('.highcharts-tooltip').hide();
                                $( "text:contains('Zoom')").hide();
                            }
                        },
                        show: function (event) {
                            $('.highcharts-grid').show();
                            $('.highcharts-input-group').show();
                            $('.highcharts-axis').show();
                            $('.highcharts-navigator').show();
                            $('.highcharts-axis-labels').show();
                            $('.highcharts-series-group').show();
                            $('.highcharts-navigator-handle-right').show();
                            $('.highcharts-navigator-handle-left').show();
                            $('.highcharts-scrollbar').show();
                            $('.highcharts-button').show();
                            $('.highcharts-background').show();
                            $('.highcharts-tooltip').show();
                            $('text').show();
                        }
                    }
                },
                column: {
                    borderColor: 'black',
                    borderWidth: 0.5,
                    pointPadding: 0,
                    groupPadding: 0.1
                }
            },

            series: me.series
        });

    },

    drawEmptyList: function () {
        this.removeAll(true);
        this.add(
            {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('deviceloadprofiles.data.empty.title', 'MDC', 'No readings found'),
                reasons: [
                    Uni.I18n.translate('deviceloadprofiles.data.empty.list.item1', 'MDC', 'No readings have been defined yet.') ]
            });

    },

    setParams: function (title, yAxis, series, channels, intervalLength) {
        this.graphTitle = title;
        this.yAxis = yAxis;
        this.series = series;
        this.channels = channels;
        this.intervalLength = intervalLength;
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});