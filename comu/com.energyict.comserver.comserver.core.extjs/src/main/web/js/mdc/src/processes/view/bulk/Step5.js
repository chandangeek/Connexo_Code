/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step5',
    title: Uni.I18n.translate('mdc.processstep5.bulk.status','MDC','Status'),

    tbar: {
        xtype: 'panel',
        ui: 'medium',
        style: {
            padding: '0 0 0 0px'
        },
        title: Uni.I18n.translate('mdc.processstep5.bulk.retryProcess', 'MDC', 'Retry process')
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});