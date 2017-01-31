/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.ImportServicePreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.import-service-preview-form',
    router: null,

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
                fieldLabel: Uni.I18n.translate('general.importService', 'APR', 'Import service'),
                name: 'importService'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.status', 'APR', 'Status'),
                name: 'status'
            }
        ];
        me.callParent(arguments);
    },

    updateImportServicePreview: function (record) {
        var me = this;

        if (!Ext.isDefined(record)) {
            return;
        }
        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(record);

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    }
});