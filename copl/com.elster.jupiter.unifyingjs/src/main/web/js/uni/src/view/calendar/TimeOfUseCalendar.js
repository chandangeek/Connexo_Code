/**
 * @class 'Uni.view.calendar.TimeOfUseCalendar'
 */
Ext.define('Uni.view.calendar.TimeOfUseCalendar', {
    extend: 'Ext.container.Container',
    alias: 'widget.timeOfUseCalendar',
    ui: 'timeOfUseCalendar',
    record: null,

    models: [
        'Uni.model.timeofuse.Calendar'
    ],

    items: [
        {
            xtype: 'container',
            itemId: 'graphContainer',
            style: {
                width: '90%'
            }
        }
    ],

    colors: ['#1E7D9E', '#70BB51', '#EB5642', '#686868', '#71ADC7', '#E6FFE3', '#A0A0A0', '#FFEBE3'],

    initComponent: function () {

        this.callParent(arguments);

        var hsSeriesTranslate = Highcharts.Series.prototype.translate;

        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        Highcharts.Series.prototype.translate = function () {

            hsSeriesTranslate.apply(this, arguments);
            var series = this,
                pointPlacement = series.options.pointPlacement,
                dynamicallyPlaced = pointPlacement === 'between' || Ext.isNumber(pointPlacement);

            if (dynamicallyPlaced) {
                var xAxis = series.xAxis,
                    points = series.points,
                    dataLength = points.length,
                    i;

                for (i = 0; i < dataLength; i += 1) {
                    var point = points[i],
                        xValue = point.x;

                    point.clientX = xAxis.translate(xValue, 0, 0, 0, 1, pointPlacement);
                }
            }
        };

        this.down('#graphContainer').on('afterrender', this.drawGraph, this);
    },

    drawGraph: function () {
        var me = this;
        me.chart = new Highcharts.Chart({

            chart: {
                type: 'column',
                height: 800,
                renderTo: me.down('#graphContainer').el.dom
            },
            title: {
                text: me.record.get('name'),
                align: 'left',
                style: {
                    color: '#1E7D9E',
                    fontWeight: 'bold',
                    fontSize: '24',
                    fontFamily: 'Open sans condensed, Lato, Helvetica, Arial, Verdana, Sans-serif'
                }
            },
            credits: {
                enabled: false
            },

            xAxis: {
                opposite: true,
                categories: me.getCategories(me.record),
                labels: {
                    style: {
                        color: '#686868',
                        fontWeight: 'normal',
                        fontSize: '13px',
                        fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                    }
                },
                lineWidth: 0,
                tickWidth: 0
            },

            yAxis: {
                title: {
                    text: ''
                },
                categories: me.createHourCategories(),
                alternateGridColor: '#F0F0F0',
                min: 0,
                max: 24,
                reversed: true,
                tickInterval: 2,
                gridLineColor: '#D2D2D2',
                labels: {
                    style: {
                        color: '#686868',
                        fontWeight: 'normal',
                        fontSize: '13px',
                        fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                    }
                }
            },

            exporting: {
                enabled: false
            },

            tooltip: {
                shape: 'square',
                followPointer: true,
                headerFormat: '',
                useHTML: true,
                style: {
                    color: '#686868',
                    fontSize: '13px',
                    fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                },
                borderWidth: 0,
                formatter: function () {
                    var fromDate = new Date(),
                        toDate = new Date(),
                        range = this.series.options.range,
                        html;

                    fromDate.setHours(range.from.hour);
                    fromDate.setMinutes(range.from.minute);
                    toDate.setHours(range.to.hour);
                    toDate.setMinutes(range.to.minute);

                    html = '<table><tbody>';
                    html += '<tr><td><b>' + Uni.I18n.translate('general.from', 'UNI', 'From') + ':</b></td><td>' + Uni.DateTime.formatTimeShort(fromDate) + '</td></tr>';
                    html += '<tr><td><b>' + Uni.I18n.translate('general.to', 'UNI', 'To') + ':</b></td><td>' + Uni.DateTime.formatTimeShort(toDate) + '</td></tr>';
                    html += '</tbody></table>';
                    return html;
                }

            },

            legend: {
                enabled: false
            },

            plotOptions: {
                column: {
                    stacking: 'normal',
                    pointPadding: 0.05,
                    borderWidth: 0,
                    groupPadding: 0,
                    shadow: false,
                    dataLabels: {
                        align: 'center',
                        verticalAlign: 'middle',
                        enabled: true,
                        color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white',
                        style: {
                            textShadow: false,
                            opacity: 0.7,
                            fontWeight: 'normal',
                            fontSize: '16px',
                            fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                        },

                        formatter: function () {
                            return this.series.options.label;
                        }
                    }
                }
            },

            series: me.createWeekSeries(me.record)
        });

    },

    createHourCategories: function () {
        var categories = [],
            d;
        for (var i = 0; i <= 24; i++) {
            d = new Date();
            d.setHours(i);
            d.setMinutes(0);
            categories.push(Uni.DateTime.formatTimeShort(d));
        }

        return categories;
    },

    getCategories: function(record) {
        var categories = [];

        Ext.Array.each(record.get('weekTemplate'), function (weekDay) {
            categories.push(weekDay.name);
        });

        return categories;
    },

    createWeekSeries: function (record) {
        var me = this,
            week,
            weekSeries = [];

        week = me.calculateWeekRepresentation(record);
        week.forEach(function (day, index) {
            var daySeries = me.createDaySerie(day, index, record);
            weekSeries = weekSeries.concat(daySeries);
        });

        return weekSeries;
    },

    calculateWeekRepresentation: function (record) {
        var me = this,
            week = [];

        Ext.Array.each(record.get('weekTemplate'), function (weekDay) {
            week.push(me.createDayRepresentation(record.dayTypes().findRecord('id', weekDay.type)));
        });

        return week;
    },

    createDayRepresentation: function (dayType) {
        var me = this,
            day = [],
            period = {};

        Ext.Array.each(dayType.ranges().getRange(), function (range, index, ranges) {
            period = {};
            period.from = range.get('from');
            if (index < ranges.length - 1) {
                period.to = ranges[index + 1].get('from');
            } else {
                period.to = {hour: 23, minute: 59}
            }
            period.event = range.get('event');
            day.push(period)
        });
        return day;
    },

    createDaySerie: function (day, index, record) {
        var me = this,
            daySerie = [];
        day.forEach(function (range) {
            var minutes = (range.to.hour * 60 + range.to.minute) - (range.from.hour * 60 + range.from.minute);
            var blockSize = (minutes / (60 * 24)) * 24;
            var s = [null, null, null, null, null, null, null];
            var label = record.events().findRecord('id', range.event).get('name');
            s[index] = blockSize;
            daySerie.push({
                data: s,
                color: me.colors[record.events().indexOf(record.events().findRecord('id', range.event))],
                showInLegend: false,
                label: label,
                range: range
            })
        });
        return daySerie.reverse();
    }
});