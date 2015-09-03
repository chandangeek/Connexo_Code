Ext.define('Imt.channeldata.view.ChannelGraph', {
    extend: 'Imt.view.GraphView',
    alias: 'widget.channel-graph',
    itemId: 'usagePointChannelGraphView',

    requires: [
        'Imt.view.GraphView'
    ],
    items: [
        {
            xtype: 'container',
            itemId: 'graphContainer',
            style: {
                width: '100%'
            }
        },
        {
            xtype: 'no-items-found-panel',
            itemId: 'ctr-graph-no-data',
            hidden: true,
            title: Uni.I18n.translate('deviceloadprofiles.data.empty.title', 'IMT', 'No readings found'),
            reasons: [
                Uni.I18n.translate('deviceloadprofiles.data.empty.list.item1', 'IMT', 'No readings have been defined yet.') ]
        }
    ],
    drawGraph: function (yAxis, series, intervalLength, channelName, unitOfMeasure, zoomLevels) {
        var me = this;

        me.chart = new Highcharts.StockChart({

            title: {
                text: channelName
            },

            chart: {
                height: 600,
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
                positioner: function (labelWidth, labelHeight, point){
                    var xValue,
                        yValue;

                    xValue = point.plotX + labelWidth < this.chart.chartWidth ? point.plotX : point.plotX - (labelWidth*2)/3;
                    yValue = point.plotY > labelHeight ? point.plotY: labelHeight;
                    return {x: xValue, y: yValue}
                },
                formatter: function () {
                    var s = '<b>' + Highcharts.dateFormat('%A, %e %B %Y', this.x) + '</b>';
                    if (intervalLength < 86400000) {
                        s += '<br/>Interval ' + Highcharts.dateFormat('%H:%M', this.x);
                        s += ' - ' + Highcharts.dateFormat('%H:%M', this.x + intervalLength) + '<br>';
                    } else {
                        s += '<b>' + ' - ' + Highcharts.dateFormat('%A, %e %B %Y', this.x + intervalLength) + '</b>' + '<br>';
                    }
                    s += '<table style="margin-top: 10px"><tbody>';
                        s += '<tr>'
                        s += '<td style="padding-right: 10px; text-align: right"><b>' + channelName + '</b></td>';
                        s += '<td style="padding-right: 1px; text-align: right">' + this.points[0].y + '</td>';
                        s += '<td style="padding-left: 1px; text-align: left">' + unitOfMeasure + '</td>';
                        s += '</tr>'
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
                column: {
//                    borderColor: 'black',
//                    borderWidth: 0.5,
                    pointPadding: 0,
                    groupPadding: 0,
                    dataGrouping:
                    {
                        enabled: false
                    },
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
});