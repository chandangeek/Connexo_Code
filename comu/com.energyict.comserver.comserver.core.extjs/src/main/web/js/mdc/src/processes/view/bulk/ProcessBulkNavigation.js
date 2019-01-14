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
    title: Uni.I18n.translate('mdc.process.bulknavigation.actions', 'MDC', 'Bulk action'),
    items: [
        {
            itemId: 'SelectIssues',
            text: Uni.I18n.translate('mdc.process.bulknavigation.selectprocesses','MDC','Select processes')
        },
        {
            itemId: 'SelectAction',
            text: Uni.I18n.translate('mdc.process.bulknavigation.selectaction','MDC','Select action')
        },
        {
            itemId: 'actionDetails',
            text: Uni.I18n.translate('mdc.process.bulknavigation.actiondetails', 'MDC', 'Action details')
        },
        {   itemId: 'Confirmation',
            text: Uni.I18n.translate('mdc.process.bulknavigation.confirmation', 'MDC', 'Confirmation')
        },
        {
            itemId: 'Status',
            text: Uni.I18n.translate('mdc.process.bulknavigation.status', 'MDC', 'Status')
        }
    ]
});