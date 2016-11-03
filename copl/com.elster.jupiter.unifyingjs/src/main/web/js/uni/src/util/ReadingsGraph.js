/**
 * @class Uni.util.ReadingsGraph
 *
 * This class contains common functions to render Highstock graph with readings
 */
Ext.define('Uni.util.ReadingsGraph', {

    showGraphView: function () {
        var me = this,
            record = me.channel || me.output,
            graphView = me.down('highstockFixGraphView'),
            dataStore = me.store,
            zoomLevelsStore = Ext.getStore('Uni.store.DataIntervalAndZoomLevels'),
            readingType = record.get('calculatedReadingType') || record.get('readingType'),
            channelName = readingType && readingType.fullAliasName ? readingType.fullAliasName : '',
            unitOfMeasure = record.get('calculatedReadingType') ? record.get('readingType').unit : (readingType.names ? readingType.names.unitOfMeasure : readingType.unit),
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
            intervalRecord,
            zoomLevels,
            data,
            intervalLengthInMs;

        seriesObject['data'] = [];

        intervalRecord = zoomLevelsStore.getIntervalRecord(record.get('interval'));
        intervalLengthInMs = zoomLevelsStore.getIntervalInMs(record.get('interval'));
        zoomLevels = intervalRecord.get('zoomLevels');

        switch (record.get('flowUnit')) {
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
            data = me.formatData(),
            seriesObject['data'] = data.data;
            seriesObject['turboThreshold'] = Number.MAX_VALUE;
            if (me.output) {
                seriesObject['pointInterval'] = intervalLengthInMs;
            }
            series.push(seriesObject);
            graphView.down('#graphContainer').show();
            me.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels, data.missedValues);
        } else {
            graphView.down('#graphContainer').hide();
        }
        me.updateLayout();
        me.fireEvent('graphrendered');
        Ext.resumeLayouts(true);
    },

    drawGraph: function (yAxis, series, intervalLength, channelName, unitOfMeasure, zoomLevels, missedValues) {
        var me = this,
            graphView = me.down('highstockFixGraphView');

        graphView.chart = new Highcharts.StockChart({
            title: {
                text: channelName
            },

            chart: {
                height: 600,
                renderTo: graphView.down('#graphContainer').el.dom
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
                plotBands: missedValues,
                events: {
                    afterSetExtremes: function (e) {
                        if (typeof(e.rangeSelectorButton) !== 'undefined') {
                            var grid = Ext.ComponentQuery.query('grid')[0],
                                record = grid.getSelectionModel().getLastSelected(),
                                intervalEnd = record.get('interval_end').getTime(),
                                xAxis = graphView.chart.xAxis[0],
                                range = e.max - e.min;

                            if (!(intervalEnd > e.min && intervalEnd < e.max)) {
                                xAxis.setExtremes(intervalEnd - range / 2, intervalEnd + range / 2);
                            }
                        }
                    }
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
                formatter: function () {
                    return graphView.createTooltip(this); // should be implemented in your Uni.view.highstock.GraphView
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
                    shadow: false,
                    pointPlacement: 'between'
                },
                series: {
                    cropThreshold: Number.MAX_VALUE,
                    cursor: 'pointer',
                    allowPointSelect: true,                    
                    events: {
                        click: function (event) {
                            graphView.fireEvent('barselect', event.point);
                        }
                    }
                }
            },

            series: series
        });
    }

});