Ext.define('Mdc.view.setup.devicechannels.GraphView', {
    extend: 'Mdc.view.setup.highstock.GraphView',

    alias: 'widget.deviceLoadProfileChannelGraphView',
    itemId: 'deviceLoadProfileChannelGraphView',

    requires: [
        'Mdc.view.setup.highstock.GraphView'
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
            title: Uni.I18n.translate('deviceloadprofiles.data.empty.title', 'MDC', 'No readings found'),
            reasons: [
                Uni.I18n.translate('deviceloadprofiles.data.empty.list.item1', 'MDC', 'No readings have been defined yet.') ]
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
                style: {
                    color: '#333333',
                    fontSize: '12px',
                    padding: '0px'
                },
                useHTML: true,
                positioner: function (labelWidth, labelHeight, point) {
                    var xValue,
                        yValue;

                    xValue = point.plotX + labelWidth < this.chart.chartWidth ? point.plotX : point.plotX - (labelWidth * 2) / 3;
                    yValue = point.plotY > labelHeight ? point.plotY : labelHeight;
                    return {x: xValue, y: yValue}
                },
                formatter: function (tooltip) {
                    var tvalColor = 'rgba(255, 255, 255, 0.85)';
                    var testColor = 'rgba(86, 131, 67, 0.3)';
                    var tsusColor = 'rgba(235, 86, 66, 0.3)';
                    var tinfColor = 'rgba(222, 220, 73, 0.3)';
                    var tedColor = 'rgba(255, 255, 255, 0.85)';
                    var tnvalColor = 'rgba(0, 131, 200, 0.3)';
                    var s = '<b>' + Highcharts.dateFormat('%A, %e %B %Y', this.x) + '</b>';
                    var point = this.points[0].point,
                        bgColor = tvalColor,
                        vicon = '',
                        bicon = '',
                        iconFlag = '',
                        iconConfirm = '';

                    s += '<br/>Interval ' + Highcharts.dateFormat('%H:%M', this.x);
                    s += ' - ' + Highcharts.dateFormat('%H:%M', this.x + 900000) + '<br>';
                    s += '<table style="margin-top: 10px"><tbody>';

                    switch (point.validationResult) {
                        case 'suspect' :
                            bgColor = tsusColor;
                            vicon = iconFlag;
                            break;
                        case 'ok' :
                            bgColor = tvalColor;
                            break;
                        case 'informative' :
                            bgColor = tinfColor;
                            break;
                        case 'notValidated' :
                            bgColor = tnvalColor;
                            break;
                        case 'confirmed'  :
                            bgColor = tvalColor;
                            bicon = iconConfirm;
                            break;
                        case 'estimated'  :
                            bgColor = testColor;
                            break;
                    }
                    switch (point.modificationFlag) {
                        case 'EDITED.saved' :
                            break;
                        case 'EDITED.notSaved':
                            break;
                    }


                    s += '<tr><td style="padding-right: 10px; text-align: right"><b>' +
                        point.series.name +
                        ':</b></td><td>' +
                        point.y +
                        ' kWh ' + vicon + '</td></tr>';
                    s += '<tr><td style="padding-right: 10px; text-align: right"><b>Bulk value:</b></td><td>1000 kWh ' +
                        bicon +
                        '</td></tr>';

                    s += '</tbody></table>';
                    s = '<div style="background-color: ' +
                        bgColor +
                        '; padding: 8px">' + s + '</div>';
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
                    dataGrouping: {
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

    }
});