/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.ProcessBulkNavigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    itemId: 'processesBulkNavigation',
    alias: 'widget.processses-bulk-navigation',
    width: 200,
    jumpForward: false,
    jumpBack: false,
    ui: 'medium',
    title: Uni.I18n.translate('mdc.process.bulkActions', 'MDC', 'Bulk action'),
    items: [
        {
            itemId: 'SelectIssues',
            text: "Select processes"//Uni.I18n.translate('md','ISU','Select issues')
        },
        {
            itemId: 'SelectAction',
            text: 'Select action'//Uni.I18n.translate('issue.selectAction','ISU','Select action')
        },
        {
            itemId: 'actionDetails',
            text: 'Action details'//Uni.I18n.translate('issue.actionDetails','ISU','Action details')
        },
        {   itemId: 'Confirmation',
            text: 'Confirmation'//Uni.I18n.translate('issue.confirmation','ISU','Confirmation')
        },
        {
            itemId: 'Status',
            text: 'Status'//Uni.I18n.translate('issue.status','ISU','Status')
        }
    ]
});
