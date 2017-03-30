/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.ServerDetails', {
    extend: 'Ext.app.Controller',
    requires: [
        'Ext.util.TaskManager'
    ],
    stores: ['ServerDetails'],
    models: ['ServerDetails'],
    views: ['ServerDetails', 'status.Status', 'performance.Performance', 'logging.Logging'],

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'serverDetails'
        },
        {
            ref: 'statusPanel',
            selector: 'status'
        },
        {
            ref: 'mainContainer',
            selector: 'app-main'
        }
    ],

    config: {
        refreshTask : null,
        intervalInSeconds: 120,
        remoteServersVisible: false,
        visibilityUpdated: false
    },

    init: function() {
        this.setRefreshTask(Ext.util.TaskManager.newTask({
            interval: this.getIntervalInSeconds() * 1000,
            scope: this,
            run: this.refreshData
        }));

        this.control({
            'serverDetails': {afterrender: this.onAfterRender},
            'status': {afterrender: this.updateVisibilityOfRemoteServersIfStillNeeded}
        });
    },
    onAfterRender: function() {
        this.getViewPanel().setUnselectable();
        this.refreshData();
        this.getRefreshTask().start();
    },

    updateVisibilityOfRemoteServersIfStillNeeded: function() {
        if (!this.getVisibilityUpdated()) {
            this.updateVisibilityOfRemoteServers();
        }
    },

    updateVisibilityOfRemoteServers: function() {
      // TODO: no Remote ComServers in Connexo yet
      //  this.getStatusPanel().setVisibilityOfRemoteServers(this.getRemoteServersVisible());
        this.setVisibilityUpdated(true);
    },

    refreshData: function() {
        var me = this;
        this.getServerDetailsStore().load({
            callback: function(records, operation, success) {
                if (!me.getViewPanel()) {
                    return;
                }
                if (success) {
                    var storageData = me.getServerDetailsStore().first();
                    if (storageData) {
                        me.getViewPanel().setServerDetails(storageData);
                        me.getMainContainer().setServerName(storageData.get('serverName'));
                    }
                } else {
                    console.log("serverDetailsStore.load() was UNsuccessful");
                }
            }
        });
    }

});