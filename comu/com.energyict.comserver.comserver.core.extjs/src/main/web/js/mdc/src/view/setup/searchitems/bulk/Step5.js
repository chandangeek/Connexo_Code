/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step5',
    title: Uni.I18n.translate('searchItems.bulk.step5title', 'MDC', 'Step 5: Status'),
    ui: 'large',
    name: 'statusPage',
    defaults: {
        margin: '0 0 8 0'
    },
    tbar: {
        xtype: 'panel',
        ui: 'medium',
        style: {
            padding: '0 0 0 3px'
        },
        title: '',
        itemId: 'searchitemsbulkactiontitle'
    },

    showChangeDeviceConfigSuccess: function (text) {
        var widget = {
            xtype: 'uni-notification-panel',
            margin: '0 0 0 -13',
            message: Uni.I18n.translate('searchItems.bulk.devicesAddedToQueueTitle', 'MDC', 'This task has been queued'),
            type: 'success',
            additionalItems: [
                {
                    xtype: 'container',
                    html: text
                }
            ]
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts(true);
    },
    showChangeDeviceConfigFailure: function (text) {
        var widget = {
            xtype: 'uni-notification-panel',
            margin: '0 0 0 -13',
            message: Uni.I18n.translate('searchItems.bulk.devicesNotAddedToQueueTitle', 'MDC', 'This task has not been queued due to the next error:'),
            type: 'error',
            additionalItems: [
                {
                    xtype: 'container',
                    html: '<span style="color: #EB5642;">'+ text + '</span>',
                }
            ]
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts(true);
    },
    showIssueCreatedSuccess: function (text) {
        var widget = {
            xtype: 'uni-notification-panel',
            margin: '0 0 0 -13',
            message: '',
            type: 'success',
            additionalItems: [
                {
                    xtype: 'container',
                    html: text
                }
            ]
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts(true);
    },


    showSentSAPNotificationsSuccess: function (devicesCnt) {
        var widget = {
            xtype: 'uni-notification-panel',
            margin: '0 0 0 -13',
            message: Uni.I18n.translate('searchItems.bulk.registeredNotificationsHaveBeenSent', 'MDC', '{0} registered notifications have been sent', devicesCnt),
            type: 'success'
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts(true);
    },
    showSentSAPNotificationsFailure: function (text) {
        var widget = {
            xtype: 'uni-notification-panel',
            margin: '0 0 0 -13',
            message: Uni.I18n.translate('searchItems.bulk.registeredNotificationsNotSent', 'MDC', 'Registered notifications have not been sent due to the next error:'),
            type: 'error',
            additionalItems: [
                {
                    xtype: 'container',
                    html: '<span style="color: #EB5642;">'+ text + '</span>',
                }
            ]
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts(true);
    },



    initComponent: function () {
        this.callParent(arguments);
    }
});