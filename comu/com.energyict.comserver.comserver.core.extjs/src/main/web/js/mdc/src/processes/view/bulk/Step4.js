/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step4',
    title: Uni.I18n.translate('mdc.processstep4.bulk.confirmation', 'MDC', 'Confirmation'),

    tbar: {
        xtype: 'panel',
        ui: 'medium',
        style: {
            padding: '0 0 0 0px'
        },
        title: Uni.I18n.translate('mdc.processstep4.bulk.retryProcess', 'MDC', 'Retry process')
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});