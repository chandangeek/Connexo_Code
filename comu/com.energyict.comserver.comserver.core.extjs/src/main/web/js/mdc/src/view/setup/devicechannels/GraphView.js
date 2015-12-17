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
        }
    ],

    drawGraph: function (yAxis, series, intervalLength, channelName, unitOfMeasure, zoomLevels, missedValues) {
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
                },
                plotBands: missedValues
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
                    var html = '<b>' + Highcharts.dateFormat('%A, %e %B %Y', this.x),
                        point = this.points[0].point,
                        deltaIcon,
                        bulkIcon,
                        bgColor,
                        iconSpan = '<span class="{icon}" ' + 'style="height: 16px; ' + 'width: 16px; ' +
                            'display: inline-block; ' + 'vertical-align: top; ' + 'margin-left: 4px"></span>',
                        editedIconSpan = '<span class="uni-icon-edit"' + 'style="height: 13px; ' + 'width: 13px; ' +
                            'display: inline-block; ' + 'vertical-align: top; ' + 'margin-left: 4px"></span>',
                        calculatedValue,
                        collectedValue;

                    if (point.delta && point.delta.suspect) {
                        deltaIcon = 'icon-validation-red';
                    } else if (point.delta && point.delta.notValidated) {
                        deltaIcon = 'icon-validation-black';
                    }

                    if (point.bulk && point.bulk.suspect)  {
                        bulkIcon = 'icon-validation-red';
                    } else if (point.bulk && point.bulk.notValidated) {
                        bulkIcon = 'icon-validation-black';
                    }

                    if (point.collectedValue) {
                        calculatedValue = point.y ? point.y + ' ' + point.calculatedUnitOfMeasure : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
                        collectedValue = point.collectedValue ? point.collectedValue + ' ' + point.collectedUnitOfMeasure : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
                    } else {
                        // If there's a value (point.y) but no point.collectedValue, then we should call the value "Collected value" (and there's no "Calculated value")
                        collectedValue = point.y ? point.y + ' ' + point.collectedUnitOfMeasure : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
                        calculatedValue = null;
                    }
                    html += '<br/>' + Uni.I18n.translate('devicechannels.interval', 'MDC', 'Interval') + ' ' + Highcharts.dateFormat('%H:%M', point.x);
                    html += ' - ' + Highcharts.dateFormat('%H:%M', point.intervalEnd) + '<br>';
                    html += '<table style="margin-top: 10px"><tbody>';
                    bgColor = point.tooltipColor;
                    if (calculatedValue) {
                        html += '<tr><td><b>' + Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated value') + ':</b></td><td>' + calculatedValue + (point.edited ? editedIconSpan : '') + iconSpan.replace('{icon}', deltaIcon) + '</td></tr>';
                        html += '<tr><td><b>' + Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value') + ':</b></td><td>' + collectedValue + (point.bulkEdited ? editedIconSpan : '') + iconSpan.replace('{icon}', bulkIcon) + '</td></tr>';
                    } else {
                        html += '<tr><td><b>' + Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value') + ':</b></td><td>' + collectedValue + (point.edited ? editedIconSpan : '') + iconSpan.replace('{icon}', deltaIcon) + '</td></tr>';
                    }
                    if (point.multiplier) {
                        html += '<tr><td><b>' + Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier') + ':</b></td><td>' + point.multiplier + '</td></tr>';
                    }
                    html += '</tbody></table>';
                    html = '<div style="background-color: ' + bgColor + '; padding: 8px">' + html + '</div>';
                    return html;
                },
                followPointer: true,
                followTouchMove: false
            },

            legend: {
                enabled: false
            },

            plotOptions: {
                column: {
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
                },
                series: {
                    cropThreshold: Number.MAX_VALUE
                }
            },

            series: series
        });

    }
});