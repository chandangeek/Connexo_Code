/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step4',
    title: 'Confirmation',//'Retry process',//Uni.I18n.translate('issue.actionDetails','ISU','Action details'),

    tbar: {
        xtype: 'panel',
        ui: 'medium',
        style: {
            padding: '0 0 0 0px'
            //padding: '0 0 0 3px'
        },
        title: 'Retry process'
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});
