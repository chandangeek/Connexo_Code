/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.MonitorPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.monitor-preview-form',
    router: null,
    layout: {
        type: 'vbox'
    },
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250,
        width: 1000
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('general.messages', 'APR', 'Messages'),
            name: 'numberOfMessages'
        },
        {
            fieldLabel: Uni.I18n.translate('general.Errors', 'APR', 'Errors'),
            name: 'numberOFErrors'
        },
        {
            fieldLabel: Uni.I18n.translate('messageQueue.subscribers', 'APR', 'Used by'),
            xtype: 'fieldcontainer',
            itemId: 'used-by-field-container'
        }
    ],

    customLoadRecord: function(record) {
        var me = this,
            fieldContainer = me.down('#used-by-field-container'),
            active = Uni.I18n.translate('general.active', 'APR', 'Active'),
            inactive = Uni.I18n.translate('general.inactive', 'APR', 'Inactive'),
            serverURL,
            htmlContent = '';

        Ext.suspendLayouts();
        fieldContainer.removeAll();
        me.loadRecord(record);
        Ext.Array.each(record.get('subscriberSpecInfos'), function (subscriberSpecInfo) {
            htmlContent += '- ' + subscriberSpecInfo.displayName + '<br/>';
            Ext.Array.each(subscriberSpecInfo.appServers, function (appServer) {
                serverURL = '#/administration/appservers/' + appServer.appServerName;
                htmlContent += '&emsp;&emsp;&emsp;- ';
                htmlContent += '<a href="' + serverURL + '">' + appServer.appServerName + '</a> (' + (appServer.active ? active : inactive) + ')<br/>';
            });
            htmlContent += '<br/>';
        });
        fieldContainer.add({
            xtype: 'displayfield',
            htmlEncode: false,
            value: htmlContent
        });
        Ext.resumeLayouts(true);
    }
});
