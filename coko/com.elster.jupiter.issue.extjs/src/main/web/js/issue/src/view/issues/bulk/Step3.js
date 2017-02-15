/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step3',
    title: Uni.I18n.translate('issue.actionDetails','ISU','Action details'),

    requires: [
        'Isu.view.issues.CloseForm',
        'Isu.view.issues.AssignIssue'
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});