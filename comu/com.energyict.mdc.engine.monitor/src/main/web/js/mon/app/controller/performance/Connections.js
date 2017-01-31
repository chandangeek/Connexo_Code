/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.performance.Connections', {
    extend: 'Ext.app.Controller',

    stores: ['performance.Connections'],
    models: ['performance.Connections'],
    views: ['performance.Connections', 'performance.ThreadsChart'],

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'connections'
        }
    ],

    init: function() {
        this.control({
            'connections': {
                afterrender: this.refresh
            },
            'storage button#performanceRefreshBtn': {
                click: this.refresh
            }
        });
    },

    refresh: function() {
        var me = this;
        this.getPerformanceConnectionsStore().load({
            callback: function(records, operation, success) {
                if (!me.getViewPanel()) {
                    return;
                }
                if (success) {
                    var connectionsInfo = me.getPerformanceConnectionsStore().first();
                    if (connectionsInfo) {
                        var threads = connectionsInfo.get('threads'),
                            threadsInUse = 0,
                            threadsNotInUse = 0;
                        Ext.Array.each(threads, function(item, index, array) {
                            if (item.name === 'inUse') {
                                threadsInUse = item.data;
                            } else if (item.name === 'notInUse') {
                                threadsNotInUse = item.data;
                            }
                        });
                        me.getViewPanel().setNumberOfThreads(threadsInUse + threadsNotInUse);

                        // Pass the number of threads to the threadsChart controller
                        me.getController('performance.ThreadsChart').setNumberOfThreads(threadsInUse, threadsNotInUse);
                    }
                } else {
                    console.log("performanceConnectionsStore.load() was UNsuccessful");
                }
            }
        });
    }

});