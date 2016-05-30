Ext.define('Imt.purpose.view.ReadingsGraph', {
    extend: 'Uni.view.highstock.GraphView',
    alias: 'widget.readings-graph',
    itemId: 'readings-graph',

    store: 'Imt.purpose.store.Readings',

    requires: [
        'Uni.view.highstock.GraphView'
    ],

    mixins: {
        bindable: 'Ext.util.Bindable'
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

    initComponent: function() {
        var me = this;

        me.callParent(arguments);
        me.bindStore(me.store || 'ext-empty-store', true);
        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            load: this.onLoad
        };
    },

    onBeforeLoad: function () {
        this.setLoading(true);
    },

    onLoad: function () {
        this.showGraphView();
        this.setLoading(false);
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    showGraphView: function () {
        var me = this,
            dataStore = me.store,
            output = me.output,
            container = me.down('#graphContainer'),
            zoomLevelsStore = Ext.getStore('Uni.store.DataIntervalAndZoomLevels'),
            readingType = output.get('readingType'),
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
            zoomLevels,
            intervalLengthInMs;

        seriesObject['data'] = [];

        intervalLengthInMs = zoomLevelsStore.getIntervalInMs(output.get('interval'));
        zoomLevels = interval.get('zoomLevels');

        switch (output.get('flowUnit')) {
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
            me.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels, data.missedValues);
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
            output = me.output,
            collectedUnitOfMeasure = output.get('readingType').names.unitOfMeasure,
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