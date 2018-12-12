/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.MessageServicePreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.msg-service-preview-form',
    layout: {
        type: 'vbox'
    },
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.messageService', 'APR', 'Message service'),
                name: 'messageService'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.status', 'APR', 'Status'),
                name: 'active',
                renderer: me.renderStatus
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.threads', 'APR', 'Threads'),
                name: 'numberOfThreads'
            }
        ];
        me.callParent(arguments);
    },

    updatePreview: function (msgServiceRecord) {
        var me = this;

        if (!Ext.isDefined(msgServiceRecord)) {
            return;
        }
        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(msgServiceRecord);
        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    renderStatus: function(value) {
        return value ? Uni.I18n.translate('general.active', 'APR', 'Active') : Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
    }

});