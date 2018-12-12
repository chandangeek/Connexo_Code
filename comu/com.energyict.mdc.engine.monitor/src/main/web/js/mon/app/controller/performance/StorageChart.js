/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.performance.StorageChart', {
    extend: 'Ext.app.Controller',

    stores: ['performance.Storage'],
    models: ['performance.Storage'],
    views: ['performance.StorageChart', 'performance.Storage'],

    config: {
        oneHourInMillis: 60 * 60 * 1000,
        maxSpanInMillis: 24 * 60 * 60 * 1000, // 24 hours
        spanWidthForRefreshRateSwitch: 2 * 60 * 60 * 1000, // 2 hours
        refreshTask: null,
        firstRefreshRateInSeconds: 60, // Start every minute,
        secondRefreshRateInSeconds: 300, // switch to every 5 minutes when more then 2 hours of data is visible
        refreshRateSwitched: false,
        refreshWaitInfoTask: null,
        previousRefreshTimeInMillis: 0,
        nextRefreshTimeInMillis: 0,

        chartStartDate: null,
        chartEndDate: null,
        chartData: [],
        previousValue: 50,
        previousDate: null,
        // hours:     1  2  3   4   5   6   7   8   9   10
        stepForSpan: [5, 5, 10, 10, 15, 15, 30, 30, 30, 30, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60] // in minutes
    },

    refs: [
        {
            ref: 'chart', // To be able to use further on 'getChart()' to get the corresponding chart instance
            selector: 'storageChart'
        },
        {
            ref: 'storageView',
            selector: 'storage'
        }
    ],

    init: function() {
        this.control({
            'storageChart': {
                afterrender: this.onAfterRender
            },
            'storage': {
                afterrender: this.onAfterRenderStorageView
            },
            'storage button#performanceRefreshBtn': {
                click: this.addNewDataPoint
            }
        });
        this.setRefreshTask(Ext.TaskManager.newTask({
            interval: 1000,
            scope: this,
            run: this.onAutoRefresh
        }));
        this.getRefreshTask().interval = this.getFirstRefreshRateInSeconds() * 1000;

        // a. perform the following to immediately have a first data point on the graph:
        this.addNewDataPoint();
        // b. then start the timer to repeat it:
        this.getRefreshTask().start();
    },

    onAfterRender: function(panel) {
//        this.fillWithRandomData();
    },

    onAfterRenderStorageView: function(panel) {
        this.setRefreshWaitInfoTask(Ext.TaskManager.newTask({
            interval: 1000, // update the wait info every second
            scope: this,
            run: this.onRefreshWaitInfo
        }));
        this.setPreviousRefreshTimeInMillis(Date.now());
        this.setNextRefreshTimeInMillis(this.getPreviousRefreshTimeInMillis() + this.getFirstRefreshRateInSeconds() * 1000);
        this.getRefreshWaitInfoTask().start();
    },

    onRefreshWaitInfo: function() {
        this.updateWaitInfo();
    },

    onAutoRefresh: function() {
        if (this.getStorageView()) {
            this.getStorageView().down('#performanceRefreshBtn').getEl().dom.click(); // mimic clicking the refresh button
        }
    },

    setStartDate: function(date) {
        if (this.getChart()) {
            this.getChart().setStartDate(date);
        }
        this.setChartStartDate(date);
    },

    setEndDate: function(date) {
        if (this.getChart()) {
            this.getChart().setEndDate(date);
        }
        this.setChartEndDate(date);
        var spanInHours = (date.getTime() - this.getChartStartDate().getTime()) / 1000 / 60 / 60;
        if (this.getChart()) {
            this.getChart().setStep([Ext.Date.MINUTE, this.getStepForSpan()[spanInHours]]);
        }
    },

    addNewDataPoint: function() {
        var me = this;

        // A. Get the data from the sever (or JSON file)
        this.getPerformanceStorageStore().load({
            callback: function(records, operation, success) {
                if (success) {
                    var storageData = me.getPerformanceStorageStore().first();
                    if (storageData) {
                        if (storageData.get('time') === undefined || storageData.get('time') === "") {
                            console.log("Result of performanceStorageStore.load() contains incorrect data");
                        } else {
                            me.addData(storageData);
                            me.getController('performance.Storage').setPriority(storageData.get('priority'));
                        }
                    }
                } else {
                    console.log("performanceStorageStore.load() was UNsuccessful");
                }
            }
        });

        this.setPreviousRefreshTimeInMillis(Date.now());
        this.setNextRefreshTimeInMillis(this.getPreviousRefreshTimeInMillis() + this.getRefreshTask().interval);

        // B. Get the data randomly
//        var storageData = {},
//            from,
//            to,
//            now;
//        if (this.getChartStartDate() === null) {
//            now = new Date();
//        } else {
//            var offsetInMinutes = 1;
//            if (this.getRefreshRateInSeconds() !== 1 && !this.getRefreshRateSwitched()) {
//                offsetInMinutes = 5;
//                // From here on we log points every 5 minutes, so remove the past points from every minute
//                var data = [];
//                do {
//                    data.push(this.getChartData()[0]);
//                    this.getChartData().shift();
//                    this.getChartData().shift();
//                    this.getChartData().shift();
//                    this.getChartData().shift();
//                    this.getChartData().shift();
//                } while (this.getChartData().length > 0);
//
//                this.setRefreshRateSwitched(true);
//                this.setChartData(data);
//            } else if (this.getRefreshRateSwitched()) {
//                offsetInMinutes = 5;
//            }
//            now = new Date(this.getPreviousDate().getTime() + offsetInMinutes * 60 * 1000);
//        }
//        storageData.time = now.getTime();
//        this.setPreviousDate(now);
//
//        if ((this.getPreviousValue() - 5) < 0) {
//            from = 0;
//        } else {
//            from = this.getPreviousValue() - 5;
//        }
//        if ((this.getPreviousValue() + 5) > 100) {
//            to = 100;
//        } else {
//            to = this.getPreviousValue() + 5;
//        }
//        this.setPreviousValue(this.getRandom(from, to));
//        storageData.load = this.getPreviousValue();
//        storageData.priority = 'Average';
//        storageData.threads = 115;
//        storageData.capacity = 56;
//
//        this.addData(storageData);
    },

    addData: function(storageDataModel) {
        var now = new Date(storageDataModel.get('time')),
            from,
            to;
        if (this.getChartStartDate() === null) {
            this.setStartDate(this.getFiveMinuteDate(now));
        }
        if (this.getChartEndDate() === null) {
            this.setEndDate(new Date(this.getChartStartDate().getTime() + this.getOneHourInMillis()));
        } else if (now > this.getChartEndDate()) {
            // console.log("shift of end date needed");
            // Shift the graph's end date with an extra hour
            var newEndDateInMillis = this.getChartEndDate().getTime() + this.getOneHourInMillis();
            var spanInMillis = newEndDateInMillis - this.getChartStartDate().getTime();
            // console.log("span (ms):" + spanInMillis + '[' + this.getSpanWidthForRefreshRateSwitch() + ']');

            // Check that the graph's new span is then no more than the max we want
            if (spanInMillis <= this.getMaxSpanInMillis()) {
                this.setEndDate(new Date(newEndDateInMillis)); // go for it!
                if (!this.getRefreshRateSwitched() && spanInMillis > this.getSpanWidthForRefreshRateSwitch()) {
                    this.getRefreshTask().stop();
                    this.getRefreshTask().interval = this.getSecondRefreshRateInSeconds() * 1000;
                    this.getRefreshTask().start();
                    this.setRefreshRateSwitched(true);
                    // From here on we log points every 5 minutes, so remove (part of) the past points from every minute
                    var data = [];
                    do {
                        data.push(this.getChartData()[0]);
                        this.getChartData().shift();
                        this.getChartData().shift();
                        this.getChartData().shift();
                        this.getChartData().shift();
                        this.getChartData().shift();
                    } while (this.getChartData().length > 0);
                    this.setChartData(data);
                }
            } else {
                // Shift the startDate so that the newest point will be exactly on the right y-axis
                var neededOffset = now.getTime() - this.getChartEndDate().getTime();
                this.setStartDate(new Date(this.getChartStartDate().getTime() + neededOffset));
                this.setEndDate(new Date(this.getChartEndDate().getTime() + neededOffset));
                // And remove the entries out of the data array that are before the startDate
                while (this.getChartData()[0].time < this.getChartStartDate()) {
                    this.getChartData().shift();
                }
            }
        }
        this.getChartData().push(storageDataModel.data);

        this.getPerformanceStorageStore().loadData(this.getChartData());
    },

    fillWithRandomData: function() {
        // Fill up the graph random data
        var data = [],
            pointFrequency = 5, // = 1 drawing point every 10 minutes
            minutesIn24Hours = 1440,
            nr_of_points = minutesIn24Hours / pointFrequency,
            now = new Date(),
            startTime = now.getTime() / 60000,  // in minutes
            nrOfThreads,
            previousValue = 70,
            from,
            to,
            dateValue,
            i;

        this.getChart().setStartDate(now);
        for (i = 0; i < nr_of_points; i = i + 1) {
            if (i === nr_of_points / 2 + 1) {
                previousValue = 35;
            }
//            if ((previousValue - 2) < 0) {
//                from = 0;
//            } else {
//                from = previousValue - 2;
//            }
//            if ((previousValue + 2) > 100) {
//                to = 100;
//            } else {
//                to = previousValue + 2;
//            }
//            previousValue + this.getRandom(1, 5);

            if (i <= nr_of_points / 2) {
                nrOfThreads = 30;
            } else {
                nrOfThreads = 60;
            }
            dateValue = new Date((startTime + (pointFrequency * i)) * 60000);
            data.push(
                {
                    'time' : dateValue,
                    'load' : previousValue + this.getRandom(1, 10),
                    'threads': nrOfThreads,
                    'capacity': 45
                }
            );
        }
        this.getChart().setStep([Ext.Date.MINUTE, 30]);
        this.getPerformanceStorageStore().loadData(data);
        var storageController = this.getController('performance.Storage');
        storageController.setPriority(5);
    },

    getRandom: function(from, to) {
        return Math.floor(Math.random() * (to - from) + from);
    },

    getFiveMinuteDate: function(date) {
        var fiveMinutesInMillis = 5 * 60 * 1000,
            inputDateMillis = date.getTime();
        inputDateMillis -= (inputDateMillis % fiveMinutesInMillis);
        return new Date(inputDateMillis);
    },

    updateWaitInfo: function() {
        if (!this.getStorageView()) {
            return;
        }
        var seconds = Math.floor((this.getNextRefreshTimeInMillis() - Date.now()) / 1000),
            totalSeconds = Math.floor((this.getNextRefreshTimeInMillis() - this.getPreviousRefreshTimeInMillis()) / 1000),
            minutesLeft = Math.floor(seconds / 60),
            secondsLeft = seconds % 60;
        if (minutesLeft > 0) {
            if (secondsLeft > 0) {
                this.getStorageView().setWaitInfo(seconds, totalSeconds, minutesLeft + " m " + secondsLeft + ' s');
            } else {
                this.getStorageView().setWaitInfo(seconds, totalSeconds, minutesLeft + " m ");
            }
        } else {
            this.getStorageView().setWaitInfo(seconds, totalSeconds, secondsLeft + ' s');
        }
    }

});