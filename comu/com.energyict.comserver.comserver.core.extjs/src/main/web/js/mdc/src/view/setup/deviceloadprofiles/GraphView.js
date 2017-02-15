/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceloadprofiles.GraphView', {
    extend: 'Uni.view.highstock.GraphView',
    alias: 'widget.deviceLoadProfilesGraphView',
    itemId: 'deviceLoadProfilesGraphView',

    requires: [
        'Uni.view.highstock.GraphView',
        'Uni.util.FormEmptyMessage'
    ],

    items: [
        {
            xtype: 'container',
            itemId: 'graphContainer'
        },
        {
            xtype: 'form',
            items: {
                xtype: 'uni-form-empty-message',
                hidden: true,
                itemId: 'emptyGraphMessage',
                text: Uni.I18n.translate('deviceloadprofiles.data.empty', 'MDC', 'No readings have been defined yet.')
            }
        }
    ],


    drawGraph: function (title, yAxis, series, channels, seriesToYAxisMap, intervalLength, zoomLevels, showDeviceQualityIcon) {
        var me = this;
        me.chart = new Highcharts.StockChart({

            title: {
                text: title
            },

            chart: {
                height: 320 + 150 * yAxis.length,
                renderTo: me.down('#graphContainer').el.dom
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
                positioner: function (labelWidth, labelHeight, point) {
                    var xValue,
                        yValue;

                    xValue = point.plotX + labelWidth < this.chart.chartWidth ? point.plotX : point.plotX - (labelWidth * 4) / 5;
                    yValue = point.plotY > labelHeight ? point.plotY : labelHeight;
                    return {x: xValue, y: yValue}
                },
                formatter: function () {
                    var s = '<b>' + Highcharts.dateFormat('%A, %e %B %Y', this.x) + '</b>';
                    if (intervalLength < 86400000) {
                        s += '<br/>' + Uni.I18n.translate('general.interval', 'MDC', 'Interval') + ' ' + Highcharts.dateFormat('%H:%M', this.x);
                        s += ' - ' + Highcharts.dateFormat('%H:%M', this.x + intervalLength);
                    } else {
                        s += '<b> - ' + Highcharts.dateFormat('%A, %e %B %Y', this.x + intervalLength) + '</b>';
                    }
                    if (showDeviceQualityIcon[this.x]) {
                        s += '<span class="icon-price-tags" style="margin-left:4px; display:inline-block; vertical-align:top;"></span>';
                    }
                    s += '<br>';
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
                },
                followPointer: true,
                followTouchMove: true
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
                    pointPadding: 0,
                    dataGrouping: {
                        enabled: false
                    },
                    groupPadding: 0,
                    color: '#70BB51',
                    shadow: false,
                    pointPlacement: 'between'

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
        step = (100 / lineCount | 0) - 1;

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

        chart.setSize(chart.chartWidth, 320 + 150 * visibleYAxises.length);
    }
});