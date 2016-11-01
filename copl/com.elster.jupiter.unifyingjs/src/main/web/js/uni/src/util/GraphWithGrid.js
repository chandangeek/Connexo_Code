/**
 * @class Uni.util.GraphWithGrid
 *
 * This class contains common functions to synchronize Highstock graph and ExtJs grid interactions
 */
Ext.define('Uni.util.GraphWithGrid', {

    onBarSelect: function (point, func, view) {
        var tableView = view.output ? view.down('#output-readings-preview-container') : view,
            graphView = view.down('highstockFixGraphView'),
            grid = tableView.down('grid'),
            index = grid.getStore().findExact('interval_end', new Date(point.intervalEnd)),
            viewEl = grid.getView().getEl(),
            currentScrollTop = viewEl.getScroll().top,
            viewHeight = viewEl.getHeight(),
            rowOffsetTop = index * 29,
            newScrollTop;

        if (index > -1) {
            if (!(rowOffsetTop > currentScrollTop && rowOffsetTop < currentScrollTop + viewHeight)) {
                newScrollTop = rowOffsetTop - viewHeight / 25;
                if (newScrollTop > 0) {
                    grid.getView().getEl().setScrollTop(newScrollTop);
                } else {
                    grid.getView().getEl().setScrollTop(0);
                }
            }

            tableView.suspendEvent('rowselect');
            grid.getSelectionModel().select(index);
            tableView.resumeEvent('rowselect');
            this.setSelectionColor(graphView, point);
        }
    },

    onRowSelect: function (record, func, view) {
        var me = this,
            index = view.down('grid').getStore().indexOf(record),
            graphView = view.down('highstockFixGraphView'),
            selectPoint = function () {
                var data = graphView.chart.series[0].data,
                    intervalEnd = record.get('interval_end').getTime(),
                    xAxis = graphView.chart.xAxis[0],
                    currentExtremes = xAxis.getExtremes(),
                    range = currentExtremes.max - currentExtremes.min,
                    point = data[data.length - index - 1];

                if (intervalEnd + range / 2 > currentExtremes.dataMax) {
                    xAxis.setExtremes(currentExtremes.dataMax - range, currentExtremes.dataMax);
                } else if (intervalEnd - range / 2 < currentExtremes.dataMin) {
                    xAxis.setExtremes(currentExtremes.dataMin, currentExtremes.dataMin + range);
                } else if (!(intervalEnd > currentExtremes.min && intervalEnd < currentExtremes.max)) {
                    xAxis.setExtremes(intervalEnd - range / 2, intervalEnd + range / 2);
                }
                me.setSelectionColor(graphView, point);
            };
        
        if (index > -1) {
            if (graphView.chart) {
                selectPoint();
            } else if (graphView.rendered) {
                view.on('graphrendered', selectPoint, view, {singelton: true});
            }
        }
    },

    setSelectionColor: function (graphView, point) {
        graphView.suspendEvent('barselect');
        point.select(true, false);
        graphView.resumeEvent('barselect');        
        graphView.chart.series[0].update({
            states: {
                select: {
                    color: point.pointAttr.hover.fill
                }
            }
        });
    },

    showGraphView: function (view, data) {        
        var record = view.channel || view.output,
            graphView = view.down('highstockFixGraphView'),
            dataStore = view.output ? graphView.store : view.store,
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
            seriesObject['data'] = data.data;
            seriesObject['turboThreshold'] = Number.MAX_VALUE;
            if (view.output) {
                seriesObject['pointInterval'] = intervalLengthInMs;
            }
            series.push(seriesObject);
            graphView.down('#graphContainer').show();
            this.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels, data.missedValues, graphView);
        } else {
            graphView.down('#graphContainer').hide();
        }
        view.updateLayout();
        view.fireEvent('graphrendered');
        Ext.resumeLayouts(true);
    },

    drawGraph: function (yAxis, series, intervalLength, channelName, unitOfMeasure, zoomLevels, missedValues, graphView) {
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