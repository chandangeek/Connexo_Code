/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.performance.PoolPortChart', {
    extend: 'Ext.app.Controller',

    stores: ['performance.PoolPort'],
    models: ['performance.Pool'],
    views: ['performance.PoolPortChart'],

    refs: [
        {
            ref: 'chartView',
            selector: 'poolPortChart'
        }
    ],

    refreshData: function(arrayOfModels) {
        var index, maxIndex = arrayOfModels.length, maxNumberOfPorts = 0;
        for (index = 0; index < maxIndex; index++) {
            if (maxNumberOfPorts < arrayOfModels[index].data.ports) {
                maxNumberOfPorts = arrayOfModels[index].data.ports;
            }
        }
        this.getChartView().setMajorTickSteps(maxNumberOfPorts < 10 ? maxNumberOfPorts - 1 : 10);
        this.getPerformancePoolPortStore().loadRecords(arrayOfModels);
    }
});