Ext.define('Mdc.view.setup.deviceloadprofiles.GraphView', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceLoadProfilesGraphView',
    itemId: 'deviceLoadProfilesGraphView',


    drawGraph: function (title, yAxis, series, channels, seriesToYAxisMap, intervalLength, zoomLevels) {
        var me = this;

        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        new Highcharts.StockChart({

            title: {
                text: title
            },

            chart: {
                height: 320 + 150 * yAxis.length ,
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

            yAxis: yAxis,

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
                buttons: zoomLevels
            },

            tooltip: {
                useHTML: true,
                formatter: function () {
                    var s = '<b>' + Highcharts.dateFormat('%A, %e %B %Y', this.x) + '</b>';
                    if (intervalLength < 86400000) {
                        s += '<br/>Interval ' + Highcharts.dateFormat('%H:%M', this.x);
                        s += ' - ' + Highcharts.dateFormat('%H:%M', this.x + intervalLength) + '<br>';
                    } else {
                        s += '<b>' + ' - ' + Highcharts.dateFormat('%A, %e %B %Y', this.x + intervalLength) + '</b>' + '<br>';
                    }
                    s += '<table style="margin-top: 10px"><tbody>';
                    $.each(this.points, function (i, points) {
                        var series = points.point.series;
                        s += '<tr>'
                        s += '<td style="padding-right: 10px; text-align: right"><b>' + channels[series.index].name + '</b></td>';
                        s += '<td style="padding-right: 1px; text-align: right">' + points.y + '</td>';
                        s += '<td style="padding-left: 1px; text-align: left">' + channels[series.index].unitOfMeasure + '</td>';
                        s += '</tr>'
                    });
                    s += '</tbody></table>';
                    return s;
                }
            },

            legend: {
                enabled: false
            },

            plotOptions: {
                series: {
                    events: {
                        showEmpty: false,
                        hide: function (event) {
                            var chart = this.chart,
                                index = this.index,
                                visibleYAxises = [],
                                yAxis;

                            $.each(chart.series, function (i, serie) {
                                if ((serie.visible) && (serie.index != index)) {
                                    yAxis = seriesToYAxisMap[serie.index];
                                    if (!isNaN(yAxis) && !Ext.Array.contains(visibleYAxises, yAxis)) {
                                        visibleYAxises.push(yAxis);
                                    }
                                }
                            });
                            me.redrawChart(chart, visibleYAxises);
                        },
                        show: function (event) {
                            var chart = this.chart,
                                index = this.index,
                                visibleYAxises = [],
                            yAxis;

                            $.each(chart.series, function (i, serie) {
                                if ((serie.visible) || (serie.index == index)) {
                                    yAxis = seriesToYAxisMap[serie.index];
                                    if (!isNaN(yAxis) && !Ext.Array.contains(visibleYAxises, yAxis)) {
                                        visibleYAxises.push(yAxis);
                                    }
                                }
                            });
                            me.redrawChart(chart, visibleYAxises);
                        }
                    }
                },
                column: {
                    borderColor: 'black',
                    borderWidth: 0.5,
                    pointPadding: 0,
                    groupPadding: 0,
                    color: '#70BB51'
                },
                line: {
                    color: '#70BB51'
                }
            },

            series: series
        });

    },

    redrawChart: function (chart, visibleYAxises) {
        var step,
            lineCount,
            currentAxisTopValue = 0;

        visibleYAxises.sort();

        lineCount = visibleYAxises.length;
        step = (100 / lineCount | 0) -1;

        Ext.Array.each(chart.yAxis, function (yAxis, index) {
            var yAxisObjectUpdate = {};
            if (Ext.Array.contains(visibleYAxises, index)) {
                if (index == visibleYAxises[lineCount - 1]) {
                    yAxisObjectUpdate['height'] = (100 - currentAxisTopValue) + '%';
                } else {
                    yAxisObjectUpdate['height'] = step + '%';
                }
                yAxisObjectUpdate['top'] = currentAxisTopValue + '%';
                currentAxisTopValue += step + 2;
                yAxis.update(yAxisObjectUpdate);
            }

        });

        chart.setSize(chart.chartWidth, 320 + 150 * visibleYAxises.length );
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

    initComponent: function () {
        this.callParent(arguments);
    }
});