/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.status.RunningInformation', {
    extend: 'Ext.app.Controller',

    stores: ['status.RunningInformation'],
    models: ['status.RunningInformation'],
    views: ['status.RunningInformation'],

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'runningInformation'
        }
    ],

    init: function() {
        this.control({
            'runningInformation': {
                afterrender: this.refresh
            },
            'generalInformation button#refreshBtn': {
                click: this.refresh
            }
        });
    },

    refresh: function() {
        var me = this;
        this.getStatusRunningInformationStore().load({
            callback: function(records, operation, success) {
                if (!me.getViewPanel()) {
                    return;
                }
                if (success) {
                    var storeData = me.getStatusRunningInformationStore().first();
                    if (storeData) {
                        me.getViewPanel().setRunningInformation(storeData);
                    }
                } else {
                    console.log("statusRunningInformationStore.load() was UNsuccessful");
                }
            }
        });
    }
});