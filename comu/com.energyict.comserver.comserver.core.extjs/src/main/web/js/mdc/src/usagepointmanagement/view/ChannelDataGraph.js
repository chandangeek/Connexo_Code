Ext.define('Mdc.usagepointmanagement.view.ChannelDataGraph', {
    extend: 'Uni.view.highstock.GraphView',
    alias: 'widget.channel-data-graph',
    channel: null,
    zoomLevels: null,

    listeners: {
        resize: {
            fn: function (graphView, width, height) {
                if (this.chart) {
                    this.chart.setSize(width, height, false);
                }
            }
        }
    },

    items: [
        {
            xtype: 'container',
            itemId: 'graphContainer',
            style: {
                width: '100%'
            }
        }
    ],

    showGraphView: function () {
        var me = this,
            dataStore = me.store,
            container = me.down('#graphContainer'),
            zoomLevelsStore = Ext.getStore('Uni.store.DataIntervalAndZoomLevels'),
            readingType = me.channel.get('readingType'),
            channelName = readingType && readingType.fullAliasName ? readingType.fullAliasName : '',
            unitOfMeasure = readingType.names ? readingType.names.unitOfMeasure : readingType.unit,
            seriesObject = {
                marker: {
                    enabled: false
                },
                name: channelName
            },
            yAxis = {
                opposite: false,
                gridLineDashStyle: 'Dot',
                showEmpty: false,
                title: {
                    rotation: 270,
                    text: unitOfMeasure
                }
            },
            series = [],
            interval = me.interval,
            intervalLengthInMs;

        seriesObject['data'] = [];

        intervalLengthInMs = zoomLevelsStore.getIntervalInMs(me.channel.get('interval'));

        switch (me.channel.get('flowUnit')) {
            case 'flow':
                seriesObject['type'] = 'line';
                seriesObject['step'] = false;
                break;
            case 'volume':
                seriesObject['type'] = 'column';
                seriesObject['step'] = true;
                break;
        }

        Ext.suspendLayouts();
        if (dataStore.getTotalCount() > 0) {
            var data = me.formatData();
            seriesObject['data'] = data.data;
            seriesObject['turboThreshold'] = Number.MAX_VALUE;
            //seriesObject['pointInterval'] = intervalLengthInMs;

            series.push(seriesObject);
            container.show();
            me.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, me.zoomLevels, data.missedValues);
        } else {
            container.hide();
        }
        me.updateLayout();
        Ext.resumeLayouts(true);
    },

    formatData: function () {
        var me = this,
            data = [],
            missedValues = [],
            unit = me.channel.get('readingType').names.unitOfMeasure;
        validationMap = {
            NOT_VALIDATED: {
                barColor: 'rgba(113,173,199,1)',
                tooltipColor: 'rgba(0,131,200,0.3)',
                icon: '<span class="icon-flag6"></span>'
            },
            SUSPECT: {
                barColor: 'rgba(235,86,66,1)',
                tooltipColor: 'rgba(235,86,66,0.3)',
                icon: '<span class="icon-flag5" style="color:red"></span>'
            },
            INFORMATIVE: {
                barColor: 'rgba(222,220,73,1)',
                tooltipColor: 'rgba(222,220,73,0.3)',
                icon: '<span class="icon-flag5" style="color:yellow"></span>'
            },
            OK: {
                barColor: 'rgba(112,187,81,1)',
                tooltipColor: 'rgba(255,255,255,0.85)',
                icon: ''
            },
            NO_LINKED_DEVICES: {
                barColor: null,
                tooltipColor: null,
                icon: ''
            }
        };

        me.store.each(function (record) {
            var point = {},
                interval = record.get('interval'),
                validation = record.get('validation');

            point.x = interval.start;
            point.id = point.x;
            point.y = parseFloat(record.get('value')) || null;
            point.intervalEnd = interval.end;
            point.color = validationMap[validation].barColor;
            point.tooltipColor = validationMap[validation].tooltipColor;
            point.icon = validationMap[validation].icon;
            point.unit = unit;
            //point.multiplier = record.get('multiplier');

            data.unshift(point);
            !point.y && (point.y = null);
            if (!point.y) {
                if (validation === 'SUSPECT' || validation === 'NO_LINKED_DEVICES') {
                    missedValues.push({
                        id: interval.start,
                        from: interval.start,
                        to: interval.end,
                        color: validation === 'SUSPECT' ? 'rgba(235, 86, 66, 0.3)' : 'rgba(210,210,210,1)'
                    });
                    record.set('plotBand', true);
                }
            }
        });

        return {data: data, missedValues: missedValues};
    },

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
                        bgColor,
                        editedIconSpan = '<span class="uni-icon-edit"' + 'style="height: 13px; ' + 'width: 13px; ' +
                            'display: inline-block; ' + 'vertical-align: top; ' + 'margin-left: 4px"></span>',
                        value;

                    value = !Ext.isEmpty(point.y) ? point.y + ' ' + point.unit : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
                    html += '<br/>' + Uni.I18n.translate('devicechannels.interval', 'MDC', 'Interval') + ' ' + Highcharts.dateFormat('%H:%M', point.x);
                    html += ' - ' + Highcharts.dateFormat('%H:%M', point.intervalEnd) + '<br>';
                    html += '<table style="margin-top: 10px"><tbody>';
                    bgColor = point.tooltipColor;
                    html += '<tr><td><b>' + Uni.I18n.translate('general.value', 'MDC', 'Value') + ':</b></td><td>' + value + (point.edited ? editedIconSpan : '') + ' ' + point.icon + '</td></tr>';
                    if (point.multiplier) {
                        html += '<tr><td><b>' + Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier') + ':</b></td><td>' + point.multiplier + '</td></tr>';
                    }
                    html += '</tbody></table>';
                    html = '<div style="background-color: ' + bgColor + '; padding: 8px">' + html + '</div>';
                    return html;
                },
                followPointer: false,
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