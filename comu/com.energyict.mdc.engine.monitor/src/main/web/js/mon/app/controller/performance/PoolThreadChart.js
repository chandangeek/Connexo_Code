/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.performance.PoolThreadChart', {
    extend: 'Ext.app.Controller',

    stores: ['performance.PoolThread'],
    models: ['performance.Pool'],
    views: ['performance.PoolThreadChart'],

    refs: [
        {
            ref: 'chartView',
            selector: 'poolThreadChart'
        }
    ],

    refreshData: function(arrayOfModels) {
        var index, maxIndex = arrayOfModels.length, maxNumberOfThreads = 0;
        for (index = 0; index < maxIndex; index++) {
            if (maxNumberOfThreads < arrayOfModels[index].data.threads) {
                maxNumberOfThreads = arrayOfModels[index].data.threads;
            }
        }
        this.getChartView().setMajorTickSteps(maxNumberOfThreads < 10 ? maxNumberOfThreads - 1 : 10);
        this.getPerformancePoolThreadStore().loadRecords(arrayOfModels);
    }
});