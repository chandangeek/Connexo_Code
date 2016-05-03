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
                text: ''
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

                    html = '<span style="font-family: Lato, Helvetica, Arial, Verdana, Sans-serif;color:#70BB51;font-size: 16px;font-weight: bold">'
                        + this.series.options.label + '</span>'
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

    getCategories: function (record) {
        var categories = [];
        if (record !== null) {
            Ext.Array.each(record.get('weekTemplate'), function (weekDay) {
                categories.push(weekDay.name);
            });
        }

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
        if (record !== null) {
            Ext.Array.each(record.get('weekTemplate'), function (weekDay) {
                week.push(me.createDayRepresentation(record.dayTypes().findRecord('id', weekDay.type)));
            });
        }

        return week;
    },

    createDayRepresentation: function (dayType) {
        var me = this,
            day = [],
            period = {};

        Ext.Array.each(dayType.ranges().getRange(), function (range, index, ranges) {
            period = {};
            period.from = {hour: range.get('fromHour'), minute: range.get('fromMinute')};
            if (index < ranges.length - 1) {
                period.to = {hour: ranges[index + 1].get('fromHour'), minute: ranges[index + 1].get('fromMinute')};
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
            daySerie = [],
            indexOf,
            colorIndex;
        day.forEach(function (range) {
            var minutes = (range.to.hour * 60 + range.to.minute) - (range.from.hour * 60 + range.from.minute);
            var blockSize = (minutes / (60 * 24)) * 24;
            var s = [null, null, null, null, null, null, null];
            var label = record.events().findRecord('id', range.event).get('name');
            s[index] = blockSize;
            indexOf = record.events().indexOf(record.events().findRecord('id', range.event));
            //colorIndex = (indexOf % 8) * 8 + Math.floor(indexOf / 8) * 8;
            daySerie.push({
                data: s,
                color: me.colors[indexOf],
                showInLegend: false,
                label: label,
                range: range
            })
        });
        return daySerie.reverse();
    }
});