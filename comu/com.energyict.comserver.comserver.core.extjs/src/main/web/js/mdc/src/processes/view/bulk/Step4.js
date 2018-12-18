/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step4',
    title: 'Retry process',//Uni.I18n.translate('issue.actionDetails','ISU','Action details'),

    requires: [
        'Isu.view.issues.CloseForm',
        'Isu.view.issues.AssignIssue',
        'Isu.view.issues.SetPriority',
        'Isu.view.issues.SnoozeBulkForm',
        'Mdc.processes.view.RetryProcessDetails'
    ],

    items: [
        {
            xtype: 'displayfield',
            name: 'startProcConfirmationMsg',
            fieldLabel: 'Confirmation:',//Uni.I18n.translate('mdc.processpreviewform.device', 'MDC', 'Device'),
            itemId: 'processNameToRetry'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
