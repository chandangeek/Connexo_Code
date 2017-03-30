/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.performance.ThreadsChart', {
    extend: 'Ext.app.Controller',

    stores: ['performance.Threads'],
    models: ['performance.Threads'],
    views: ['performance.ThreadsChart'],

    refs: [
        {
            ref: 'chart', // To be able to use further on 'getChart()' to get the corresponding chart instance
            selector: 'threadsChart'
        }
    ],

    setNumberOfThreads: function(threadsInUse, threadsNotInUse) {
        var data = [];
        data.push(
            {
                'name' : this.getChart().getThreadsInUseText(),
                'data' : threadsInUse
            }
        );
        data.push(
            {
                'name': this.getChart().getThreadsNotInUseText(),
                'data': threadsNotInUse
            }
        );
        this.getPerformanceThreadsStore().loadData(data);
    }
});