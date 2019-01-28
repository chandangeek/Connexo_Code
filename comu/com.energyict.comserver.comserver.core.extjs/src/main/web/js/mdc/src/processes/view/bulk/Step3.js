/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step3',
    title: Uni.I18n.translate('mdc.processstep3.bulk.actionDetails','MDC','Action details'),

    requires: [
        'Mdc.processes.view.RetryProcessDetails'
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});