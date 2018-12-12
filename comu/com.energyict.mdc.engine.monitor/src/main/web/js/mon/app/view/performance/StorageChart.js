/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.performance.StorageChart', {
    extend: 'Ext.chart.Chart',
    xtype: 'storageChart',
    requires: [ 'Ext.chart.series.Line', 'Ext.chart.axis.Time', 'CSMonitor.theme.Elster'],

    theme: 'Elster:gradient',
    animate: true,
    store: 'performance.Storage',
    shadow: true,
    width: 500,
    height: 400,

    legend: {
        position: 'bottom'
    },

    config: {
        timeField: 'time',
        loadField: 'load',
        threadsField: 'threads',
        capacityField: 'capacity'
    },
    axes: [
        {
            type: 'Time',
            dateFormat: 'H:i',
            step: [Ext.Date.MINUTE, 5],
            position: 'bottom',
            fields: ['time'],
            title: 'Time'
        },
        {
            type: 'Numeric',
            minimum: 0,
            maximum: 100,
            position: 'left',
            fields: ['load'],
            title: 'Load (in %)',
            minorTickSteps: 1,
            grid: {
                odd: {
                    opacity: 1,
                    fill: '#f9f9f9',
                    stroke: '#ddd',
                    'stroke-width': 0.5
                }
            }
        },
        {
            type: 'Numeric',
            minimum: 0,
            position: 'right',
            fields: ['threads', 'capacity'],
            title: '# data storage threads & Capacity',
            minorTickSteps: 1
        }
    ],

    setStartDate: function (startDate) {
        this.axes.items[0].fromDate = startDate;
    },

    setEndDate: function (endDate) {
        this.axes.items[0].toDate = endDate;
    },

    setStep: function (stepInfoArray) {
        this.axes.items[0].step = stepInfoArray;
    },

    formatTimeInHHMMSS: function (dateInMillis) {
        var date = new Date(dateInMillis),
            hours = date.getHours(),
            minutes = date.getMinutes(),
            seconds = date.getSeconds(),
            formattedTime;
        if (hours < 10) {
            formattedTime = '0' + String(hours);
        } else {
            formattedTime = String(hours);
        }
        formattedTime += ':';
        if (minutes < 10) {
            formattedTime += ('0' + String(minutes));
        } else {
            formattedTime += String(minutes);
        }
        formattedTime += ':';
        if (seconds < 10) {
            formattedTime += ('0' + String(seconds));
        } else {
            formattedTime += String(seconds);
        }
        return formattedTime;
    },

    initComponent: function () {
        var me = this;
        this.series = [
            {
                type: 'line',
                highlight: {
                    size: 2,
                    radius: 2
                },
                style: {
                    'stroke-width': 2
                },
                axis: 'left',
                xField: me.getTimeField(),
                yField: me.getLoadField(),
                tips: {
                    trackMouse: true,
                    renderer: function (storeItem) {
                        this.update(me.getTooltip(storeItem, me.getLoadField()));
                    }
                }
            },
            {
                type: 'line',
                style: {
                    'stroke-width': 2
                },
                axis: 'right',
                xField: me.getTimeField(),
                yField: me.getCapacityField(),
                tips: {
                    trackMouse: true,
                    renderer: function (storeItem) {
                        this.update(me.getTooltip(storeItem, me.getCapacityField()));
                    }
                }
            },
            {
                type: 'line',
                style: {
                    'stroke-width': 2
                },
                axis: 'right',
                xField: me.getTimeField(),
                yField: me.getThreadsField(),
                tips: {
                    trackMouse: true,
                    renderer: function (storeItem) {
                        this.update(me.getTooltip(storeItem, me.getThreadsField()));
                    }
                }
            }
        ];

        this.callParent(arguments);
    },

    getTooltip: function (storeItem, inBold) {
        var dateInMillis = storeItem.get(this.getTimeField()),
            tooltipText = 'Time: ' + this.formatTimeInHHMMSS(dateInMillis) + '<br/>';
        if (inBold === this.getLoadField()) {
            tooltipText += '<b>';
        }
        tooltipText += ('Load: ' + storeItem.get(this.getLoadField()) + '%');
        if (inBold === this.getLoadField()) {
            tooltipText += '</b>';
        }
        tooltipText += '<br/>';
        if (inBold === this.getThreadsField()) {
            tooltipText += '<b>';
        }
        tooltipText += ('# Threads: ' + storeItem.get(this.getThreadsField()));
        if (inBold === this.getThreadsField()) {
            tooltipText += '</b>';
        }
        tooltipText += '<br/>';
        if (inBold === this.getCapacityField()) {
            tooltipText += '<b>';
        }
        tooltipText += ('Capacity: ' + storeItem.get(this.getCapacityField()));
        if (inBold === this.getCapacityField()) {
            tooltipText += '</b>';
        }
        return tooltipText;
    }

});