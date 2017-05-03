/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.PurposeDataView', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.purpose-data-view',
    itemId: 'purpose-data-view',
    requires: [
        'Uni.grid.FilterPanelTop',
        'Imt.purpose.view.summary.PurposeDataGrid',
        'Uni.view.highstock.GraphView',
        'Imt.purpose.view.summary.TopNavigationToolbar'
    ],
    store: 'Imt.purpose.store.PurposeSummaryData',
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    idProperty: 'interval_end',
    filterStores: [
        'Imt.purpose.store.IntervalFilter',
        'Imt.purpose.store.UnitFilter'
    ],


    onGraphResize: function (graphView, width, height) {
        if (graphView.chart) {
            graphView.chart.setSize(width, height, false);
        }
    },

    initComponent: function () {
        var me = this,
            output = me.output,
            emptyComponent,
            durations,
            zoomLevelsStore = Ext.getStore('Uni.store.DataIntervalAndZoomLevels'),
            all,
            intervalLengthInMs = 0,
            duration;


        emptyComponent = {
            xtype: 'no-readings-found-panel',
            itemId: 'readings-empty-panel'
        };


        me.interval = zoomLevelsStore.getIntervalRecord(Ext.getStore('Imt.purpose.store.IntervalFilter').getAt(0).getData());

        if (me.interval) {
            durations = Ext.create('Uni.store.Durations');
            all = me.interval.get('all');
            duration = {
                //Last reading?
                defaultFromDate: me.interval.getIntervalStart(new Date()),
                defaultDuration: all.count + all.timeUnit,
                durationStore: durations
            };
            durations.loadData(me.interval.get('duration'));
        } else {
            duration = {
                defaultFromDate: moment().startOf('day').subtract(1, 'years').toDate(),
                defaultDuration: '1years'
            };
        }

        me.items = [
            {
                xtype: 'purpose-top-navigation-toolbar',
                disabled: true,
                // itemId: 'tabbed-device-channels-view-previous-next-navigation-toolbar',
                store: 'Imt.purpose.store.FilteredOutputs',
                // router: me.router,
                // routerIdArgument: 'id',
                // itemsName: me.prevNextListLink,
                // indexLocation: 'arguments',
                // isFullTotalCount: true
            },
            {
                xtype: 'uni-grid-filterpaneltop',
                itemId: 'output-readings-topfilter',
                store: me.store,
                hasDefaultFilters: true,
                filters: [
                    Ext.apply({
                        type: 'duration',
                        dataIndex: 'interval',
                        dataIndexFrom: 'intervalStart',
                        dataIndexTo: 'intervalEnd',
                        text: Uni.I18n.translate('general.startDate', 'IMT', 'Start date'),
                        loadStore: false,
                        itemId: 'output-readings-topfilter-duration'
                    }, duration),
                    {
                        type: 'combobox',
                        dataIndex: 'timeInterval',
                        itemId: 'purpose-data-topfilter-interval',
                        store: 'Imt.purpose.store.IntervalFilter',
                        emptyText: Uni.I18n.translate('general.interval', 'IMT', 'Interval'),
                        valueField: 'id',
                        displayField: 'name',
                        editable: false,
                        forceSelection: true,
                        loadStore: false,
                        defaults: {margin: '0 10 0 0'},
                        listeners: {
                            beforerender: function (fld) {
                                var defaultValue = fld.getStore().getAt(0);
                                fld.setValue(defaultValue);
                                fld.defaultValue = defaultValue.get('id');
                            },
                            change: function (fld, newValue, oldValue, eOpts) {
                                if (!newValue) {
                                    fld.setValue(fld.defaultValue);
                                }
                            }
                        }
                    },
                    {
                        type: 'combobox',
                        dataIndex: 'unit',
                        itemId: 'purpose-data-topfilter-unit',
                        store: 'Imt.purpose.store.UnitFilter',
                        emptyText: Uni.I18n.translate('general.unit', 'IMT', 'Unit'),
                        valueField: 'id',
                        displayField: 'name',
                        editable: false,
                        forceSelection: true,
                        loadStore: false,
                        defaults: {margin: '0 10 0 0'},
                    },
                    {
                        type: 'checkbox',
                        dataIndex: 'bulk',
                        itemId: 'purpose-data-topfilter-include-bulk-values',
                        layout: 'hbox',
                        defaults: {margin: '0 10 0 0'},
                        options: [
                            {
                                display: Uni.I18n.translate('purpose.summary.includeBulkValues', 'IMT', 'Include bulk values'),
                                value: true,
                                itemId: 'purpose-data-topfilter-bulk'
                            }
                        ]
                    }
                ]
            }
        ];

        me.items.push(
            {
                xtype: 'highstockFixGraphView',
                router: me.router,
                output: me.output,
                interval: me.interval,
                listeners: {
                    resize: me.onGraphResize
                },
                items: [
                    {
                        xtype: 'container',
                        itemId: 'graphContainer',
                        style: {
                            width: '100%'
                        }
                    }
                ]
            }
        );

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

    onBeforeLoad: function (store, operation, eOpts) {
        this.outputs.storeFilter = operation.params.filter;
        this.setLoading(true);
    },

    onLoad: function (store, records, successful, eOpts) {
        var me = this;
        me.graphTitle = Uni.I18n.translate('purpose.summary.intervalData', 'IMT', '{0} data', me.down('#purpose-data-topfilter-interval').getRawValue());
        me.outputs.load({
            callback: function (records, operation, success) {
                me.showGraphView();
                me.redrawGrid();
                me.setLoading(false);
            }
        });

    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    redrawGrid: function(){
        var me = this,
            grid = me.down('purpose-data-grid');
        me.remove(grid);
        me.add({
            xtype: 'purpose-data-grid',
            outputs: me.outputs
        })
    },

    showGraphView: function () {
        var me = this,
            graphView = me.down('highstockFixGraphView'),
            dataStore = me.store,
            seriesObject = {},
            yAxis= [],
            series = [],
            intervalRecord,
            measurementTypeOrder = [],
            channelDataArrays = {},
            seriesToYAxisMap = {},
            currentAxisTopValue = 2,
            currentLine = 0,
            channels = [],
            zoomLevels,
            data,
            axisBacklash,
            lineCount,
            step,
            intervalLengthInMs;

        seriesObject['data'] = [];

        me.outputs.each( function (channel, index) {

            var seriesObject = {marker: {
                enabled: false
            }};
            seriesObject['name'] = channel.get('name');
            channelDataArrays[channel.get('id')] = [];
            seriesObject['data'] = channelDataArrays[channel.get('id')];
            switch (channel.get('flowUnit')) {
                case 'flow':
                    seriesObject['type'] = 'line';
                    seriesObject['step'] = false;
                    break;
                case 'volume':
                    seriesObject['type'] = 'column';
                    seriesObject['step'] = true;
                    break;
            }
            measurementTypeOrder.push(channel.name);
            seriesObject['yAxis'] = currentLine;
            currentLine += 1;
            var channelName = Ext.String.format('{0} ({1})', channel.get('name'), channel.get('readingType').names.unitOfMeasure);
            channels.push(
                {
                    id: channel.get('id'),
                    name: channelName,
                    unitOfMeasure: channel.get('readingType').names.unitOfMeasure
                }
            );
            seriesToYAxisMap[index] = seriesObject['yAxis'];
            series.push(seriesObject);
        });

        lineCount = measurementTypeOrder.length;
        step = (100 / lineCount | 0) - 1;
        axisBacklash = (4 - lineCount) > 0 ? (4 - lineCount) : 0;

        Ext.Array.each(channels, function (channel, index) {
            var yAxisObject = {
                    opposite: false,
                    gridLineDashStyle: 'Dot',
                    showEmpty: false,
                    labels: {
                        style: {
                            color: '#686868',
                            fontWeight: 'normal',
                            fontSize: '14px',
                            fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                        }
                    }
                },
                yAxisTitle = channel.name;


            if (index == 0) {
                yAxisObject['height'] = step + '%';
            } else {
                if (index == lineCount - 1) {
                    yAxisObject['height'] = (100 - currentAxisTopValue) + '%';
                } else {
                    yAxisObject['height'] = step + '%';
                }
                yAxisObject['offset'] = 0;
            }
            yAxisObject['top'] = currentAxisTopValue + '%';
            currentAxisTopValue += step + 2 + axisBacklash;
            yAxisObject['title'] = {
                rotation: 0,
                align: 'high',
                margin: -6 * yAxisTitle.length,
                text: yAxisTitle,
                style: {
                    color: '#686868',
                    fontWeight: 'normal',
                    fontSize: '14px',
                    fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                }
            };
            yAxis.push(yAxisObject);
        });

        intervalLengthInMs = me.interval.get('intervalInMs');
        intervalRecord = me.interval;
        zoomLevels = intervalRecord.get('zoomLevels');

        Ext.suspendLayouts();
        if (dataStore.getTotalCount() > 0) {
            dataStore.each(function (record) {
                if (record.get('channelData')) {
                    Ext.iterate(record.get('channelData'), function (key, value) {
                        if (channelDataArrays[key]) {
                            if (value) {
                                channelDataArrays[key].unshift([record.get('interval').start, parseFloat(value)]);
                            } else {
                                channelDataArrays[key].unshift([record.get('interval').start, null]);
                            }
                        }
                    });
                }
            });
            graphView.down('#graphContainer').show();
            me.drawGraph(me.graphTitle, yAxis, series, channels, seriesToYAxisMap, intervalLengthInMs, zoomLevels);
        } else {
            graphView.down('#graphContainer').hide();
        }
        me.updateLayout();
        me.fireEvent('graphrendered');
        Ext.resumeLayouts(true);
    },

    drawGraph: function (title, yAxis, series, channels, seriesToYAxisMap, intervalLength, zoomLevels) {
        var me = this,
            graphView = me.down('highstockFixGraphView');
        graphView.chart = new Highcharts.StockChart({

            title: {
                text: title,
                style: {
                    color: '#74af74',
                    fontWeight: 'normal',
                    fontSize: '25px',
                    fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                }
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
                },
                labels: {
                    style: {
                        color: '#686868',
                        fontWeight: 'normal',
                        fontSize: '13px',
                        fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
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
                    },
                    labels: {
                        style: {
                            color: '#686868',
                            fontWeight: 'normal',
                            fontSize: '14px',
                            fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                        }
                    }
                }
            },

            rangeSelector: {
                selected: 0,
                inputEnabled: true,
                buttons: zoomLevels,
                labelStyle: {
                    color: '#686868',
                    fontWeight: 'normal',
                    fontSize: '14px',
                    fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                },
                inputStyle: {
                    color: '#686868',
                    fontWeight: 'normal',
                    fontSize: '14px',
                    fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                },
                buttonTheme: {
                    style: {
                        color: '#686868',
                        fontWeight: 'normal',
                        fontSize: '14px',
                        fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                    }
                }
            },

            tooltip: {
                useHTML: true,
                style: {
                    color: '#686868',
                    fontWeight: 'normal',
                    fontSize: '14px',
                    fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                },
                positioner: function (labelWidth, labelHeight, point) {
                    var xValue,
                        yValue;

                    xValue = point.plotX + labelWidth < this.chart.chartWidth ? point.plotX : point.plotX - (labelWidth * 4) / 5;
                    yValue = point.plotY > labelHeight ? point.plotY : labelHeight;
                    return {x: xValue, y: yValue}
                },
                formatter: function () {
                    var s = '<b style=" color: #74af74; font-size: 14px; fontFamily: Lato, Helvetica, Arial, Verdana, Sans-serif;">' + Highcharts.dateFormat('%A, %e %B %Y', this.x) ;
                    if (intervalLength < 86400000) {
                        s += '<br/>' + Uni.I18n.translate('general.interval', 'IMT', 'Interval') + ' ' + Highcharts.dateFormat('%H:%M', this.x);
                        s += ' - ' + Highcharts.dateFormat('%H:%M', this.x + intervalLength)+ '</b>';
                    } else {
                        s += ' - ' + Highcharts.dateFormat('%A, %e %B %Y', this.x + intervalLength) + '</b>';
                    }
                    s += '<br>';
                    s += '<table style="margin-top: 10px"><tbody>';
                    $.each(this.points, function (i, points) {
                        var series = points.point.series;
                        s += '<tr>'
                        s += '<td style="padding-right: 10px; text-align: left"><b>' + channels[series.index].name + '</b></td>';
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
    }
});