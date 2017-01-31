/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.status.GeneralInformation', {
    extend: 'Ext.app.Controller',

    stores: ['status.GeneralInformation'],
    models: ['status.GeneralInformation'],
    views: ['status.GeneralInformation'],

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'generalInformation'
        }
    ],

    config: {
        refreshTask : null,
        autoRefresh : false,
        secondsUntilNextRefresh : 0,
        userInfoRefreshRateInSeconds : 1
    },

    init: function() {
        this.control({
            'generalInformation': {
                afterrender: this.refreshData
            },
            'generalInformation button#refreshBtn': {
                click: this.refreshData
            },
            'generalInformation checkbox#autoRefreshChkBox': {
                change: this.onAutoRefreshChange
            },
            'generalInformation combobox#refreshRateCombo': {
                select: this.onRefreshRateChange
            }
        });

        this.setRefreshTask(Ext.TaskManager.newTask({
            interval: this.getUserInfoRefreshRateInSeconds() * 1000,
            scope: this,
            run: this.onAutoRefresh
        }));
    },

    // Refresh my own data
    refreshData: function() {
        var me = this;
        this.getStatusGeneralInformationStore().load({
            callback: function(records, operation, success) {
                if (!me.getViewPanel()) {
                    return;
                }
                if (success) {
                    var storeData = me.getStatusGeneralInformationStore().first();
                    if (storeData) {
                        me.getViewPanel().setGeneralInformation(storeData);
                        me.updateUserInfo();
                    }
                } else {
                    console.log("statusGeneralInformationStore.load() was UNsuccessful");
                }
            }
        });
    },

    // Triggered when the refresh timer ends
    onAutoRefresh: function() {
        this.setSecondsUntilNextRefresh(this.getSecondsUntilNextRefresh() - this.getUserInfoRefreshRateInSeconds());
        this.updateUserInfo();
        if (this.getSecondsUntilNextRefresh() === 0) {
            this.getViewPanel().down('#refreshBtn').getEl().dom.click(); // mimic clicking the refresh button
            this.setSecondsUntilNextRefresh(this.getViewPanel().getRefreshRateInSeconds()); // restart the cycle
        }
    },

    // Called when the refresh rate has been changed
    onRefreshRateChange: function() {
        if (this.getAutoRefresh()) {
            this.stopAutoRefresh();
            this.startAutoRefresh();
        }
    },

    // Called when this view's "Auto refresh" check box is clicked
    onAutoRefreshChange: function(checkbox) {
        if (checkbox.getValue()) {
            this.startAutoRefresh();
        } else {
            this.stopAutoRefresh();
        }
    },

    stopAutoRefresh: function() {
        if (this.getAutoRefresh()) {
            this.setAutoRefresh(false);
            this.getRefreshTask().stop();
            this.updateUserInfo();
            // console.log("auto refresh stopped -" + new Date());
        }
    },

    startAutoRefresh: function() {
        this.setAutoRefresh(true);
        this.getRefreshTask().start();
        this.setSecondsUntilNextRefresh(this.getViewPanel().getRefreshRateInSeconds());
        this.updateUserInfo();
        // console.log("auto refresh started -" + new Date());
    },

    updateUserInfo: function() {
        if (this.getAutoRefresh()) {
            var seconds = this.getSecondsUntilNextRefresh(),
                totalSeconds = this.getViewPanel().getRefreshRateInSeconds(),
                minutesLeft = Math.floor(seconds / 60),
                secondsLeft = seconds % 60;
            if (minutesLeft > 0) {
                if (secondsLeft > 0) {
                    this.getViewPanel().setWaitInfo(seconds, totalSeconds, minutesLeft + " m " + secondsLeft + ' s');
                } else {
                    this.getViewPanel().setWaitInfo(seconds, totalSeconds, minutesLeft + " m ");
                }
            } else {
                this.getViewPanel().setWaitInfo(seconds, totalSeconds, secondsLeft + ' s');
            }
        } else {
            this.getViewPanel().setWaitInfo(0, 0, '');
        }
    }

});