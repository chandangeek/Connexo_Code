/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.performance.Pools', {
    extend: 'Ext.app.Controller',

    views: ['performance.Pools'],
    models: ['performance.PoolsPerformance'],
    stores: ['performance.Pools'],

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'poolsPerformance'
        }
    ],

    init: function() {
        this.control({
            'poolsPerformance': {
                afterrender: this.refreshData
            },
            'storage button#performanceRefreshBtn': {
                click: this.refreshData
            }
        });
    },

    refreshData: function() {
        var me = this;
        // Get the data from the sever (or JSON file)
        this.getPerformancePoolsStore().load({
            callback: function(records, operation, success) {
                if (success) {
                    var poolsData = me.getPerformancePoolsStore().first();
                    if (poolsData) {
                        me.getController('performance.PoolThreadChart').refreshData(poolsData.pools().data.items);
                        me.getController('performance.PoolPortChart').refreshData(poolsData.pools().data.items);
                    }
                } else {
                    console.log("performancePoolsStore.load() was UNsuccessful");
                }
            }
        });
    }

});