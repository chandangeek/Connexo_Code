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

    day: [
        {
            from: {
                hour: 0,
                minute: 0
            },
            to: {
                hour: 3,
                minute: 18
            },
            type: 1
        }, {
            from: {
                hour: 3,
                minute: 18
            },
            to: {
                hour: 6,
                minute: 45
            },
            type: 2
        }, {
            from: {
                hour: 6,
                minute: 45
            },
            to: {
                hour: 10,
                minute: 00
            },
            type: 1
        }, {
            from: {
                hours: 10,
                minutes: 00
            },
            to: {
                hours: 15,
                minutes: 00
            },
            type: 2
        }, {
            from: {
                hours: 15,
                minutes: 00
            },
            to: {
                hours: 24,
                minutes: 00
            },
            type: 1
        }],

    day2: [
        {
            from: {
                hours: 0,
                minutes: 0
            },
            to: {
                hours: 6,
                minutes: 45
            },
            type: 2
        }, {
            from: {
                hours: 6,
                minutes: 45
            },
            to: {
                hours: 15,
                minutes: 00
            },
            type: 1
        }, {
            from: {
                hours: 15,
                minutes: 00
            },
            to: {
                hours: 24,
                minutes: 00
            },
            type: 2
        }],

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
                text: 'test'
            },
            credits: {
                enabled: false
            },

            xAxis: {
                opposite: true,
                categories: me.getCategories(me.record)
            },

            yAxis: {
                title: {
                    text: 'Hours'
                },
                categories: me.createHourCategories(),
                alternateGridColor: '#f4f2f2',
                min: 0,
                max: 24,
                reversed: true,
                tickInterval: 2,
                minorTickInterval: 1
            },

            tooltip: {
                headerFormat: '<b>{point.x}</b><br/>',
                pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
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
                        align: 'left',
                        verticalAlign: 'top',
                        enabled: true,
                        color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white',
                        style: {
                            textShadow: false,
                            opacity: 0.7,
                            fontWeight: 'normal'
                        },

                        formatter: function () {
                            return 'DAY';
                            //return this.series.options.label;
                        }
                    }
                }
            },

            series: me.createWeekSeries(me.record)
        });

    },

    createHourCategories: function () {
        var categories = [];
        for (var i = 0; i <= 24; i++) {
            categories.push(("0" + i).slice(-2) + ':00');
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
            var daySeries = me.createDaySerie(day, index);
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
                period.to = {hour: 24, minute: 00}
            }
            period.type = 1;
            day.push(period)
        });
        return day;
    },
    createDaySerie: function (day, index) {
        var daySerie = [],
            counter = 1;
        day.forEach(function (range) {
            var minutes = (range.to.hour * 60 + range.to.minute) - (range.from.hour * 60 + range.from.minute);
            var blockSize = (minutes / (60 * 24)) * 24;
            switch (counter) {
                case 1:
                    color = '#1E7D9E';
                    counter = 2;
                    break;
                case 2:
                    color = '#70BB51';
                    counter = 1;
                    break;
            }
            var s = [null, null, null, null, null, null, null];
            var label = '' + ('0' + range.from.hour).slice(-2) + ':' + ('0' + range.from.minute).slice(-2) + ' - ' + ('0' + range.to.hour).slice(-2) + ':' + ('0' + range.to.minute).slice(-2);
            s[index] = blockSize;
            daySerie.push({
                data: s,
                color: color,
                showInLegend: false,
                label: label
            })
        });
        return daySerie.reverse();
    }
});