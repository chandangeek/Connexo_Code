/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class 'Uni.view.calendar.CalendarGraphView'
 */
Ext.define('Uni.view.calendar.CalendarGraphView', {
    extend: 'Ext.container.Container',
    alias: 'widget.calendarGraphView',
    record: null,
    router: null,
    chart: null,

    models: [
        'Uni.model.timeofuse.Calendar'
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

    colors: ['#BEE64B', '#33CC99', '#00CCCC', '#7ED4E6', '#2887C8', '#C3CDE6', '#7070CC', '#C9A0DC', '#733380', '#2D383A',
        '#5E8C31', '#7BA05B', '#4D8C57', '#3AA655', '#93DFB8', '#1AB385', '#29AB87', '#00CC99', '#00755E',
        '#8DD9CC', '#01786F', '#30BFBF', '#008080', '#8FD8D8',
        '#95E0E8', '#6CDAE7', '#76D7EA', '#0095B7', '#009DC4', '#02A4D3', '#47ABCC', '#4997D0', '#339ACC',
        '#93CCEA', '#00468C', '#0066CC', '#1560BD', '#0066FF', '#A9B2C3', '#4570E6', '#7A89B8', '#4F69C6',
        '#8D90A1', '#8C90C8', '#9999CC', '#ACACE6', '#766EC8', '#6456B7', '#3F26BF', '#8B72BE', '#652DC1', '#6B3FA0',
        '#8359A3', '#8F47B3', '#BF8FCC', '#803790', '#D6AEDD', '#C154C1', '#FC74FD', '#C5E17A', '#9DE093', '#63B76C', '#6CA67C', '#5FA777'],


    initComponent: function () {
        this.callParent(arguments);

        this.down('#graphContainer').on('afterrender', this.drawGraph, this);
    },

    drawGraph: function () {
        var me = this;
        me.chart = new Highcharts.Chart({

            chart: {
                type: 'column',
                height: 600,
                renderTo: me.down('#graphContainer').el.dom
            },
            title: {
                text: null
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
                    if (this.series.options.code === undefined) {
                        return false;
                    }
                    var fromDate = new Date(),
                        toDate = new Date(),
                        range = this.series.options.range,
                        html;

                    fromDate.setHours(range.from.hour);
                    fromDate.setMinutes(range.from.minute);
                    toDate.setHours(range.to.hour);
                    toDate.setMinutes(range.to.minute);

                    html = '<span style="font-family: Lato, Helvetica, Arial, Verdana, Sans-serif;color:#70BB51;font-size: 16px;font-weight: bold">'
                        + this.series.options.label + ' (' + this.series.options.code + ')' + '</span>'
                    html += '<table style="margin-top: 5px" ><tbody>';
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
                    animation: false,
                    dataLabels: {
                        align: 'center',
                        verticalAlign: 'middle',
                        enabled: true,
                        color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white',
                        style: {
                            textShadow: false,
                            fontWeight: 'normal',
                            fontSize: '16px',
                            fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif',
                            width: '130px'
                        },

                        formatter: function () {
                            var color = '#FFFFFF'
                            if (this.series.options.code === undefined) {
                                color = '#686868'
                            }
                            return '<span style="font-family: Lato, Helvetica, Arial, Verdana, Sans-serif;color:' + color + ';font-size: 16px;font-weight: bold">'
                                + this.series.options.label + '</span>';
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

    getCategories: function (record) {
        var me = this,
            categories = [],
            date = new Date(),
            midnightTime,
            weekArray,
            weekDay,
            offSet,
            i;
        if (record !== null) {
            weekArray = record.get('weekTemplate');
            for (i = 1; i < weekArray.length; i++) {
                weekDay = weekArray[i];
                offSet = me.calculateOffset(weekDay.date);
                midnightTime = weekDay.date * 86400 * 1000 + offSet * 60 * 1000;
                date.setTime(midnightTime);
                categories.push(weekDay.name + '<br/>' + Uni.DateTime.formatDateShort(date));
            }
        }

        return categories;
    },

    calculateOffset: function (dateInDays) {
        var date = new Date();
        date.setTime(dateInDays * 86400 * 1000 + 60 * 1000);
        return date.getTimezoneOffset();
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
            week = [],
            weekArray,
            weekDay,
            i;
        if (record !== null) {

            weekArray = record.get('weekTemplate');
            for (i = 1; i < weekArray.length; i++) {
                weekDay = weekArray[i];
                if (weekDay.inCalendar) {
                    week.push(me.createDayRepresentation(record.dayTypes().findRecord('id', weekDay.type), record.dayTypes().findRecord('id', weekArray[i - 1].type), weekArray[i-1].inCalendar));
                } else {
                    week.push(me.createEmptyDay());
                }
            }
        }

        return week;
    },

    createDayRepresentation: function (dayType, dayTypeBefore, dayBeforeInCalendar) {
        var me = this,
            day = [],
            period = {},
            firstPeriodIsMidnight = false;
        Ext.Array.each(dayType.ranges().getRange(), function (range, index, ranges) {
            period = {};
            if ((range.get('fromHour') !== 0 || range.get('fromMinute') !== 0) && !firstPeriodIsMidnight) {
                day.push(me.getLastPeriodFromLastDay(dayTypeBefore.ranges(), range, dayBeforeInCalendar));
            }

            period.from = {hour: range.get('fromHour'), minute: range.get('fromMinute')};
            if (index < ranges.length - 1) {
                period.to = {hour: ranges[index + 1].get('fromHour'), minute: ranges[index + 1].get('fromMinute')};
            } else {
                period.to = {hour: 24, minute: 00}
            }
            period.event = range.get('event');
            day.push(period)
            firstPeriodIsMidnight = true;
        });
        return day;
    },

    getLastPeriodFromLastDay: function (lastDayRanges, nextRange, dayBeforeInCalendar) {
        var range, period = {};
        period.from = {hour: 0, minute: 0};
        period.to = {hour: nextRange.get('fromHour'), minute: nextRange.get('fromMinute')};
        if(dayBeforeInCalendar) {
            range = lastDayRanges.getAt(lastDayRanges.getCount() - 1);
            period.event = range.get('event');
        } else {
            period.event = -1;
        }

        return period;
    },

    createEmptyDay: function () {
        var me = this,
            day = [];

        day.push({
            from: {hour: 0, minute: 0},
            to: {hour: 24, minute: 0},
            event: -1
        });

        return day;
    },

    createDaySerie: function (day, index, record) {
        var me = this,
            daySerie = [],
            indexOf;
        day.forEach(function (range) {
            var minutes = (range.to.hour * 60 + range.to.minute) - (range.from.hour * 60 + range.from.minute);
            var blockSize = (minutes / (60 * 24)) * 24;
            var s = [null, null, null, null, null, null, null];
            var event = record.events().findRecord('id', range.event);
            var label;
            var color;
            var code;
            s[index] = blockSize
            if (event !== null) {
                label = event.get('name');
                indexOf = record.events().indexOf(record.events().findRecord('id', range.event));
                color = me.colors[indexOf];
                code = event.get('code');
            } else {
                label = Uni.I18n.translate('general.notApplicable', 'UNI', 'Not applicable');
                color = 'rgba(0,0,0,0)';
                code = undefined;
            }
            daySerie.push({
                data: s,
                color: color,
                showInLegend: false,
                label: label,
                range: range,
                code: code
            })
        });
        return daySerie.reverse();
    }
});