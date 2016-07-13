Ext.define('Mdc.usagepointmanagement.view.ChannelDataGraph', {
    extend: 'Uni.view.highstock.GraphView',
    alias: 'widget.channel-data-graph',
    channel: null,
    zoomLevels: null,

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

        switch ('volume'/*me.channel.get('flowUnit')*/) {
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
            collectedUnitOfMeasure = me.channel.get('readingType').names.unitOfMeasure,
        //calculatedUnitOfMeasure = me.channel.get('calculatedReadingType') ? me.channel.get('calculatedReadingType').names.unitOfMeasure : collectedUnitOfMeasure,
            okColor = "#70BB51",
            estimatedColor = "#568343",
            suspectColor = 'rgba(235, 86, 66, 1)',
            informativeColor = "#dedc49",
            notValidatedColor = "#71adc7",
            tooltipOkColor = 'rgba(255, 255, 255, 0.85)',
            tooltipSuspectColor = 'rgba(235, 86, 66, 0.3)',
            tooltipEstimatedColor = 'rgba(86, 131, 67, 0.3)',
            tooltipInformativeColor = 'rgba(222, 220, 73, 0.3)',
            tooltipNotValidatedColor = 'rgba(0, 131, 200, 0.3)';

        me.store.each(function (record) {
            var point = {},
                interval = record.get('interval');
            //mainValidationInfo = record.get('mainValidationInfo'),
            //bulkValidationInfo = record.get('bulkValidationInfo'),
            //properties = record.get('readingProperties');

            point.x = interval.start;
            point.id = point.x;
            point.y = parseFloat(record.get('value')) || null;
            point.intervalEnd = interval.end;
            //point.collectedValue = record.get('collectedValue');
            point.collectedUnitOfMeasure = collectedUnitOfMeasure;
            //point.calculatedUnitOfMeasure = calculatedUnitOfMeasure;
            point.color = okColor;
            point.tooltipColor = tooltipOkColor;
            //point.multiplier = record.get('multiplier');

            //if (mainValidationInfo.valueModificationFlag == 'EDITED') {
            //    point.edited = true;
            //}
            //if (mainValidationInfo.estimatedByRule) {
            //    point.color = estimatedColor;
            //    point.tooltipColor = tooltipEstimatedColor;
            //} else if (properties.delta.notValidated) {
            //    point.color = notValidatedColor;
            //    point.tooltipColor = tooltipNotValidatedColor
            //} else if (properties.delta.suspect) {
            //    point.color = suspectColor;
            //    point.tooltipColor = tooltipSuspectColor
            //} else if (properties.delta.informative) {
            //    point.color = informativeColor;
            //    point.tooltipColor = tooltipInformativeColor;
            //}
            //
            //if (bulkValidationInfo.valueModificationFlag == 'EDITED') {
            //    point.bulkEdited = true;
            //}

            //Ext.merge(point, properties);
            data.push(point);


            //!point.y && (point.y = null);
            //if (!point.y) {
            //if (properties.delta.suspect) {
            //    missedValues.push({
            //        id: record.get('interval').start,
            //        from: record.get('interval').start,
            //        to: record.get('interval').end,
            //        color: 'rgba(235, 86, 66, 0.3)'
            //    });
            //    record.set('plotBand', true);
            //}
            //}
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
                        deltaIcon,
                        bulkIcon,
                        bgColor,
                        iconSpan = '<span class="{icon}" ' + 'style="height: 16px; ' + 'width: 16px; ' +
                            'display: inline-block; ' + 'vertical-align: top; ' + 'margin-left: 4px"></span>',
                        editedIconSpan = '<span class="uni-icon-edit"' + 'style="height: 13px; ' + 'width: 13px; ' +
                            'display: inline-block; ' + 'vertical-align: top; ' + 'margin-left: 4px"></span>',
                        calculatedValue,
                        value;

                    if (point.delta && point.delta.suspect) {
                        deltaIcon = 'icon-validation-red';
                    } else if (point.delta && point.delta.notValidated) {
                        deltaIcon = 'icon-validation-black';
                    }

                    if (point.bulk && point.bulk.suspect) {
                        bulkIcon = 'icon-validation-red';
                    } else if (point.bulk && point.bulk.notValidated) {
                        bulkIcon = 'icon-validation-black';
                    }

                    value = point.y ? point.y + ' ' + point.collectedUnitOfMeasure : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
                    html += '<br/>' + Uni.I18n.translate('devicechannels.interval', 'MDC', 'Interval') + ' ' + Highcharts.dateFormat('%H:%M', point.x);
                    html += ' - ' + Highcharts.dateFormat('%H:%M', point.intervalEnd) + '<br>';
                    html += '<table style="margin-top: 10px"><tbody>';
                    bgColor = point.tooltipColor;
                    html += '<tr><td><b>' + Uni.I18n.translate('general.value', 'MDC', 'Value') + ':</b></td><td>' + value + (point.edited ? editedIconSpan : '') + iconSpan.replace('{icon}', deltaIcon) + '</td></tr>';
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